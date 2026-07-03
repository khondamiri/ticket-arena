package com.ticketarena.repository;

import com.ticketarena.common.BaseRepositoryTests;
import com.ticketarena.common.exception.EntityNotFoundException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class EventSeatRepositoryTest extends BaseRepositoryTests {
    @Autowired
    private EventSeatRepository eventSeatRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Test
    void saveAll_shouldPersistEventSeats() {
        User organizer = savedOrganizer();
        Venue venue = savedVenueWithSectionAndSeats();
        Event event = savedEvent( organizer, venue );

        List< Section > section = sectionRepository.findByVenueId( venue.getId() );
        List< Seat > seats = seatRepository.findBySectionId( section.getFirst().getId() );
        List< EventSeat > eventSeatList = savedEventSeats( seats, event );

        List< EventSeat > saved = eventSeatRepository.findByEventId( event.getId() );

        assertThat( saved )
                .hasSize( eventSeatList.size() );
    }

    @Test
    void findByEventId_shouldReturnAllSeats() {
        User organizer = savedOrganizer();
        Venue venue = savedVenueWithSectionAndSeats();
        Event event = savedEvent( organizer, venue );

        List< Section > section = sectionRepository.findByVenueId( venue.getId() );
        List< Seat > seats = seatRepository.findBySectionId( section.getFirst().getId() );
        List< EventSeat > eventSeatList = savedEventSeats( seats, event );

        Map< Long, Long > eventSeatsMap = new HashMap<>();
        eventSeatList.forEach( es -> eventSeatsMap.put( es.getSeatId(), es.getEventId() ) );

        List< EventSeat > savedEventSeats = eventSeatRepository.findByEventId( event.getId() );

        assertThat( savedEventSeats )
                .isNotEmpty();

        for ( EventSeat ses : savedEventSeats ) {
            assertThat( eventSeatsMap )
                    .containsKey( ses.getSeatId() );
        }
    }

    @Test
    void findAvailableByEventId_shouldReturnOnlyAvailableSeats() {
        User organizer = savedOrganizer();
        Venue venue = savedVenueWithSectionAndSeats();

        Event event = savedEvent( organizer, venue );

        List< Section > section = sectionRepository.findByVenueId( venue.getId() );
        List< Seat > seats = seatRepository.findBySectionId( section.getFirst().getId() );
        List< EventSeat > eventSeatList = savedEventSeats( seats, event );

        List< EventSeat > available = eventSeatRepository.findAvailableByEventId( event.getId() );

        assertThat( available )
                .isNotEmpty();

        assertThat( available.size() )
                .isEqualTo( eventSeatList.size() );
    }

    @Test
    void findById_shouldReturnEventSeat_whenExists() {
        User organizer = savedOrganizer();
        Venue venue = savedVenueWithSectionAndSeats();

        Event event = savedEvent( organizer, venue );

        List< Section > section = sectionRepository.findByVenueId( venue.getId() );
        List< Seat > seats = seatRepository.findBySectionId( section.getFirst().getId() );

        savedEventSeats( seats, event );

        List< EventSeat > savedEventSeats = eventSeatRepository.findByEventId( event.getId() );

        Optional< EventSeat > eventSeatById = eventSeatRepository.findById( savedEventSeats.getFirst().getId() );

        assertThat( eventSeatById )
                .isPresent();

        assertThat( eventSeatById.get().getId() )
                .isEqualTo( savedEventSeats.getFirst().getId() );
    }

    @Test
    void findByEventIdAndSeatId_shouldReturnEventSeat() {
        User organizer = savedOrganizer();
        Venue venue = savedVenueWithSectionAndSeats();

        Event event = savedEvent( organizer, venue );

        List< Section > section = sectionRepository.findByVenueId( venue.getId() );
        List< Seat > seats = seatRepository.findBySectionId( section.getFirst().getId() );
        List< EventSeat > eventSeatList = savedEventSeats( seats, event );

        Optional< EventSeat > eventSeatById = eventSeatRepository.findByEventIdAndSeatId(
                eventSeatList.getFirst().getEventId(),
                eventSeatList.getFirst().getSeatId()
        );

        assertThat( eventSeatById )
                .isPresent();

        assertThat( eventSeatById.get().getEventId() )
                .isEqualTo( eventSeatList.getFirst().getEventId() );

        assertThat( eventSeatById.get().getSeatId() )
                .isEqualTo( eventSeatList.getFirst().getSeatId() );
    }

    @Test
    void update_shouldModifyEventSeatFields() {
        User organizer = savedOrganizer();
        Venue venue = savedVenueWithSectionAndSeats();

        Event event = savedEvent( organizer, venue );

        List< Section > section = sectionRepository.findByVenueId( venue.getId() );
        List< Seat > seats = seatRepository.findBySectionId( section.getFirst().getId() );

        savedEventSeats( seats, event );

        String sql = """
                INSERT INTO bookings (public_id, user_id, event_id, status, total_amount, expires_at)
                VALUES (?, ?, ?, ?, ?, NOW())
                """;

        jdbc.update( sql,
                UUID.randomUUID(),
                organizer.getId(),
                event.getId(),
                "PENDING",
                100
        );

        List< EventSeat > savedEventSeats = eventSeatRepository.findByEventId( event.getId() );

        Long id = savedEventSeats.getFirst().getId();
        Optional< EventSeat > eventSeatById = eventSeatRepository.findById( id );

        EventSeat update = eventSeatById.orElseThrow( () -> new EntityNotFoundException( "EventSeat not found with id: " + id ) );
        update.setStatus( EventSeatStatus.BOOKED );
        update.setLockedUntil( OffsetDateTime.now().plus( Duration.ofDays( 10 ) ) );

        eventSeatRepository.update( update );

        Optional< EventSeat > eventSeatAfterUpdate = eventSeatRepository.findById( savedEventSeats.getFirst().getId() );
        EventSeat afterUpdate = eventSeatAfterUpdate.orElseThrow( () -> new EntityNotFoundException( "EventSeat not found with id: " + id ) );

        assertThat( afterUpdate.getStatus() )
                .isNotEqualTo( savedEventSeats.getFirst().getStatus() );

        assertThat( afterUpdate.getId() )
                .isEqualTo( update.getId() );

        assertThat( afterUpdate.getVersion() )
                .isNotEqualTo( savedEventSeats.getFirst().getVersion() );
    }

    @Test
    void countAvailableByEventId_shouldReturnCorrectCount() {
        User organizer = savedOrganizer();
        Venue venue = savedVenueWithSectionAndSeats();

        Event event = savedEvent( organizer, venue );

        List< Section > section = sectionRepository.findByVenueId( venue.getId() );
        List< Seat > seats = seatRepository.findBySectionId( section.getFirst().getId() );
        List< EventSeat > eventSeatList = savedEventSeats( seats, event );

        int result = eventSeatRepository.countAvailableByEventId( event.getId() );

        assertThat( result )
                .isEqualTo( eventSeatList.size() );
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
                Venue.create( "Test Venue", "Tashkent", "Uzbekistan", "13 A. Navoiy St", 100 )
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
