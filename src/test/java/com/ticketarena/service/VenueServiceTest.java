package com.ticketarena.service;

import com.ticketarena.common.BaseRepositoryTests;
import com.ticketarena.common.exception.EntityNotFoundException;
import com.ticketarena.venue.VenueService;
import com.ticketarena.venue.dto.request.CreateSeatRequest;
import com.ticketarena.venue.dto.request.CreateSectionRequest;
import com.ticketarena.venue.dto.request.CreateVenueRequest;
import com.ticketarena.venue.dto.request.UpdateVenueRequest;
import com.ticketarena.venue.dto.response.VenueResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class VenueServiceTest extends BaseRepositoryTests {
    @Autowired
    private VenueService venueService;

    private final String SECTION_A_NAME = "Iye";
    private final String SECTION_B_NAME = "bobo";
    private final String VENUE_NAME = "Alisher Navoiy Kinoteatri";
    private final String UPDATE_VENUE_NAME = "Inter Galaxy";
    private final String CITY = "Tashkent";
    private final String UPDATE_CITY = "London";
    private final String COUNTRY = "Uzbekistan";
    private final String UPDATE_COUNTRY = "UK";
    private final String ADDRESS = "A. Navoiy 13";
    private final String UPDATE_ADDRESS = "Water st. 17";
    private final Integer TOTAL_CAPACITY = 4;

    @Test
    void createVenueWithLayout_shouldPersistVenueAndSectionsAndSeats() {
        VenueResponse response = venueService.createVenueWithLayout( buildVenueRequest() );

        assertThat( response.getId() )
                .isNotNull();

        assertThat( response.getName() )
                .isEqualTo( VENUE_NAME );

        assertThat( response.getCity() )
                .isEqualTo( CITY );

        assertThat( response.getAddress() )
                .isEqualTo( ADDRESS );

        assertThat( response.getCountry() )
                .isEqualTo( COUNTRY );

        assertThat( response.getTotalCapacity() )
                .isEqualTo( TOTAL_CAPACITY );

        assertThat( response.getSections() )
                .isNotEmpty();

        assertThat( response.getSections().getFirst().getName() )
                .isEqualTo( SECTION_A_NAME );

        assertThat( response.getSections().get( 1 ).getName() )
                .isEqualTo( SECTION_B_NAME );
    }

    @Test
    void createVenueWithLayout_shouldReturnCorrectSeatCounts() {
        VenueResponse response = venueService.createVenueWithLayout( buildVenueRequest() );

        assertThat( response.getSections().getFirst().getSeatCount() )
                .isEqualTo( 4 );
    }

    @Test
    void findById_shouldReturnVenueWithSections() {
        VenueResponse savedVenue = venueService.createVenueWithLayout( buildVenueRequest() );

        VenueResponse venueById = venueService.findById( savedVenue.getId() );

        assertThat( venueById )
                .isNotNull();

        assertThat( venueById.getSections() )
                .isNotEmpty();
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        assertThatThrownBy( () -> venueService.findById( 1234L ) )
                .isInstanceOf( EntityNotFoundException.class );
    }

    @Test
    void findAll_shouldReturnAllVenues() {
        VenueResponse savedVenue = venueService.createVenueWithLayout( buildVenueRequest() );

        List< VenueResponse > allVenues = venueService.findAll();

        assertThat( allVenues )
                .isNotEmpty();

        assertThat( allVenues.getFirst().getId() )
                .isEqualTo( savedVenue.getId() );

        assertThat( allVenues.getFirst().getName() )
                .isEqualTo( VENUE_NAME );

        assertThat( allVenues.getFirst().getTotalCapacity() )
                .isEqualTo( TOTAL_CAPACITY );
    }

    @Test
    void update_shouldModifyVenueFields() {
        VenueResponse savedVenue = venueService.createVenueWithLayout( buildVenueRequest() );
        UpdateVenueRequest request = new UpdateVenueRequest();
        request.setName( UPDATE_VENUE_NAME );
        request.setAddress( UPDATE_ADDRESS );
        request.setCity( UPDATE_CITY );
        request.setCountry( UPDATE_COUNTRY );

        VenueResponse updatedVenue = venueService.update( savedVenue.getId(), request );

        VenueResponse updatedVenueById = venueService.findById( updatedVenue.getId() );

        assertThat( updatedVenueById.getName() )
                .isEqualTo( UPDATE_VENUE_NAME );

        assertThat( updatedVenueById.getAddress() )
                .isEqualTo( UPDATE_ADDRESS );

        assertThat( updatedVenueById.getCity() )
                .isEqualTo( UPDATE_CITY );

        assertThat( updatedVenueById.getCountry() )
                .isEqualTo( UPDATE_COUNTRY );
    }

    @Test
    void update_shouldThrow_whenNotFound() {
        assertThatThrownBy( () -> venueService.update( 1234L, new UpdateVenueRequest() ) )
                .isInstanceOf( EntityNotFoundException.class );
    }

    private CreateVenueRequest buildVenueRequest() {
        CreateSeatRequest seatAOne = new CreateSeatRequest( "A", 1 );
        CreateSeatRequest seatATwo = new CreateSeatRequest( "A", 2 );
        CreateSeatRequest seatBOne = new CreateSeatRequest( "B", 1 );
        CreateSeatRequest seatBTwo = new CreateSeatRequest( "B", 2 );

        CreateSectionRequest sectionA = new CreateSectionRequest(
                SECTION_A_NAME,
                1,
                List.of( seatAOne, seatATwo, seatBOne, seatBTwo )
        );

        CreateSectionRequest sectionB = new CreateSectionRequest(
                SECTION_B_NAME,
                10,
                List.of( seatAOne, seatATwo, seatBOne )
        );

        return new CreateVenueRequest(
                VENUE_NAME,
                CITY,
                COUNTRY,
                ADDRESS,
                TOTAL_CAPACITY,
                List.of( sectionA, sectionB )
        );
    }
}
