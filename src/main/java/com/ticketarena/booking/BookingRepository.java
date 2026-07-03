package com.ticketarena.booking;

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
import java.util.UUID;

@Repository
public class BookingRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public BookingRepository( NamedParameterJdbcTemplate jdbc ) {
        this.jdbc = jdbc;
    }

    public Booking save( Booking booking ) {
        String sql = """
                INSERT INTO bookings (public_id, user_id, event_id, status, total_amount, expires_at)
                VALUES (:publicId, :userId, :eventId, :status, :totalAmount, :expiresAt)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "publicId", booking.getPublicId() )
                .addValue( "userId", booking.getUserId() )
                .addValue( "eventId", booking.getEventId() )
                .addValue( "status", booking.getStatus().toString() )
                .addValue( "totalAmount", booking.getTotalAmount() )
                .addValue( "expiresAt", booking.getExpiresAt() );

        jdbc.update( sql, params, keyHolder, new String[]{ "id" } );

        booking.setId( Objects.requireNonNull( keyHolder.getKey() ).longValue() );

        return booking;
    }

    public Optional< Booking > findById( Long id ) {
        String sql = """
                SELECT *
                FROM bookings
                WHERE id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "id", id );

        List< Booking > bookingList = jdbc.query( sql, params, BOOKING_ROW_MAPPER );
        Booking booking = DataAccessUtils.singleResult( bookingList );

        if ( booking != null ) {
            return Optional.of( booking );
        } else {
            return Optional.empty();
        }
    }

    public Optional< Booking > findByPublicId( UUID publicId ) {
        String sql = """
                SELECT *
                FROM bookings
                WHERE public_id = :publicId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "publicId", publicId );

        List< Booking > bookingList = jdbc.query( sql, params, BOOKING_ROW_MAPPER );
        Booking booking = DataAccessUtils.singleResult( bookingList );

        if ( booking != null ) {
            return Optional.of( booking );
        } else {
            return Optional.empty();
        }
    }

    public List< Booking > findByUserId( Long userId ) {
        String sql = """
                SELECT *
                FROM bookings
                WHERE user_id = :userId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "userId", userId );

        return jdbc.query( sql, params, BOOKING_ROW_MAPPER );
    }

    public void updateStatus( Long id, BookingStatus status ) {
        String sql = """
                UPDATE bookings
                SET status = :status
                WHERE id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "status", status.name() )
                .addValue( "id", id );

        jdbc.update( sql, params );
    }

    public List< Booking > findExpired() {
        String sql = """
                SELECT *
                FROM bookings
                WHERE expires_at < NOW() AND
                      status = :status
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "status", BookingStatus.PENDING.name() );

        return jdbc.query( sql, params, BOOKING_ROW_MAPPER );
    }

    private final RowMapper<Booking> BOOKING_ROW_MAPPER = (rs, rowNum) -> {
        Booking b = new Booking();
        b.setId( rs.getLong("id") );
        b.setPublicId( rs.getObject("public_id", UUID.class) );
        b.setUserId( rs.getLong("user_id") );
        b.setEventId( rs.getLong("event_id") );
        b.setStatus( BookingStatus.valueOf( rs.getString("status") ) );
        b.setTotalAmount( rs.getBigDecimal("total_amount" ) );
        b.setExpiresAt( rs.getObject( "expires_at", OffsetDateTime.class ) );
        b.setCreatedAt( rs.getObject( "updated_at", OffsetDateTime.class ) );
        b.setUpdatedAt( rs.getObject( "created_at", OffsetDateTime.class ) );
        return b;
    };
}
