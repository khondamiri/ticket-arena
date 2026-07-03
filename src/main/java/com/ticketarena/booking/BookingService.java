package com.ticketarena.booking;

import com.ticketarena.booking.dto.BookingResponse;
import com.ticketarena.booking.dto.InitiateBookingRequest;
import com.ticketarena.booking.dto.TicketResponse;
import com.ticketarena.common.exception.BookingExpiredException;
import com.ticketarena.common.exception.EntityNotFoundException;
import com.ticketarena.common.exception.SeatLockedException;
import com.ticketarena.common.exception.SeatNotAvailableException;
import com.ticketarena.event.EventRepository;
import com.ticketarena.event.EventSeat;
import com.ticketarena.event.EventSeatRepository;
import com.ticketarena.event.EventSeatStatus;
import com.ticketarena.venue.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional( readOnly = true )
public class BookingService {
    private final EventRepository eventRepository;
    private final EventSeatRepository eventSeatRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public BookingResponse initiateBooking( InitiateBookingRequest request ) {
        try {
            Optional< EventSeat > esOptional = eventSeatRepository.findByIdForUpdate( request.getEventSeatId() );

            if ( esOptional.isEmpty() )
                throw new EntityNotFoundException( "EventSeat not found with id: " + request.getEventSeatId() );

            EventSeat es = esOptional.get();

            if ( es.getStatus() != EventSeatStatus.AVAILABLE )
                throw new SeatNotAvailableException( "EventSeat not found with id: " + request.getEventSeatId() );

            Booking b = bookingRepository.save(
                    Booking.builder()
                            .publicId( UUID.randomUUID() )
                            .userId( request.getUserId() )
                            .eventId( es.getEventId() )
                            .status( BookingStatus.PENDING )
                            .totalAmount( es.getPrice() )
                            .expiresAt( OffsetDateTime.now().plusMinutes( 10 ) )
                            .build()
            );

            String seatLabel = seatRepository.findSeatLabelByEventSeatId( es.getId() );

            Ticket t = ticketRepository.save( Ticket.builder()
                    .publicId(UUID.randomUUID())
                    .bookingId(b.getId())
                    .eventSeatId(es.getId())
                    .pricePaid(es.getPrice())
                    .seatLabel(seatLabel)
                    .build()
            );

            es.setStatus( EventSeatStatus.LOCKED );
            es.setLockedUntil( b.getExpiresAt() );
            es.setLockedByBookingId( b.getId() );
            es.incrementVersion();

            return BookingResponse.builder()
                    .id(b.getId())
                    .publicId(b.getPublicId())
                    .status(b.getStatus())
                    .totalAmount(b.getTotalAmount())
                    .expiresAt(b.getExpiresAt())
                    .tickets(
                            List.of( TicketResponse.builder()
                                    .id(t.getId())
                                    .publicId(t.getPublicId())
                                    .seatLabel(t.getSeatLabel())
                                    .pricePaid(t.getPricePaid())
                                    .build() )
                    )
                    .build();
        } catch ( CannotAcquireLockException e ) {
            throw new SeatLockedException( e.getMessage() );
        }
    }

    @Transactional
    public BookingResponse confirmBooking( Long bookingId ) {
        Optional< Booking > bookingOptional = bookingRepository.findById( bookingId );

        if ( bookingOptional.isEmpty() ) {
            throw new EntityNotFoundException( "Booking not found with id: " + bookingId );
        }

        Booking b = bookingOptional.get();

        if ( b.getStatus() != BookingStatus.PENDING ) {
            throw new IllegalStateException( "Illegal state of Booking: " + BookingStatus.PENDING.name() + ". Booking id: " + b.getId() );
        }

        if ( OffsetDateTime.now().isAfter( b.getExpiresAt() ) ) {
            throw new BookingExpiredException( "Booking already expired with id: " + b.getId() );
        }

        b.setStatus( BookingStatus.CONFIRMED );

        List< Ticket > tickets = ticketRepository.findByBookingId( b.getId() );
        Long esId = tickets.getFirst().getEventSeatId();
        Optional< EventSeat > eventSeatOptional = eventSeatRepository.findById( esId );

        if ( eventSeatOptional.isEmpty() ) {
            throw new EntityNotFoundException( "EventSeat not found with id: " + esId );
        }

        EventSeat es = eventSeatOptional.get();
        es.setStatus( EventSeatStatus.BOOKED );
        eventSeatRepository.update( es );

        return BookingResponse.builder()
                .id( b.getId() )
                .publicId( b.getPublicId() )
                .status( b.getStatus() )
                .totalAmount( b.getTotalAmount() )
                .expiresAt( b.getExpiresAt() )
                .tickets( tickets.stream()
                        .map( t -> TicketResponse.builder()
                                .id( t.getId() )
                                .publicId( t.getPublicId() )
                                .seatLabel( t.getSeatLabel() )
                                .pricePaid( t.getPricePaid() )
                                .build()
                        )
                        .toList()
                )
                .build();
    }

    @Transactional
    public BookingResponse cancelBooking( Long bookingId ) {
        Optional< Booking > byId = bookingRepository.findById( bookingId );

        if ( byId.isEmpty() ) {
            throw new EntityNotFoundException( "Booking not found with id: " + bookingId );
        }

        Booking b = byId.get();

        if ( b.getStatus() == BookingStatus.EXPIRED || b.getStatus() == BookingStatus.CANCELLED ) {
            throw new BookingExpiredException( "Booking expired or cancelled with id: " + bookingId );
        }

        b.setStatus( BookingStatus.CANCELLED );
        bookingRepository.updateStatus( bookingId, BookingStatus.CANCELLED );

        List< Ticket > tickets = ticketRepository.findByBookingId( b.getId() );
        Long esId = tickets.getFirst().getEventSeatId();
        Optional< EventSeat > eventSeatOptional = eventSeatRepository.findById( esId );

        if ( eventSeatOptional.isEmpty() ) {
            throw new EntityNotFoundException( "EventSeat not found with id: " + esId );
        }

        EventSeat es = eventSeatOptional.get();
        es.setStatus( EventSeatStatus.AVAILABLE );
        es.setLockedUntil( null );
        es.setLockedByBookingId( null );
        es.incrementVersion();
        eventSeatRepository.update( es );

        return BookingResponse.builder()
                .id( b.getId() )
                .publicId( b.getPublicId() )
                .status( b.getStatus() )
                .totalAmount( b.getTotalAmount() )
                .expiresAt( b.getExpiresAt() )
                .tickets( null )
                .build();
    }

    public BookingResponse findByPublicId( UUID publicId ) {
        Optional< Booking > byPublicId = bookingRepository.findByPublicId( publicId );

        if ( byPublicId.isEmpty() ) {
            throw new EntityNotFoundException( "Booking not found with public id: " + publicId );
        }

        Booking b = byPublicId.get();

        List< Ticket > tickets = ticketRepository.findByBookingId( b.getId() );

        return BookingResponse.builder()
                .id( b.getId() )
                .publicId( b.getPublicId() )
                .status( b.getStatus() )
                .totalAmount( b.getTotalAmount() )
                .expiresAt( b.getExpiresAt() )
                .tickets( tickets.stream()
                        .map( t -> TicketResponse.builder()
                                .id( t.getId() )
                                .publicId( t.getPublicId() )
                                .seatLabel( t.getSeatLabel() )
                                .pricePaid( t.getPricePaid() )
                                .build()
                        )
                        .toList()
                )
                .build();

    }
}
