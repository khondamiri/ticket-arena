package com.ticketarena;

import org.springframework.boot.SpringApplication;

public class TestTicketArenaApplication {

    public static void main( String[] args ) {
        SpringApplication.from( Application::main ).with( TestcontainersConfiguration.class ).run( args );
    }

}
