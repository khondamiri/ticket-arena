package com.ticketarena.venue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    private Long id;
    private Long sectionId;
    private String rowLabel;
    private Integer seatNumber;

    public static Seat create( Long sectionId, String rowLabel, Integer seatNumber ) {
        Seat s = new Seat();
        s.setSectionId( sectionId );
        s.setRowLabel( rowLabel );
        s.setSeatNumber( seatNumber );
        return s;
    }
}
