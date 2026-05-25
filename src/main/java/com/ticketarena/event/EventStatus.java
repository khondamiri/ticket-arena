package com.ticketarena.event;

public enum EventStatus {
    DRAFT,
    PUBLISHED,
    CANCELLED,
    COMPLETED;

    public boolean canTransitionTo(EventStatus next) {
        return switch ( this ) {
            case DRAFT -> next == PUBLISHED || next == CANCELLED;
            case PUBLISHED -> next == CANCELLED || next == COMPLETED;
            case CANCELLED, COMPLETED -> false;
        };
    }
}
