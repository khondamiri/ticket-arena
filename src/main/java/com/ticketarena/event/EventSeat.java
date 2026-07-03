package com.ticketarena.event;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class EventSeat {
    private Long id;
    private Long eventId;
    private Long seatId;
    private EventSeatStatus status;
    private BigDecimal price;
    private OffsetDateTime lockedUntil;
    private Long lockedByBookingId;
    private Integer version;
    private OffsetDateTime updatedAt;
    private OffsetDateTime createdAt;

    public static EventSeat create(
            Long eventId,
            Long seatId,
            EventSeatStatus status,
            BigDecimal price,
            OffsetDateTime lockedUntil,
            Long lockedByBookingId,
            Integer version
    ) {
        EventSeat eventSeat = new EventSeat();
        eventSeat.setEventId( eventId );
        eventSeat.setSeatId( seatId );
        eventSeat.setStatus( status );
        eventSeat.setPrice( price );
        eventSeat.setLockedUntil( lockedUntil );
        eventSeat.setLockedByBookingId( lockedByBookingId );
        eventSeat.setVersion( version );
        return eventSeat;
    }

    public void incrementVersion() {
        this.version += 1;
    }
}
