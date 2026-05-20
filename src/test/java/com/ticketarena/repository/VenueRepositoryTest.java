package com.ticketarena.repository;

import com.ticketarena.common.BaseRepositoryTests;
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

@SpringBootTest
@Testcontainers
public class VenueRepositoryTest extends BaseRepositoryTests {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Test
    void save_shouldPersistVenue_andReturnWithGeneratedId() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );

        Venue savedVenue = venueRepository.save( venue );

        assertThat( savedVenue.getId() )
                .isNotNull();
    }

    @Test
    void findById_shouldReturnVenue_whenExists() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );

        Venue savedVenue = venueRepository.save( venue );
        Optional< Venue > venueById = venueRepository.findById( savedVenue.getId() );

        assertThat( venueById )
                .isPresent();

        assertThat( venueById.get().getId() )
                .isEqualTo( savedVenue.getId() );
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional< Venue > venueById = venueRepository.findById( 1234L );

        assertThat( venueById )
                .isNotPresent();
    }

    @Test
    void findByIdWithSections_shouldReturnVenueWithSections() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );
        Venue savedVenue = venueRepository.save( venue );

        Section s1 = Section.create( savedVenue.getId(), "section A", 1 );
        Section s2 = Section.create( savedVenue.getId(), "section B", 10 );
        Section s3 = Section.create( savedVenue.getId(), "section C", 20 );

        sectionRepository.save( s1 );
        sectionRepository.save( s2 );
        sectionRepository.save( s3 );

        Optional< Venue > venueWithSections = venueRepository.findByIdWithSections( savedVenue.getId() );

        assertThat( venueWithSections )
                .isPresent();

        assertThat( venueWithSections.get().getSections() )
                .isNotEmpty();

        assertThat( venueWithSections.get().getSections().get( 0 ).getVenueId() )
                .isEqualTo( venueWithSections.get().getId() );
    }

    @Test
    void findByIdWithSections_shouldReturnVenueWithEmptySections_whenNoSectionsExist() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );
        Venue savedVenue = venueRepository.save( venue );

        Optional< Venue > venueWithoutSections = venueRepository.findByIdWithSections( savedVenue.getId() );

        assertThat( venueWithoutSections )
                .isPresent();

        assertThat( venueWithoutSections.get().getSections() )
                .isEmpty();
    }

    @Test
    void findAll_shouldReturnAllVenues() {
        Venue venue1 = Venue.create( "venue1", "city1", "country1", "address1", 100 );
        Venue venue2 = Venue.create( "venue2", "city2", "country2", "address2", 100 );
        Venue venue3 = Venue.create( "venue3", "city3", "country3", "address3", 100 );

        venueRepository.save( venue1 );
        venueRepository.save( venue2 );
        venueRepository.save( venue3 );

        List< Venue > result = venueRepository.findAll();

        assertThat( result )
                .isNotEmpty();

        assertThat( result.size() )
                .isEqualTo( 3 );
    }

    @Test
    void update_shouldModifyFields() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );
        Venue savedVenue = venueRepository.save( venue );

        String newName = "new name";
        String newCity = "new city";

        savedVenue.setName( newName );
        savedVenue.setCity( newCity );

        venueRepository.update( savedVenue );

        Optional< Venue > updatedVenue = venueRepository.findById( savedVenue.getId() );

        assertThat( updatedVenue )
                .isPresent();

        assertThat( updatedVenue.get().getName() )
                .isEqualTo( newName );

        assertThat( updatedVenue.get().getCity() )
                .isEqualTo( newCity );
    }
}
