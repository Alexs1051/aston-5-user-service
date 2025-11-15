package org.aston.learning.stage2.repository;

import org.aston.learning.stage2.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=optional:file:.env[.properties]",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user1 = new User("John Doe", "john@example.com", 30);
        user2 = new User("Jane Smith", "jane@example.com", 25);

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
    }

    @Test
    @DisplayName("Should find all users")
    void findAll_ShouldReturnAllUsers() {
        // When
        List<User> users = userRepository.findAll();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    @DisplayName("Should find user by ID")
    void findById_ExistingUser_ShouldReturnUser() {
        // When
        Optional<User> foundUser = userRepository.findById(user1.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John Doe");
        assertThat(foundUser.get().getEmail()).isEqualTo("john@example.com");
        assertThat(foundUser.get().getAge()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void findById_NonExistingUser_ShouldReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findById(999L);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should save user successfully")
    void save_ValidUser_ShouldSaveUser() {
        // Given
        User newUser = new User("New User", "new@example.com", 35);

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("New User");
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.getAge()).isEqualTo(35);
        assertThat(savedUser.getCreatedAt()).isNotNull();

        // Verify in database
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
    }

    @Test
    @DisplayName("Should update user successfully")
    void save_ExistingUser_ShouldUpdateUser() {
        // Given
        user1.setName("Updated Name");
        user1.setEmail("updated@example.com");
        user1.setAge(40);

        // When
        User updatedUser = userRepository.save(user1);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getAge()).isEqualTo(40);

        // Verify in database
        Optional<User> foundUser = userRepository.findById(user1.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("Should delete user successfully")
    void delete_ExistingUser_ShouldDeleteUser() {
        // When
        userRepository.deleteById(user1.getId());

        // Then
        Optional<User> foundUser = userRepository.findById(user1.getId());
        assertThat(foundUser).isEmpty();

        // Verify other user still exists
        List<User> remainingUsers = userRepository.findAll();
        assertThat(remainingUsers).hasSize(1);
        assertThat(remainingUsers.get(0).getName()).isEqualTo("Jane Smith");
    }

    @Test
    @DisplayName("Should find user by email")
    void findByEmail_ExistingUser_ShouldReturnUser() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("john@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void findByEmail_NonExistingUser_ShouldReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void existsByEmail_ExistingEmail_ShouldReturnTrue() {
        // When
        boolean exists = userRepository.existsByEmail("john@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-existing email")
    void existsByEmail_NonExistingEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should check if user exists by email excluding specific ID")
    void existsByEmailAndIdNot_ShouldWorkCorrectly() {
        // Given - user with email exists but with different ID
        boolean exists = userRepository.existsByEmailAndIdNot("jane@example.com", user1.getId());

        // Then
        assertThat(exists).isTrue();

        // When - check with same ID (should return false)
        boolean existsSameId = userRepository.existsByEmailAndIdNot("jane@example.com", user2.getId());

        // Then
        assertThat(existsSameId).isFalse();
    }

    @Test
    @DisplayName("Should set createdAt automatically on save")
    void save_User_ShouldSetCreatedAtAutomatically() {
        // Given
        User newUser = new User("Test User", "test@example.com", 30);

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should maintain createdAt on update")
    void update_User_ShouldNotChangeCreatedAt() {
        // Given
        LocalDateTime originalCreatedAt = user1.getCreatedAt();
        user1.setName("Updated Name");

        // When
        User updatedUser = userRepository.save(user1);

        // Then
        assertThat(updatedUser.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    @DisplayName("Should handle unique constraint on email")
    void save_DuplicateEmail_ShouldThrowException() {
        // Given
        User duplicateUser = new User("Another John", "john@example.com", 40);

        // When & Then
        assertThatThrownBy(() -> userRepository.save(duplicateUser))
                .isInstanceOf(Exception.class); // Spring Data throws DataIntegrityViolationException
    }
}