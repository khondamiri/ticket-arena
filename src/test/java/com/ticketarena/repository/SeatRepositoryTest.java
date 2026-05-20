package com.ticketarena.repository;

import com.ticketarena.common.BaseRepositoryTests;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class SeatRepositoryTest extends BaseRepositoryTests {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    void save_shouldPersistSeat() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );
        Venue savedVenue = venueRepository.save( venue );

        Section section = Section.create( savedVenue.getId(), "section A", 1 );
        Section savedSection = sectionRepository.save( section );

        Seat seat = Seat.create( savedSection.getId(), "A", 1 );
        Seat savedSeat = seatRepository.save( seat );

        assertThat( savedSeat.getId() )
                .isNotNull();
    }

    @Test
    void saveAll_shouldPersistMultipleSeats() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );
        Venue savedVenue = venueRepository.save( venue );

        Section section = Section.create( savedVenue.getId(), "section A", 1 );
        Section savedSection = sectionRepository.save( section );

        List< Seat > seats = new ArrayList<>();

        for ( int i = 1; i <= 50; i++ ) {
            String rowLabel;

            if ( i <= 10 ) {
                rowLabel = "A";
            } else if ( i > 10 && i <= 20 ) {
                rowLabel = "B";
            } else if ( i > 20 && i <= 30 ) {
                rowLabel = "C";
            } else if ( i > 30 && i <= 40 ) {
                rowLabel = "D";
            } else {
                rowLabel = "E";
            }

            Seat seat = Seat.create( savedSection.getId(), rowLabel, i );

            seats.add( seat );
        }

        seatRepository.saveAll( seats );

        List< Seat > seatsBySectionId = seatRepository.findBySectionId( savedSection.getId() );

        assertThat( seatsBySectionId )
                .isNotEmpty();

        assertThat( seatsBySectionId.size() )
                .isEqualTo( seats.size() );
    }

    @Test
    void findBySectionId_shouldReturnSeatsOrderedByRowThenSeatNumber() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );
        Venue savedVenue = venueRepository.save( venue );

        Section section = Section.create( savedVenue.getId(), "section A", 1 );
        Section savedSection = sectionRepository.save( section );

        List< Seat > seats = new ArrayList<>();

        for ( int i = 1; i <= 50; i++ ) {
            String rowLabel;

            if ( i <= 10 ) {
                rowLabel = "A";
            } else if ( i > 10 && i <= 20 ) {
                rowLabel = "B";
            } else if ( i > 20 && i <= 30 ) {
                rowLabel = "C";
            } else if ( i > 30 && i <= 40 ) {
                rowLabel = "D";
            } else {
                rowLabel = "E";
            }

            Seat seat = Seat.create( savedSection.getId(), rowLabel, i );

            seats.add( seat );
        }

        seatRepository.saveAll( seats );

        List< Seat > seatsBySectionId = seatRepository.findBySectionId( savedSection.getId() );

        assertThat( seatsBySectionId )
                .isNotEmpty();


        for ( int i = 1; i <= 50; i++ ) {
            String rowLabel;

            if ( i <= 10 ) {
                rowLabel = "A";
            } else if ( i <= 20 ) {
                rowLabel = "B";
            } else if ( i <= 30 ) {
                rowLabel = "C";
            } else if ( i <= 40 ) {
                rowLabel = "D";
            } else {
                rowLabel = "E";
            }

            assertThat( seatsBySectionId.get( i - 1 ).getSeatNumber() )
                    .isEqualTo( i );

            assertThat( seatsBySectionId.get( i - 1 ).getRowLabel() )
                    .isEqualTo( rowLabel );
        }
    }

    @Test
    void countBySectionId_shouldReturnCorrectCount() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );
        Venue savedVenue = venueRepository.save( venue );

        Section section = Section.create( savedVenue.getId(), "section A", 1 );
        Section savedSection = sectionRepository.save( section );

        List< Seat > seats = new ArrayList<>();

        for ( int i = 1; i <= 50; i++ ) {
            Seat seat = Seat.create( savedSection.getId(), "A", i );

            seats.add( seat );
        }

        seatRepository.saveAll( seats );

        int count = seatRepository.countBySectionId( savedSection.getId() );

        assertThat( count )
                .isEqualTo( 50 );
    }

    @Test
    void deleteBySectionId_shouldRemoveAllSeats() {
        Venue venue = Venue.create( "venue", "city", "country", "address", 100 );
        Venue savedVenue = venueRepository.save( venue );

        Section section = Section.create( savedVenue.getId(), "section A", 1 );
        Section savedSection = sectionRepository.save( section );

        List< Seat > seats = new ArrayList<>();

        for ( int i = 1; i <= 50; i++ ) {
            Seat seat = Seat.create( savedSection.getId(), "A", i );

            seats.add( seat );
        }

        seatRepository.saveAll( seats );

        seatRepository.deleteBySectionId( savedSection.getId() );

        int countAfterDeleting = seatRepository.countBySectionId( savedSection.getId() );

        assertThat( countAfterDeleting )
                .isEqualTo( 0 );
    }
}
