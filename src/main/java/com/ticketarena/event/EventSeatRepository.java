package com.ticketarena.event;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class EventSeatRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public EventSeatRepository( NamedParameterJdbcTemplate jdbcTemplate ) {
        this.jdbc = jdbcTemplate;
    }

    public void saveAll( List< EventSeat > eventSeats ) {
        String sql = """
                INSERT INTO event_seats (event_id, seat_id, status, price, locked_until, locked_by_booking_id, version)
                VALUES (:eventId, :seatId, :status, :price, :lockedUntil, :lockedByBookingId, :version)
                """;

        SqlParameterSource[] batch = new SqlParameterSource[ eventSeats.size() ];

        for ( int i = 0; i < eventSeats.size(); i++ ) {
            EventSeat es = eventSeats.get( i );
            batch[ i ] = new MapSqlParameterSource()
                    .addValue( "eventId", es.getEventId() )
                    .addValue( "seatId", es.getSeatId() )
                    .addValue( "status", es.getStatus().name() )
                    .addValue( "price", es.getPrice() )
                    .addValue( "lockedUntil", es.getLockedUntil() )
                    .addValue( "lockedByBookingId", es.getLockedByBookingId() )
                    .addValue( "version", es.getVersion() );
        }

        jdbc.batchUpdate( sql, batch );
    }

    public List< EventSeat > findByEventId( Long eventId ) {
        String sql = """
                SELECT *
                FROM event_seats
                WHERE event_id = :eventId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource( "eventId", eventId );

        return jdbc.query( sql, params, EVENT_SEAT_ROW_MAPPER );
    }

    public List< EventSeat > findAvailableByEventId( Long eventId ) {
        String sql = """
                SELECT *
                FROM event_seats
                WHERE event_id = :eventId AND
                      status = :status
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "eventId", eventId )
                .addValue( "status", EventSeatStatus.AVAILABLE.name() );

        return jdbc.query( sql, params, EVENT_SEAT_ROW_MAPPER );
    }

    public Optional< EventSeat > findById( Long id ) {
        String sql = """
                SELECT *
                FROM event_seats
                WHERE id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "id", id );


        List< EventSeat > eventSeatList = jdbc.query( sql, params, EVENT_SEAT_ROW_MAPPER );
        EventSeat eventSeat = DataAccessUtils.singleResult( eventSeatList );

        if ( eventSeat != null ) {
            return Optional.of( eventSeat );
        } else {
            return Optional.empty();
        }
    }

    public Optional< EventSeat > findByEventIdAndSeatId( Long eventId, Long seatId ) {
        String sql = """
                SELECT *
                FROM event_seats
                WHERE seat_id = :seatId AND event_id = :eventId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "eventId", eventId )
                .addValue( "seatId", seatId );

        List< EventSeat > eventSeatList = jdbc.query( sql, params, EVENT_SEAT_ROW_MAPPER );
        EventSeat eventSeat = DataAccessUtils.singleResult( eventSeatList );

        if ( eventSeat != null ) {
            return Optional.of( eventSeat );
        } else {
            return Optional.empty();
        }
    }

    public void update( EventSeat eventSeat ) {
        String sql = """
                UPDATE event_seats
                SET
                    status = :status,
                    locked_until = :lockedUntil,
                    locked_by_booking_id = :lockedByBookingId,
                    version = :version
                WHERE
                    id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "id", eventSeat.getId() )
                .addValue( "status", eventSeat.getStatus().name() )
                .addValue( "lockedUntil", eventSeat.getLockedUntil() )
                .addValue( "lockedByBookingId", eventSeat.getLockedByBookingId() )
                .addValue( "version", eventSeat.getVersion() + 1 );

        jdbc.update( sql, params );
    }

    public int countAvailableByEventId( Long eventId ) {
        String sql = """
                SELECT COUNT(*) FROM event_seats
                WHERE event_id = :eventId AND
                      status = :status
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "eventId", eventId )
                .addValue( "status", EventSeatStatus.AVAILABLE.name() );

        Integer result = jdbc.queryForObject( sql, params, Integer.class );

        return result != null ? result : 0;
    }

    private static final RowMapper< EventSeat > EVENT_SEAT_ROW_MAPPER = ( rs, rowNum ) -> {
        EventSeat es = new EventSeat();
        es.setId( rs.getLong( "id" ) );
        es.setEventId( rs.getLong( "event_id" ) );
        es.setSeatId( rs.getLong( "seat_id" ) );
        es.setStatus( EventSeatStatus.valueOf( rs.getString( "status" ) ) );
        es.setPrice( rs.getBigDecimal( "price" ) );
        es.setLockedUntil( rs.getObject( "locked_until", OffsetDateTime.class ) );
        es.setLockedByBookingId( rs.getObject( "locked_by_booking_id", Long.class ) );
        es.setVersion( rs.getInt( "version" ) );
        es.setUpdatedAt( rs.getObject( "updated_at", OffsetDateTime.class ) );
        es.setCreatedAt( rs.getObject( "created_at", OffsetDateTime.class ) );
        return es;
    };
}
