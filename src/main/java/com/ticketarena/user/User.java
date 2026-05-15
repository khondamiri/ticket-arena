package com.ticketarena.user;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class User {
    private Long id;
    private UUID publicId;
    private String email;
    private String passwordHash;
    private String fullName;
    private UserRole role;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    public static User create( String email, String passwordHash, String fullName, UserRole role ) {
        User user = new User();
        user.setPublicId( UUID.randomUUID() );
        user.setEmail( email );
        user.setPasswordHash( passwordHash );
        user.setFullName( fullName );
        user.setRole( role );
        return user;
    }
}