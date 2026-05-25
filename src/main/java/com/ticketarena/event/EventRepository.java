package com.ticketarena.event;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EventRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public EventRepository( NamedParameterJdbcTemplate jdbcTemplate ) {
        this.jdbc = jdbcTemplate;
    }

    public Event save( Event event ) {
        String sql = """
                INSERT INTO events (public_id, organizer_id, venue_id, title, description, starts_at, ends_at, status )
                VALUES (:publicId, :organizerId, :venueId, :title, :description, :startsAt, :endsAt, :status)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "publicId", event.getPublicId() )
                .addValue( "organizerId", event.getOrganizerId() )
                .addValue( "venueId", event.getVenueId() )
                .addValue( "title", event.getTitle() )
                .addValue( "description", event.getDescription() )
                .addValue( "startsAt", event.getStartsAt() )
                .addValue( "endsAt", event.getEndsAt() )
                .addValue( "status", event.getStatus().name() );

        jdbc.update( sql, params, keyHolder, new String[]{ "id" } );

        event.setId( (Long) keyHolder.getKeys().get( "id" ) );

        return event;
    }

    public Optional< Event > findById( Long id ) {
        String sql = """
                SELECT *
                FROM events
                WHERE
                    id = :id AND
                    deleted_at IS NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "id", id );

        Event event = DataAccessUtils.singleResult( jdbc.query( sql, params, EVENT_ROW_MAPPER ) );
        if ( event != null ) {
            return Optional.of( event );
        } else {
            return Optional.empty();
        }
    }

    public Optional< Event > findByPublicId( UUID publicId ) {
        String sql = """
                SELECT *
                FROM events
                WHERE
                    public_id = :publicId AND
                    deleted_at IS NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "publicId", publicId );

        Event event = DataAccessUtils.singleResult( jdbc.query( sql, params, EVENT_ROW_MAPPER ) );
        if ( event != null ) {
            return Optional.of( event );
        } else {
            return Optional.empty();
        }
    }

    public List< Event > findByStatus( EventStatus status ) {
        String sql = """
                SELECT *
                FROM events
                WHERE
                    status = :status AND
                    deleted_at IS NULL
                ORDER BY starts_at
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "status", status.name() );

        return jdbc.query( sql, params, EVENT_ROW_MAPPER );
    }

    public List< Event > findByVenueId( Long venueId ) {
        String sql = """
                SELECT *
                FROM events
                WHERE
                    venue_id = :venueId AND
                    deleted_at IS NULL
                ORDER BY starts_at
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "venueId", venueId );

        return jdbc.query( sql, params, EVENT_ROW_MAPPER );
    }

    public void updateStatus( Long id, EventStatus newStatus ) {
//        findById( id ).orElseThrow( () -> new EntityNotFoundException( "Event not found with id: " + id ) );

//        if ( !event.getStatus().canTransitionTo( newStatus ) ) {
//            throw new IllegalStateException(
//                    "Invalid transition from " + event.getStatus() + " to " + newStatus + "in event with id: " + id
//            );
//        }

        String sql = """
                UPDATE events
                SET status = :newStatus
                WHERE 
                    id = :id AND
                    deleted_at IS NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "id", id )
                .addValue( "newStatus", newStatus.name() );

        jdbc.update( sql, params );
    }

    public void update( Event event ) {
        String sql = """
                UPDATE events
                SET title = :title,
                    description = :description,
                    starts_at = :startsAt,
                    ends_at = :endsAt,
                    updated_at = NOW()
                WHERE
                    id = :id AND
                    deleted_at IS NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "id", event.getId() )
                .addValue( "title", event.getTitle() )
                .addValue( "description", event.getDescription() )
                .addValue( "startsAt", event.getStartsAt() )
                .addValue( "endsAt", event.getEndsAt() );

        jdbc.update( sql, params );
    }

    private static final RowMapper< Event > EVENT_ROW_MAPPER = ( rs, rowNum ) -> {
        Event e = new Event();
        e.setId( rs.getLong( "id" ) );
        e.setPublicId( rs.getObject( "public_id", UUID.class ) );
        e.setOrganizerId( rs.getLong( "organizer_id" ) );
        e.setVenueId( rs.getLong( "venue_id" ) );
        e.setTitle( rs.getString( "title" ) );
        e.setDescription( rs.getString( "description" ) );
        e.setStartsAt( rs.getObject( "starts_at", OffsetDateTime.class ) );
        e.setEndsAt( rs.getObject( "ends_at", OffsetDateTime.class ) );
        e.setStatus( EventStatus.valueOf( rs.getString( "status" ) ) );
        e.setCreatedAt( rs.getObject( "created_at", OffsetDateTime.class ) );
        e.setUpdatedAt( rs.getObject( "updated_at", OffsetDateTime.class ) );
        return e;
    };
}
