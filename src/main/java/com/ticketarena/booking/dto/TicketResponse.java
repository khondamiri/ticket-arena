package com.ticketarena.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class TicketResponse {
    private Long id;
    private UUID publicId;
    private String seatLabel;
    private BigDecimal pricePaid;
}
