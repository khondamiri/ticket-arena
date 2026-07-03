package com.ticketarena.common.exception;

public class SeatLockedException extends RuntimeException {
    public SeatLockedException( String message ) {
        super( message );
    }
}
