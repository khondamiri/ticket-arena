package com.ticketarena.venue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venue {
    private Long id;
    private UUID publicId;
    private String name;
    private String city;
    private String country;
    private String address;
    private Integer totalCapacity;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    @Builder.Default
    private List< Section > sections = new ArrayList<>();

    public static Venue create( String name, String city, String country, String address, Integer totalCapacity ) {
        Venue v = new Venue();
        v.setName( name );
        v.setCity( city );
        v.setCountry( country );
        v.setAddress( address );
        v.setTotalCapacity( totalCapacity );
        return v;
    }
}