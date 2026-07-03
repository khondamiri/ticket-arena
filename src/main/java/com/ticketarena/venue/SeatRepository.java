package com.ticketarena.venue;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class SeatRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public SeatRepository( NamedParameterJdbcTemplate jdbcTemplate ) {
        this.jdbc = jdbcTemplate;
    }

    public Seat save( Seat seat ) {
        String sql = """
                INSERT INTO seats (section_id, row_label, seat_number)
                VALUES (:sectionId, :rowLabel, :seatNumber)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "sectionId", seat.getSectionId() )
                .addValue( "seatNumber", seat.getSeatNumber() )
                .addValue( "rowLabel", seat.getRowLabel() );

        jdbc.update( sql, params, keyHolder, new String[]{ "id" } );

        seat.setId( Objects.requireNonNull( keyHolder.getKey() ).longValue() );

        return seat;
    }

    public void saveAll( List< Seat > seats ) {
        String sql = """
                INSERT INTO seats (section_id, row_label, seat_number)
                VALUES (:sectionId, :rowLabel, :seatNumber)
                """;

        SqlParameterSource[] params = SqlParameterSourceUtils.createBatch( seats );

        jdbc.batchUpdate( sql, params );
    }

    public List< Seat > findBySectionId( Long sectionId ) {
        String sql = """
                SELECT * FROM seats
                WHERE section_id = :sectionId
                ORDER BY row_label, seat_number
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "sectionId", sectionId );

        return jdbc.query( sql, params, SEAT_ROW_MAPPER );
    }

    public int countBySectionId( Long sectionId ) {
        String sql = """
                SELECT COUNT(*) FROM seats
                WHERE section_id = :sectionId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "sectionId", sectionId );

        Integer result = jdbc.queryForObject( sql, params, Integer.class );

        return result != null ? result : 0;
    }

    public void deleteBySectionId( Long sectionId ) {
        String sql = """
                DELETE FROM seats WHERE section_id = :sectionId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "sectionId", sectionId );

        jdbc.update( sql, params );
    }

    public String findSeatLabelByEventSeatId( Long eventSeatId ) {
        String sql = """
                SELECT
                    concat_ws(
                        ' ',
                        sc.name,
                        st.row_label,
                        st.seat_number
                    )
                FROM event_seats as es
                JOIN seats AS st ON st.id = es.seat_id
                JOIN sections AS sc ON sc.id = st.section_id
                WHERE es.id = :eventSeatId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "eventSeatId", eventSeatId );

        return jdbc.queryForObject( sql, params, String.class );
    }

    private static final RowMapper< Seat > SEAT_ROW_MAPPER = ( rs, rowNum ) -> {
        Seat s = new Seat();
        s.setId( rs.getLong( "id" ) );
        s.setSectionId( rs.getLong( "section_id" ) );
        s.setSeatNumber( rs.getInt( "seat_number" ) );
        s.setRowLabel( rs.getString( "row_label" ) );
        return s;
    };
}
