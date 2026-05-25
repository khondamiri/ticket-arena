package com.ticketarena.venue;

import com.ticketarena.common.exception.EntityNotFoundException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VenueRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public VenueRepository( NamedParameterJdbcTemplate jdbcTemplate ) {
        this.jdbc = jdbcTemplate;
    }

    public Venue save( Venue venue ) {
        String sql = """
                INSERT INTO venues (name, country, city, address, total_capacity)
                VALUES (:name, :country, :city, :address, :totalCapacity)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "name", venue.getName() )
                .addValue( "country", venue.getCountry() )
                .addValue( "city", venue.getCity() )
                .addValue( "address", venue.getAddress() )
                .addValue( "totalCapacity", venue.getTotalCapacity() );

        jdbc.update( sql, params, keyHolder, new String[]{ "id" } );

        venue.setId( Objects.requireNonNull( keyHolder.getKey() ).longValue() );

        return venue;
    }

    public Optional< Venue > findById( Long id ) {
        String sql = """
                SELECT * FROM venues
                WHERE id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource().addValue( "id", id );

        List< Venue > venues = jdbc.query( sql, params, VENUE_ROW_MAPPER );
        Venue venue = DataAccessUtils.singleResult( venues );

        if ( venue != null ) {
            return Optional.of( venue );
        } else {
            return Optional.empty();
        }
    }

    public Optional< Venue > findByIdWithSections( Long id ) {
        String sql = """
                SELECT
                    v.id AS v_id,
                    v.public_id AS v_public_id,
                    v.name AS v_name,
                    v.country AS v_country,
                    v.city AS v_city,
                    v.address AS v_address,
                    v.total_capacity AS v_total_capacity,
                    v.updated_at AS v_updated_at,
                    v.created_at AS v_created_at,
                    s.id AS s_id,
                    s.name AS s_name,
                    s.display_order AS s_display_order,
                    s.created_at AS s_created_at
                FROM venues AS v
                LEFT JOIN sections AS s ON v.id = s.venue_id 
                WHERE v.id = :venueId
                ORDER BY s.display_order
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "venueId", id );

        List< Venue > venues = jdbc.query( sql, params, VENUE_WITH_SECTIONS_EXTRACTOR );
        Venue venue = DataAccessUtils.singleResult( venues );

        if ( venue != null ) {
            return Optional.of( venue );
        } else {
            return Optional.empty();
        }
    }

    public List< Venue > findAll() {
        String sql = """
                SELECT * FROM venues ORDER BY name;
                """;

        return jdbc.query( sql, VENUE_ROW_MAPPER );
    }

    public void update( Venue venue ) {
        String sql = """
                UPDATE venues
                SET name = :name,
                    city = :city,
                    country = :country,
                    address = :address,
                    total_capacity = :totalCapacity,
                    updated_at = NOW()
                WHERE id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "id", venue.getId() )
                .addValue( "name", venue.getName() )
                .addValue( "city", venue.getCity() )
                .addValue( "country", venue.getCountry() )
                .addValue( "address", venue.getAddress() )
                .addValue( "totalCapacity", venue.getTotalCapacity() );

        int affected = jdbc.update( sql, params );

        if ( affected != 1 ) {
            throw new EntityNotFoundException( "Venue not found with id: " + venue.getId() );
        }
    }

    private static final RowMapper< Venue > VENUE_ROW_MAPPER = ( rs, rowNum ) -> {
        Venue venue = new Venue();
        venue.setId( rs.getLong( "id" ) );
        venue.setPublicId( rs.getObject( "public_id", UUID.class ) );
        venue.setName( rs.getString( "name" ) );
        venue.setCountry( rs.getString( "country" ) );
        venue.setCity( rs.getString( "city" ) );
        venue.setAddress( rs.getString( "address" ) );
        venue.setTotalCapacity( rs.getInt( "total_capacity" ) );
        venue.setUpdatedAt( rs.getObject( "updated_at", OffsetDateTime.class ) );
        venue.setCreatedAt( rs.getObject( "created_at", OffsetDateTime.class ) );
        return venue;
    };

    private static final ResultSetExtractor< List< Venue > > VENUE_WITH_SECTIONS_EXTRACTOR = rs -> {
        Map< Long, Venue > venueMap = new LinkedHashMap<>();

        while ( rs.next() ) {
            Long venueId = rs.getLong( "v_id" );

            if ( !venueMap.containsKey( venueId ) ) {
                Venue v = new Venue();
                v.setId( venueId );
                v.setPublicId( rs.getObject( "v_public_id", UUID.class ) );
                v.setName( rs.getString( "v_name" ) );
                v.setCountry( rs.getString( "v_country" ) );
                v.setCity( rs.getString( "v_city" ) );
                v.setAddress( rs.getString( "v_address" ) );
                v.setTotalCapacity( rs.getInt( "v_total_capacity" ) );
                v.setUpdatedAt( rs.getObject( "v_updated_at", OffsetDateTime.class ) );
                v.setCreatedAt( rs.getObject( "v_created_at", OffsetDateTime.class ) );
                venueMap.put( venueId, v );
            }

            Long sectionId = rs.getObject( "s_id", Long.class );
            if ( sectionId != null ) {
                Section s = new Section();
                s.setId( rs.getLong( "s_id" ) );
                s.setVenueId( venueId );
                s.setName( rs.getString( "s_name" ) );
                s.setDisplayOrder( rs.getInt( "s_display_order" ) );
                s.setCreatedAt( rs.getObject( "s_created_at", OffsetDateTime.class ) );
                venueMap.get( venueId ).getSections().add( s );
            }
        }
        return new ArrayList<>( venueMap.values() );
    };
}
