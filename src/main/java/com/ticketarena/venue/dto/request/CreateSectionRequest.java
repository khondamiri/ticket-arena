package com.ticketarena.venue.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSectionRequest {
    @NotBlank
    private String name;
    @Min( 0 )
    private Integer displayOrder;
    private List< CreateSeatRequest > seatRequests = new ArrayList<>();
}
