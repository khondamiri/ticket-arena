package com.ticketarena.repository;

import com.ticketarena.user.User;
import com.ticketarena.user.UserRepository;
import com.ticketarena.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Testcontainers
@Import( UserRepository.class )
@AutoConfigureTestDatabase( replace = AutoConfigureTestDatabase.Replace.NONE )
public class UserRepositoryTests {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer( "postgres:16" )
                    .withDatabaseName( "ticket_arena_db" )
                    .withUsername( "dbUsername" )
                    .withPassword( "dbPassword" );

    @DynamicPropertySource
    static void configure( DynamicPropertyRegistry registry ) {
        registry.add( "spring.datasource.url", postgres::getJdbcUrl );
        registry.add( "spring.datasource.username", postgres::getUsername );
        registry.add( "spring.datasource.password", postgres::getPassword );
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    public void userCreateTest() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        User savedUser = userRepository.save( user );

        assertThat( savedUser.getId() )
                .isNotNull();
    }

    @Test
    public void userFindByIdTest() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        User savedUser = userRepository.save( user );
        Optional< User > userById = userRepository.findById( savedUser.getId() );

        assertThat( userById )
                .isNotNull()
                .isNotEmpty();

        assertThat( userById.isPresent() )
                .isTrue();

        assertThat( userById.get().getId() )
                .isEqualTo( savedUser.getId() );
    }

    @Test
    public void userFindByPublicIdTest() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        User savedUser = userRepository.save( user );

        Optional< User > userByPublicId = userRepository.findByPublicId( savedUser.getPublicId() );

        assertThat( userByPublicId )
                .isNotNull()
                .isNotEmpty();

        assertThat( userByPublicId.isPresent() )
                .isTrue();

        assertThat( userByPublicId.get().getPublicId() )
                .isEqualTo( savedUser.getPublicId() );
    }

    @Test
    public void userFindByEmailTest() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        User savedUser = userRepository.save( user );

        Optional< User > userByEmail = userRepository.findByEmail( savedUser.getEmail() );

        assertThat( userByEmail )
                .isNotNull()
                .isNotEmpty();

        assertThat( userByEmail.isPresent() )
                .isTrue();

        assertThat( userByEmail.get().getEmail() )
                .isEqualTo( savedUser.getEmail() );
    }

    @Test
    public void userUpdateTest() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        userRepository.save( user );

        String updatedEmail = "updated@email.com";
        String updatedFullName = "full name";

        user.setEmail( updatedEmail );
        user.setFullName( updatedFullName );

        userRepository.update( user );

        Optional< User > updatedUser = userRepository.findById( user.getId() );

        assertThat( updatedUser )
                .isNotNull()
                .isNotEmpty();

        assertThat( updatedUser.isPresent() )
                .isTrue();

        assertThat( updatedUser.get().getEmail() )
                .isEqualTo( updatedEmail );

        assertThat( updatedUser.get().getFullName() )
                .isEqualTo( updatedFullName );
    }

    @Test
    public void userDeleteTest() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        userRepository.save( user );

        userRepository.softDelete( user.getId() );

        Optional< User > deletedUser = userRepository.findById( user.getId() );

        assertThat( deletedUser )
                .isEmpty();

        assertThat( deletedUser.isPresent() )
                .isFalse();
    }

    @Test
    public void userEmptyUserTest() {
        Optional< User > emptyUser = userRepository.findByPublicId( UUID.randomUUID() );

        assertThat( emptyUser )
                .isEmpty();

        assertThat( emptyUser.isPresent() )
                .isFalse();
    }
}
