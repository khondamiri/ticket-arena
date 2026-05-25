package com.ticketarena.repository;

import com.ticketarena.common.BaseRepositoryTests;
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

        Event event = eventRepository.save(
                Event.create(
                        organizer.getId(),
                        venue.getId(),
                        "Iye",
                        "Description",
                        OffsetDateTime.now(),
                        OffsetDateTime.now().plus( Duration.ofHours( 1 ) )
                )
        );

        List< Section > section = sectionRepository.findByVenueId( venue.getId() );
        List< Seat > seats = seatRepository.findBySectionId( section.getFirst().getId() );
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

        List< EventSeat > savedEventSeats = eventSeatRepository.findByEventId( event.getId() );

        assertThat( savedEventSeats )
                .isNotEmpty();
    }

    @Test
    void findByEventId_shouldReturnAllSeats() {

    }

    @Test
    void findAvailableByEventId_shouldReturnOnlyAvailableSeats() {

    }

    @Test
    void findById_shouldReturnEventSeat_whenExists() {

    }

    @Test
    void findByEventIdAndSeatId_shouldReturnEventSeat() {

    }

    @Test
    void update_shouldModifyEventSeatFields() {

    }

    @Test
    void countAvailableByEventId_shouldReturnCorrectCount() {

    }

    private User savedOrganizer() {
        return userRepository.save(
                User.create( "organizer@test.com", "password", "Organizer", UserRole.ORGANIZER )
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
}
