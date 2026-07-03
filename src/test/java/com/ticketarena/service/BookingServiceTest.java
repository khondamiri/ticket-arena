package com.ticketarena.service;

import com.ticketarena.booking.BookingService;
import com.ticketarena.booking.BookingStatus;
import com.ticketarena.booking.dto.BookingResponse;
import com.ticketarena.booking.dto.InitiateBookingRequest;
import com.ticketarena.common.BaseRepositoryTests;
import com.ticketarena.common.exception.EntityNotFoundException;
import com.ticketarena.common.exception.SeatLockedException;
import com.ticketarena.common.exception.SeatNotAvailableException;
import com.ticketarena.event.Event;
import com.ticketarena.event.EventRepository;
import com.ticketarena.event.EventSeat;
import com.ticketarena.event.EventSeatRepository;
import com.ticketarena.event.EventSeatStatus;
import com.ticketarena.user.User;
import com.ticketarena.user.UserRepository;
import com.ticketarena.user.UserRole;
import com.ticketarena.venue.Seat;
import com.ticketarena.venue.SeatRepository;
import com.ticketarena.venue.Section;
import com.ticketarena.venue.SectionRepository;
import com.ticketarena.venue.Venue;
import com.ticketarena.venue.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class BookingServiceTest extends BaseRepositoryTests {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventSeatRepository eventSeatRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Test
    void initiateBooking_shouldInitiateOnlyForOne() throws InterruptedException {
        List< User > users = savedCustomers();
        User organizer = savedOrganizer();
        Venue venue = savedVenueWithSectionAndSeats();
        Event event = savedEvent( organizer, venue );

        List< Section > section = sectionRepository.findByVenueId( venue.getId() );
        List< Seat > seats = seatRepository.findBySectionId( section.getFirst().getId() );

        savedEventSeats( seats, event );
        List< EventSeat > eventSeatList = eventSeatRepository.findByEventId( event.getId() );

        EventSeat es = eventSeatList.getFirst();
        Long eventSeatId = es.getId();

        int threadCount = 10;
        try ( ExecutorService executor = Executors.newFixedThreadPool( threadCount ) ) {
            AtomicInteger success = new AtomicInteger( 0 );
            AtomicInteger failure = new AtomicInteger( 0 );
            CountDownLatch latch = new CountDownLatch( threadCount );

            for ( int i = 0; i < threadCount; i++ ) {
                final Long userId = users.get( i ).getId();
                executor.submit( () -> {
                    try {
                        bookingService.initiateBooking(
                                new InitiateBookingRequest( eventSeatId, userId )
                        );

                        success.incrementAndGet();
                    } catch ( SeatLockedException | SeatNotAvailableException e ) {
                        failure.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                } );
            }

            latch.await( 10, TimeUnit.SECONDS );

            assertThat( success.get() )
                    .isEqualTo( 1 );
            assertThat( failure.get() )
                    .isEqualTo( 9 );
        }
    }

    @Test
    void confirmBooking_shouldSetStatusConfirmed() {
        List< User > users = savedCustomers();
        User customer = users.getFirst();
        User organizer = savedOrganizer();
        Venue venue = savedVenueWithSectionAndSeats();
        Event event = savedEvent( organizer, venue );

        List< Section > section = sectionRepository.findByVenueId( venue.getId() );
        List< Seat > seats = seatRepository.findBySectionId( section.getFirst().getId() );

        savedEventSeats( seats, event );
        List< EventSeat > eventSeatList = eventSeatRepository.findByEventId( event.getId() );

        EventSeat es = eventSeatList.getFirst();
        Long eventSeatId = es.getId();

        BookingResponse bookingResponse = bookingService.initiateBooking(
                new InitiateBookingRequest( eventSeatId, customer.getId() )
        );

        BookingResponse br = bookingService.confirmBooking( bookingResponse.getId() );

        assertThat( br ).isNotNull();
        assertThat( br.getStatus() ).isEqualTo( BookingStatus.CONFIRMED );
    }

    @Test
    void cancelBooking_shouldSetStatusCancelled() {
        List< User > users = savedCustomers();
        User customer = users.getFirst();
        User organizer = savedOrganizer();
        Venue venue = savedVenueWithSectionAndSeats();
        Event event = savedEvent( organizer, venue );

        List< Section > section = sectionRepository.findByVenueId( venue.getId() );
        List< Seat > seats = seatRepository.findBySectionId( section.getFirst().getId() );

        savedEventSeats( seats, event );
        List< EventSeat > eventSeatList = eventSeatRepository.findByEventId( event.getId() );

        EventSeat es = eventSeatList.getFirst();
        Long eventSeatId = es.getId();

        BookingResponse bookingResponse = bookingService.initiateBooking(
                new InitiateBookingRequest( eventSeatId, customer.getId() )
        );

        BookingResponse br = bookingService.cancelBooking( bookingResponse.getId() );

        assertThat( br ).isNotNull();
        assertThat( br.getStatus() ).isEqualTo( BookingStatus.CANCELLED );
    }

    @Test
    void findByPublicId_shouldReturnSuccessfully() {
        List< User > users = savedCustomers();
        User customer = users.getFirst();
        User organizer = savedOrganizer();
        Venue venue = savedVenueWithSectionAndSeats();
        Event event = savedEvent( organizer, venue );

        List< Section > section = sectionRepository.findByVenueId( venue.getId() );
        List< Seat > seats = seatRepository.findBySectionId( section.getFirst().getId() );

        savedEventSeats( seats, event );
        List< EventSeat > eventSeatList = eventSeatRepository.findByEventId( event.getId() );

        EventSeat es = eventSeatList.getFirst();
        Long eventSeatId = es.getId();

        BookingResponse bookingResponse = bookingService.initiateBooking(
                new InitiateBookingRequest( eventSeatId, customer.getId() )
        );

        BookingResponse br = bookingService.findByPublicId( bookingResponse.getPublicId() );

        assertThat( br ).isNotNull();
        assertThat( br.getPublicId() ).isEqualTo( bookingResponse.getPublicId() );
    }

    @Test
    void findByPublicId_shouldThrowEntityNotFoundException() {
        assertThatThrownBy( () -> bookingService.findByPublicId( UUID.randomUUID() ) )
                .isInstanceOf( EntityNotFoundException.class );
    }

    private List< User > savedCustomers() {
        List< User > users = new ArrayList<>();
        for ( int i = 0; i < 10; i++ ) {
            users.add(
                    userRepository.save(
                            User.create(
                                    "test" + i + "@email.com",
                                    "password_hash",
                                    "test",
                                    UserRole.CUSTOMER
                            )
                    )
            );
        }
        return users;
    }

    private User savedOrganizer() {
        return userRepository.save(
                User.create( "organizer@test.com", "password", "Organizer", UserRole.ORGANIZER )
        );
    }

    private Event savedEvent( User organizer, Venue venue ) {
        return eventRepository.save(
                Event.create(
                        organizer.getId(),
                        venue.getId(),
                        "Iye",
                        "Description",
                        OffsetDateTime.now(),
                        OffsetDateTime.now().plus( Duration.ofHours( 1 ) )
                )
        );
    }

    private Venue savedVenueWithSectionAndSeats() {
        Venue savedVenue = venueRepository.save(
                Venue.create( "Venue", "Tashkent", "Uzbekistan", "13 A. Navoiy St", 100 )
        );

        Section savedSection = sectionRepository.save(
                Section.create( savedVenue.getId(), "Section", 1 )
        );

        seatRepository.save( Seat.create( savedSection.getId(), "A", 1 ) );
        seatRepository.save( Seat.create( savedSection.getId(), "A", 2 ) );
        seatRepository.save( Seat.create( savedSection.getId(), "A", 3 ) );
        seatRepository.save( Seat.create( savedSection.getId(), "A", 4 ) );
        seatRepository.save( Seat.create( savedSection.getId(), "A", 5 ) );

        seatRepository.save( Seat.create( savedSection.getId(), "B", 1 ) );
        seatRepository.save( Seat.create( savedSection.getId(), "B", 2 ) );
        seatRepository.save( Seat.create( savedSection.getId(), "B", 3 ) );
        seatRepository.save( Seat.create( savedSection.getId(), "B", 4 ) );
        seatRepository.save( Seat.create( savedSection.getId(), "B", 5 ) );

        return savedVenue;
    }

    private List< EventSeat > savedEventSeats( List< Seat > seats, Event event ) {
        List< EventSeat > eventSeatList = new ArrayList<>();

        for ( Seat s : seats ) {
            eventSeatList.add(
                    EventSeat.create(
                            event.getId(),
                            s.getId(),
                            EventSeatStatus.AVAILABLE,
                            BigDecimal.valueOf( 50.00 ),
                            null,
                            null,
                            0
                    )
            );
        }

        eventSeatRepository.saveAll( eventSeatList );

        return eventSeatList;
    }
}
