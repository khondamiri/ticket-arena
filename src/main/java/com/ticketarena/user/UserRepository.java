package com.ticketarena.user;

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
import java.util.UUID;

@Repository
public class UserRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public UserRepository( NamedParameterJdbcTemplate jdbcTemplate ) {
        this.jdbc = jdbcTemplate;
    }

    public User save( User user ) {
        String sql = """
                INSERT INTO users (public_id, email, password_hash, full_name, role)
                VALUES (:publicId, :email, :passwordHash, :fullName, :role)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "publicId", user.getPublicId() )
                .addValue( "email", user.getEmail() )
                .addValue( "passwordHash", user.getPasswordHash() )
                .addValue( "fullName", user.getFullName() )
                .addValue( "role", user.getRole().name() );

        jdbc.update( sql, params, keyHolder, new String[]{ "id" } );

        user.setId( Objects.requireNonNull( keyHolder.getKey() ).longValue() );

        return user;
    }

    public Optional< User > findById( Long id ) {
        String sql = """
                SELECT * FROM users
                WHERE id = :id
                AND deleted_at IS NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource( "id", id );

        List< User > users = jdbc.query( sql, params, ROW_MAPPER );
        User user = DataAccessUtils.singleResult( users );

        if ( user != null ) {
            return Optional.of( user );
        } else {
            return Optional.empty();
        }
    }

    public Optional< User > findByPublicId( UUID publicId ) {
        String sql = """
                SELECT * FROM users
                WHERE public_id = :publicId 
                AND deleted_at IS NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource( "publicId", publicId );

        List< User > users = jdbc.query( sql, params, ROW_MAPPER );
        User user = DataAccessUtils.singleResult( users );

        if ( user != null ) {
            return Optional.of( user );
        } else {
            return Optional.empty();
        }
    }

    public Optional< User > findByEmail( String email ) {
        String sql = """
                SELECT * FROM users
                WHERE email = :email
                AND deleted_at IS NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource( "email", email );

        List< User > users = jdbc.query( sql, params, ROW_MAPPER );
        User user = DataAccessUtils.singleResult( users );

        if ( user != null ) {
            return Optional.of( user );
        } else {
            return Optional.empty();
        }
    }

    public void update( User user ) {
        String sql = """
                UPDATE users
                SET email = :email,
                    full_name = :fullName,
                    updated_at = NOW()
                WHERE id = :id
                AND deleted_at IS NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue( "email", user.getEmail() )
                .addValue( "fullName", user.getFullName() )
                .addValue( "id", user.getId() );

        int rowsAffected = jdbc.update( sql, params );

        if ( rowsAffected != 1 ) {
            throw new EntityNotFoundException("User not found with id: " + user.getId());
        }
    }

    public void softDelete( Long id ) {
        String sql = """
                UPDATE users
                SET deleted_at = NOW()
                WHERE id = :id
                AND deleted_at IS NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource( "id", id );

        int rowsAffected = jdbc.update( sql, params );

        if ( rowsAffected != 1 ) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
    }

    private static final RowMapper< User > ROW_MAPPER = ( rs, rowNum ) -> {
        User user = new User();
        user.setId( rs.getLong( "id" ) );
        user.setPublicId( UUID.fromString( rs.getString( "public_id" ) ) );
        user.setEmail( rs.getString( "email" ) );
        user.setPasswordHash( rs.getString( "password_hash" ) );
        user.setFullName( rs.getString( "full_name" ) );
        user.setRole( UserRole.valueOf( rs.getString( "role" ) ) );
        user.setCreatedAt( rs.getObject( "created_at", OffsetDateTime.class ) );
        user.setUpdatedAt( rs.getObject( "updated_at", OffsetDateTime.class ) );
        user.setDeletedAt( rs.getObject( "deleted_at", OffsetDateTime.class ) );
        return user;
    };
}
