package com.ticketarena.venue.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class VenueResponse {
    @NotNull
    private Long id;
    @NotNull
    private UUID publicId;
    @NotBlank
    private String name;
    @NotBlank
    private String city;
    @NotBlank
    private String country;
    @NotBlank
    private String address;
    @NotNull
    @Min( 1 )
    private Integer totalCapacity;
    private List< SectionResponse > sections = new ArrayList<>();
}
