package com.ticketarena.common;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
public abstract class BaseRepositoryTests {
    static final PostgreSQLContainer postgres;

    static {
        postgres = new PostgreSQLContainer("postgres:16");
        postgres.start();
    }

    @DynamicPropertySource
    static void configure( DynamicPropertyRegistry registry ) {
        registry.add( "spring.datasource.url", postgres::getJdbcUrl );
        registry.add( "spring.datasource.username", postgres::getUsername );
        registry.add( "spring.datasource.password", postgres::getPassword );
    }

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void cleanUp() {
        jdbc.execute( "DELETE FROM payments" );
        jdbc.execute( "DELETE FROM tickets" );
        jdbc.execute( "DELETE FROM event_seats" );
        jdbc.execute( "DELETE FROM bookings" );
        jdbc.execute( "DELETE FROM events" );
        jdbc.execute( "DELETE FROM seats" );
        jdbc.execute( "DELETE FROM sections" );
        jdbc.execute( "DELETE FROM venues" );
        jdbc.execute( "DELETE FROM users" );
    }
}
