package com.ticketarena.booking;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class TicketRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public TicketRepository( NamedParameterJdbcTemplate jdbc ) {
        this.jdbc = jdbc;
    }

    public void saveAll( List< Ticket > tickets ) {
        String sql = """
                INSERT INTO tickets (public_id, booking_id, event_seat_id, price_paid, seat_label)
                VALUES (:publicId, :bookingId, :eventSeatId, :pricePaid, :seatLabel)
                """;

        SqlParameterSource[] batch = new SqlParameterSource[ tickets.size() ];

        for ( int i = 0; i < tickets.size(); i++ ) {
            Ticket t = tickets.get( i );
            batch[ i ] = new MapSqlParameterSource()
                    .addValue( "publicId", t.getPublicId() )
                    .addValue( "bookingId", t.getBookingId() )
                    .addValue( "eventSeatId", t.getEventSeatId() )
                    .addValue( "pricePaid", t.getPricePaid() )
                    .addValue( "seatLabel", t.getSeatLabel() );
        }

        jdbc.batchUpdate( sql, batch );
    }

    public Ticket save( Ticket t ) {
        String sql = """
                INSERT INTO tickets (public_id, booking_id, event_seat_id, price_paid, seat_label)
                VALUES (:publicId, :bookingId, :eventSeatId, :pricePaid, :seatLabel)
                """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "publicId", t.getPublicId() )
                .addValue( "bookingId", t.getBookingId() )
                .addValue( "eventSeatId", t.getEventSeatId() )
                .addValue( "pricePaid", t.getPricePaid() )
                .addValue( "seatLabel", t.getSeatLabel() );

        jdbc.update( sql, params, keyHolder, new String[]{ "id" } );

        t.setId( Objects.requireNonNull( keyHolder.getKey() ).longValue() );

        return t;
    }

    public List< Ticket > findByBookingId( Long bookingId ) {
        String sql = """
                SELECT *
                FROM tickets
                WHERE booking_id = :bookingId AND
                      deleted_at IS NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "bookingId", bookingId );

        return jdbc.query( sql, params, TICKETS_ROW_MAPPER );
    }

    private final RowMapper< Ticket > TICKETS_ROW_MAPPER = ( rs, rowNum ) -> {
        Ticket t = new Ticket();
        t.setId( rs.getLong( "id" ) );
        t.setPublicId( rs.getObject( "public_id", UUID.class ) );
        t.setBookingId( rs.getLong( "booking_id" ) );
        t.setEventSeatId( rs.getLong( "event_seat_id" ) );
        t.setPricePaid( rs.getBigDecimal( "price_paid" ) );
        t.setSeatLabel( rs.getString( "seat_label" ) );
        t.setCreatedAt( rs.getObject( "created_at", OffsetDateTime.class ) );
        return t;
    };
}
