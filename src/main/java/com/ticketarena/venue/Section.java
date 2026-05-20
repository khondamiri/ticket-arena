package com.ticketarena.venue;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class Section {
    private Long id;
    private Long venueId;
    private String name;
    private Integer displayOrder;
    private OffsetDateTime createdAt;

    public static Section create( Long venueId, String name, Integer displayOrder ) {
        Section s = new Section();
        s.setVenueId( venueId );
        s.setName( name );
        s.setDisplayOrder( displayOrder );
        return s;
    }
}
