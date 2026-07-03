package com.ticketarena.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    private Long id;
    private UUID publicId;
    private Long bookingId;
    private Long eventSeatId;
    private BigDecimal pricePaid;
    private String seatLabel;
    private OffsetDateTime createdAt;
}
