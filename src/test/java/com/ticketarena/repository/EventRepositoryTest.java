package com.ticketarena.repository;

import com.ticketarena.common.BaseRepositoryTests;
import com.ticketarena.event.Event;
import com.ticketarena.event.EventRepository;
import com.ticketarena.event.EventStatus;
import com.ticketarena.user.User;
import com.ticketarena.user.UserRepository;
import com.ticketarena.user.UserRole;
import com.ticketarena.venue.Venue;
import com.ticketarena.venue.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class EventRepositoryTest extends BaseRepositoryTests {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Test
    void save_shouldPersistEvent_andReturnWithGeneratedId() {
        Event savedEvent = eventRepository.save(
                Event.create(
                        savedOrganizer().getId(),
                        savedVenue().getId(),
                        "Title",
                        "Description",
                        OffsetDateTime.now(),
                        OffsetDateTime.now().plus( Duration.ofHours( 1 ) )
                )
        );

        assertThat( savedEvent.getId() )
                .isNotNull();
    }

    @Test
    void findById_shouldReturnEvent_whenExists() {
        Event savedEvent = eventRepository.save(
                Event.create(
                        savedOrganizer().getId(),
                        savedVenue().getId(),
                        "Title",
                        "Description",
                        OffsetDateTime.now(),
                        OffsetDateTime.now().plus( Duration.ofHours( 1 ) )
                )
        );

        Optional< Event > eventById = eventRepository.findById( savedEvent.getId() );

        assertThat( eventById )
                .isPresent();

        assertThat( eventById.get().getId() )
                .isEqualTo( savedEvent.getId() );
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional< Event > eventById = eventRepository.findById( 1234L );

        assertThat( eventById )
                .isNotPresent();
    }

    @Test
    void findByPublicId_shouldReturnEvent_whenExists() {
        Event savedEvent = eventRepository.save(
                Event.create(
                        savedOrganizer().getId(),
                        savedVenue().getId(),
                        "Title",
                        "Description",
                        OffsetDateTime.now(),
                        OffsetDateTime.now().plus( Duration.ofHours( 1 ) )
                )
        );

        Optional< Event > eventPublicById = eventRepository.findByPublicId( savedEvent.getPublicId() );

        assertThat( eventPublicById )
                .isPresent();

        assertThat( eventPublicById.get().getPublicId() )
                .isEqualTo( savedEvent.getPublicId() );
    }

    @Test
    void findByPublicId_shouldReturnEmpty_whenNotExists() {
        Optional< Event > eventPublicById = eventRepository.findByPublicId( UUID.randomUUID() );

        assertThat( eventPublicById )
                .isNotPresent();
    }

    @Test
    void findByStatus_shouldReturnOnlyPublishedEvents() {
        Event savedEvent = eventRepository.save(
                Event.create(
                        savedOrganizer().getId(),
                        savedVenue().getId(),
                        "Title",
                        "Description",
                        OffsetDateTime.now(),
                        OffsetDateTime.now().plus( Duration.ofHours( 1 ) )
                )
        );

        eventRepository.updateStatus( savedEvent.getId(), EventStatus.PUBLISHED );

        List< Event > eventList = eventRepository.findByStatus( EventStatus.PUBLISHED );
        assertThat( eventList )
                .isNotEmpty();

        assertThat( eventList.getFirst().getStatus() )
                .isEqualTo( EventStatus.PUBLISHED );
    }

    @Test
    void findByVenueId_shouldReturnEventsForVenue() {
        Venue venue = savedVenue();
        Event savedEvent = eventRepository.save(
                Event.create(
                        savedOrganizer().getId(),
                        venue.getId(),
                        "Title",
                        "Description",
                        OffsetDateTime.now(),
                        OffsetDateTime.now().plus( Duration.ofHours( 1 ) )
                )
        );

        List< Event > eventByVenueId = eventRepository.findByVenueId( savedEvent.getId() );

        assertThat( eventByVenueId )
                .isNotEmpty();

        assertThat( eventByVenueId.getFirst().getVenueId() )
                .isEqualTo( savedEvent.getVenueId() );
    }

    @Test
    void updateStatus_shouldChangeEventStatus() {
        Event savedEvent = eventRepository.save(
                Event.create(
                        savedOrganizer().getId(),
                        savedVenue().getId(),
                        "Title",
                        "Description",
                        OffsetDateTime.now(),
                        OffsetDateTime.now().plus( Duration.ofHours( 1 ) )
                )
        );

        Optional< Event > beforeChangingStatus = eventRepository.findById( savedEvent.getId() );

        eventRepository.updateStatus( savedEvent.getId(), EventStatus.PUBLISHED );

        Optional< Event > afterChangingStatus = eventRepository.findById( savedEvent.getId() );

        assertThat( beforeChangingStatus )
                .isPresent();

        assertThat( afterChangingStatus )
                .isPresent();

        assertThat( beforeChangingStatus.get().getStatus() )
                .isEqualTo( EventStatus.DRAFT );

        assertThat( afterChangingStatus.get().getStatus() )
                .isEqualTo( EventStatus.PUBLISHED );
    }

    @Test
    void update_shouldModifyMutableFields() {
        Event savedEvent = eventRepository.save(
                Event.create(
                        savedOrganizer().getId(),
                        savedVenue().getId(),
                        "Title",
                        "Description",
                        OffsetDateTime.now(),
                        OffsetDateTime.now().plus( Duration.ofHours( 1 ) )
                )
        );

        Optional< Event > beforeUpdate = eventRepository.findById( savedEvent.getId() );

        savedEvent.setTitle( "New Title" );
        savedEvent.setDescription( "New Description" );
        savedEvent.setEndsAt( OffsetDateTime.now().plus( Duration.ofHours( 2 ) ) );

        eventRepository.update( savedEvent );

        Optional< Event > afterUpdate = eventRepository.findById( savedEvent.getId() );

        assertThat( afterUpdate )
                .isPresent();

        assertThat( beforeUpdate )
                .isPresent();

        assertThat( afterUpdate.get().getTitle() )
                .isEqualTo( savedEvent.getTitle() );

        assertThat( afterUpdate.get().getDescription() )
                .isEqualTo( savedEvent.getDescription() );

        assertThat( afterUpdate.get().getEndsAt() )
                .isNotEqualTo( beforeUpdate.get().getEndsAt() );
    }

    private User savedOrganizer() {
        return userRepository.save(
                User.create( "organizer@test.com", "password", "Organizer", UserRole.ORGANIZER )
        );
    }

    private Venue savedVenue() {
        return venueRepository.save(
                Venue.create( "Test Venue", "Tashkent", "Uzbekistan", "13 A. Navoiy St", 100 )
        );
    }
}
