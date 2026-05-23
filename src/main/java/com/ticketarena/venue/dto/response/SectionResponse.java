package com.ticketarena.venue.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectionResponse {
    @NotNull
    private Long id;
    @NotBlank
    private String name;
    @Min( 0 )
    @NotNull
    private Integer displayOrder;
    @Min( 0 )
    @NotNull
    private Integer seatCount;
}
