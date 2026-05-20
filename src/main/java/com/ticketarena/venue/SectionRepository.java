package com.ticketarena.venue;

import com.ticketarena.common.exception.EntityNotFoundException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class SectionRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public SectionRepository( NamedParameterJdbcTemplate jdbcTemplate ) {
        this.jdbc = jdbcTemplate;
    }

    public Section save( Section section ) {
        String sql = """
                INSERT INTO sections (venue_id, name, display_order)
                VALUES (:venueId, :name, :displayOrder)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "venueId", section.getVenueId() )
                .addValue( "name", section.getName() )
                .addValue( "displayOrder", section.getDisplayOrder() );

        jdbc.update( sql, params, keyHolder, new String[]{ "id" } );

        section.setId( Objects.requireNonNull( keyHolder.getKey() ).longValue() );

        return section;
    }

    public List< Section > findByVenueId( Long venueId ) {
        String sql = """
                SELECT * FROM sections
                WHERE venue_id = :venueId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "venueId", venueId );

        return jdbc.query( sql, params, SECTION_ROW_MAPPER );
    }

    public Optional< Section > findById( Long id ) {
        String sql = """
                SELECT * FROM sections
                WHERE id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "id", id );

        List<Section> sections = jdbc.query( sql, params, SECTION_ROW_MAPPER );
        Section s = DataAccessUtils.singleResult( sections );

        if (s != null) {
            return Optional.of( s );
        } else {
            return Optional.empty();
        }
    }

    public void delete( Long id ) {
        String sql = """
                DELETE FROM sections WHERE id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "id", id );

        int affected = jdbc.update( sql, params );

        if (affected != 1) {
            throw new EntityNotFoundException( "Section not found with id: " + id );
        }
    }

    private static final RowMapper< Section > SECTION_ROW_MAPPER = ( rs, rowNum ) -> {
        Section s = new Section();
        s.setId( rs.getLong( "id" ) );
        s.setVenueId( rs.getLong( "venue_id" ) );
        s.setName( rs.getString( "name" ) );
        s.setDisplayOrder( rs.getInt( "display_order" ) );
        s.setCreatedAt( rs.getObject( "created_at", OffsetDateTime.class ) );
        return s;
    };
}
