package com.ticketarena.event;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class Event {
    private Long id;
    private UUID publicId;
    private Long organizerId;
    private Long venueId;
    private String title;
    private String description;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private EventStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static Event create(
            Long organizerId,
            Long venueId,
            String title,
            String description,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt
    ) {
        Event e = new Event();
        e.setPublicId( UUID.randomUUID() );
        e.setStatus( EventStatus.DRAFT );
        e.setOrganizerId( organizerId );
        e.setVenueId( venueId );
        e.setTitle( title );
        e.setDescription( description );
        e.setStartsAt( startsAt );
        e.setEndsAt( endsAt );
        return e;
    }
}
