package com.ticketarena.booking.dto;

import com.ticketarena.booking.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class BookingResponse {
    private Long id;
    private UUID publicId;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private OffsetDateTime expiresAt;
    private List<TicketResponse> tickets;
}
