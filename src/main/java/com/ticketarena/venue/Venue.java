package com.ticketarena.venue;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Venue {
    private Long id;
    private String name;
    private String city;
    private String country;
    private String address;
    private Integer totalCapacity;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
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

    public void addSections( Section s ) {
        this.sections.add( s );
    }
}