package com.ticketarena.repository;

import com.ticketarena.common.BaseRepositoryTests;
import com.ticketarena.common.exception.EntityNotFoundException;
import com.ticketarena.user.User;
import com.ticketarena.user.UserRepository;
import com.ticketarena.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class UserRepositoryTests extends BaseRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_shouldPersistUser_andReturnWithGeneratedId() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        User savedUser = userRepository.save( user );

        assertThat( savedUser.getId() )
                .isNotNull();
    }

    @Test
    void save_shouldThrowException_whenEmailAlreadyExists() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        userRepository.save( user );

        assertThatThrownBy( () -> userRepository.save( user ) )
                .isInstanceOf( org.springframework.dao.DuplicateKeyException.class );
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        User savedUser = userRepository.save( user );
        Optional< User > userById = userRepository.findById( savedUser.getId() );

        assertThat( userById ).isPresent();

        assertThat( userById.get().getId() )
                .isEqualTo( savedUser.getId() );
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional< User > emptyUser = userRepository.findById( 1234L );

        assertThat( emptyUser ).isNotPresent();
    }

    @Test
    void findById_shouldReturnEmpty_whenSoftDeleted() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        User savedUser = userRepository.save( user );
        userRepository.softDelete( savedUser.getId() );

        Optional< User > emptyUser = userRepository.findById( savedUser.getId() );

        assertThat( emptyUser ).isNotPresent();
    }

    @Test
    void findByPublicId_shouldReturnUser_whenExists() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        User savedUser = userRepository.save( user );

        Optional< User > userByPublicId = userRepository.findByPublicId( savedUser.getPublicId() );

        assertThat( userByPublicId ).isPresent();

        assertThat( userByPublicId.get().getPublicId() )
                .isEqualTo( savedUser.getPublicId() );
    }

    @Test
    void findByPublicId_shouldReturnEmpty_whenNotExists() {
        Optional< User > emptyUser = userRepository.findByPublicId( UUID.randomUUID() );

        assertThat( emptyUser ).isNotPresent();
    }

    @Test
    void findByEmail_shouldReturnUser_whenExists() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        User savedUser = userRepository.save( user );

        Optional< User > userByEmail = userRepository.findByEmail( savedUser.getEmail() );

        assertThat( userByEmail ).isPresent();

        assertThat( userByEmail.get().getEmail() )
                .isEqualTo( savedUser.getEmail() );
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenNotExists() {
        Optional< User > emptyUser = userRepository.findByEmail( "" );

        assertThat( emptyUser ).isNotPresent();
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenSoftDeleted() {
        String email = "test@email.com";
        User user = User.create( email, "password_hash", "test", UserRole.CUSTOMER );

        User savedUser = userRepository.save( user );
        userRepository.softDelete( savedUser.getId() );

        Optional< User > emptyUser = userRepository.findByEmail( email );

        assertThat( emptyUser ).isNotPresent();
    }

    @Test
    void update_shouldModifyFields_andNotTouchOtherFields() {
        String passwordHash = "password_hash";
        User user = User.create( "test@email.com", passwordHash, "test", UserRole.CUSTOMER );

        userRepository.save( user );

        String updatedEmail = "updated@email.com";
        String updatedFullName = "full name";

        user.setEmail( updatedEmail );
        user.setFullName( updatedFullName );

        userRepository.update( user );

        Optional< User > updatedUser = userRepository.findById( user.getId() );

        assertThat( updatedUser ).isPresent();

        assertThat( updatedUser.get().getEmail() )
                .isEqualTo( updatedEmail );

        assertThat( updatedUser.get().getFullName() )
                .isEqualTo( updatedFullName );

        assertThat( updatedUser.get().getPasswordHash() )
                .isEqualTo( passwordHash );
    }

    @Test
    void update_shouldThrow_whenUserNotFound() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        assertThatThrownBy( () -> userRepository.update( user ) )
                .isInstanceOf( EntityNotFoundException.class );
    }

    @Test
    void softDelete_shouldSetDeletedAt() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        userRepository.save( user );
        userRepository.softDelete( user.getId() );

        RowMapper< User > ROW_MAPPER = ( rs, rowNum) -> {
            User rowUser = new User();
            rowUser.setDeletedAt( rs.getObject( "deleted_at", OffsetDateTime.class ) );
            return rowUser;
        };

        List< User > result = jdbc.query(
                "SELECT deleted_at FROM users WHERE id = ?",
                new Object[]{ user.getId() },
                ROW_MAPPER
        );

        User userToCheck = DataAccessUtils.singleResult( result );

        assertThat( userToCheck.getDeletedAt() ).isNotNull();
    }

    @Test
    void softDelete_shouldThrow_whenAlreadyDeleted() {
        User user = User.create( "test@email.com", "password_hash", "test", UserRole.CUSTOMER );

        userRepository.save( user );
        userRepository.softDelete( user.getId() );

        assertThatThrownBy( () -> userRepository.softDelete( user.getId() ) )
                .isInstanceOf( EntityNotFoundException.class );
    }

    @Test
    void softDelete_shouldThrow_whenUserNotFound() {
        assertThatThrownBy( () -> userRepository.softDelete( 1234L ) )
                .isInstanceOf( EntityNotFoundException.class );
    }
}
