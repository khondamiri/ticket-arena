package com.ticketarena.repository;

import com.ticketarena.common.BaseRepositoryTests;
import com.ticketarena.common.exception.EntityNotFoundException;
import com.ticketarena.venue.Section;
import com.ticketarena.venue.SectionRepository;
import com.ticketarena.venue.Venue;
import com.ticketarena.venue.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class SectionRepositoryTest extends BaseRepositoryTests {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Test
    void save_shouldPersistSection() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );
        Venue savedVenue = venueRepository.save( venue );

        Section section = Section.create( savedVenue.getId(), "section A", 1 );
        Section savedSection = sectionRepository.save( section );

        assertThat( savedSection.getId() )
                .isNotNull();

        assertThat( savedSection.getVenueId() )
                .isEqualTo( savedVenue.getId() );
    }

    @Test
    void findByVenueId_shouldReturnSectionsOrderedByDisplayOrder() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );
        Venue savedVenue = venueRepository.save( venue );

        Section s1 = Section.create( savedVenue.getId(), "section A", 1 );
        Section s2 = Section.create( savedVenue.getId(), "section B", 10 );

        sectionRepository.save( s1 );
        sectionRepository.save( s2 );

        List< Section > sections = sectionRepository.findByVenueId( savedVenue.getId() );

        assertThat( sections.getFirst().getDisplayOrder() )
                .isEqualTo( 1 );
    }

    @Test
    void delete_shouldRemoveSection() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );
        Venue savedVenue = venueRepository.save( venue );

        Section section = Section.create( savedVenue.getId(), "section A", 1 );

        Section savedSection = sectionRepository.save( section );

        sectionRepository.delete( savedSection.getId() );
        Optional< Section > result = sectionRepository.findById( savedSection.getId() );

        assertThat( result )
                .isNotPresent();
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        assertThatThrownBy( () -> sectionRepository.delete( 1234L ) )
                .isInstanceOf( EntityNotFoundException.class );

    }
}
