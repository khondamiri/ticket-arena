package com.ticketarena.venue;

import com.ticketarena.common.exception.EntityNotFoundException;
import com.ticketarena.venue.dto.request.CreateVenueRequest;
import com.ticketarena.venue.dto.request.UpdateVenueRequest;
import com.ticketarena.venue.dto.response.SectionResponse;
import com.ticketarena.venue.dto.response.VenueResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional( readOnly = true )
public class VenueService {
    private final VenueRepository venueRepository;
    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public VenueResponse createVenueWithLayout( @NotNull CreateVenueRequest request ) {
        Venue savedVenue = venueRepository.save(
                Venue.builder()
                        .name( request.getName() )
                        .country( request.getCountry() )
                        .city( request.getCity() )
                        .address( request.getAddress() )
                        .totalCapacity( request.getTotalCapacity() )
                        .build()
        );

        request.getSectionRequests().forEach( secReq -> {
            Section savedSection = sectionRepository.save(
                    Section.builder()
                            .venueId( savedVenue.getId() )
                            .name( secReq.getName() )
                            .displayOrder( secReq.getDisplayOrder() )
                            .build()
            );

            savedVenue.getSections().add( savedSection );

            List< Seat > seats = new ArrayList<>();

            secReq.getSeatRequests().forEach( seatReq -> {
                seats.add(
                        Seat.builder()
                                .rowLabel( seatReq.getRowLabel() )
                                .seatNumber( seatReq.getSeatNumber() )
                                .sectionId( savedSection.getId() )
                                .build()
                );
            } );

            seatRepository.saveAll( seats );
        } );

        return toResponse( savedVenue );
    }

    public VenueResponse findById( Long id ) {
        Optional< Venue > venue = venueRepository.findByIdWithSections( id );

        if ( venue.isPresent() ) {
            return toResponse( venue.get() );
        } else {
            throw new EntityNotFoundException( "Venue not found with id: " + id );
        }
    }

    public List< VenueResponse > findAll() {
        List< Venue > venues = venueRepository.findAll();

        List< VenueResponse > responseList = new ArrayList<>();

        for ( Venue v : venues ) {
            responseList.add( toResponse( v ) );
        }
        return responseList;
    }

    @Transactional
    public VenueResponse update( Long id, UpdateVenueRequest request ) {
        Optional< Venue > venue = venueRepository.findById( id );

        if ( venue.isEmpty() ) {
            throw new EntityNotFoundException( "Venue not found with id: " + id );
        }

        if ( request.getName() != null ) {
            venue.get().setName( request.getName() );
        }

        if ( request.getCity() != null ) {
            venue.get().setCity( request.getCity() );
        }

        if ( request.getCountry() != null ) {
            venue.get().setCountry( request.getCountry() );
        }

        if ( request.getAddress() != null ) {
            venue.get().setAddress( request.getAddress() );
        }

        if ( request.getTotalCapacity() != null ) {
            venue.get().setTotalCapacity( request.getTotalCapacity() );
        }

        venueRepository.update( venue.get() );

        return toResponse( venue.get() );
    }

    private VenueResponse toResponse( Venue venue ) {
        VenueResponse response = new VenueResponse();
        response.setId( venue.getId() );
        response.setPublicId( venue.getPublicId() );
        response.setName( venue.getName() );
        response.setCity( venue.getCity() );
        response.setCountry( venue.getCountry() );
        response.setAddress( venue.getAddress() );
        response.setTotalCapacity( venue.getTotalCapacity() );
        for ( Section s : venue.getSections() ) {
            response.getSections()
                    .add(
                            toSectionResponse(
                                    s,
                                    seatRepository.countBySectionId( s.getId() )
                            )
                    );
        }
        return response;
    }

    private SectionResponse toSectionResponse( Section section, int seatCount ) {
        SectionResponse response = new SectionResponse();
        response.setId( section.getId() );
        response.setName( section.getName() );
        response.setDisplayOrder( section.getDisplayOrder() );
        response.setSeatCount( seatCount );
        return response;
    }
}
