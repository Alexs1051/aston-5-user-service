package org.aston.learning.stage2.service;

import org.aston.learning.stage2.dto.UserRequest;
import org.aston.learning.stage2.dto.UserResponse;
import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class UserServiceIntegrationTest {

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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create user successfully")
    void createUser_ValidRequest_ShouldCreateUser() {
        // Given
        UserRequest userRequest = new UserRequest("John Doe", "john@example.com", 30);

        // When
        UserResponse response = userService.createUser(userRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getAge()).isEqualTo(30);
        assertThat(response.getCreatedAt()).isNotNull();

        // Verify in database
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should get all users")
    void getAllUsers_WithUsers_ShouldReturnAllUsers() {
        // Given
        userService.createUser(new UserRequest("User1", "user1@example.com", 20));
        userService.createUser(new UserRequest("User2", "user2@example.com", 25));

        // When
        List<UserResponse> users = userService.getAllUsers();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserResponse::getName)
                .containsExactlyInAnyOrder("User1", "User2");
    }

    @Test
    @DisplayName("Should get user by ID")
    void getUserById_ExistingUser_ShouldReturnUser() {
        // Given
        UserResponse createdUser = userService.createUser(
                new UserRequest("John Doe", "john@example.com", 30)
        );

        // When
        UserResponse foundUser = userService.getUserById(createdUser.getId());

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getName()).isEqualTo("John Doe");
        assertThat(foundUser.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_ValidRequest_ShouldUpdateUser() {
        // Given
        UserResponse createdUser = userService.createUser(
                new UserRequest("Old Name", "old@example.com", 25)
        );

        UserRequest updateRequest = new UserRequest("New Name", "new@example.com", 30);

        // When
        UserResponse updatedUser = userService.updateUser(createdUser.getId(), updateRequest);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("New Name");
        assertThat(updatedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(updatedUser.getAge()).isEqualTo(30);

        // Verify in database
        UserResponse foundUser = userService.getUserById(createdUser.getId());
        assertThat(foundUser.getName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_ExistingUser_ShouldDeleteUser() {
        // Given
        UserResponse createdUser = userService.createUser(
                new UserRequest("To Delete", "delete@example.com", 40)
        );

        // When
        userService.deleteUser(createdUser.getId());

        // Then
        assertThatThrownBy(() -> userService.getUserById(createdUser.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        // Verify no users in database
        List<UserResponse> users = userService.getAllUsers();
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when creating user with duplicate email")
    void createUser_DuplicateEmail_ShouldThrowException() {
        // Given
        userService.createUser(new UserRequest("User1", "duplicate@example.com", 20));

        UserRequest duplicateRequest = new UserRequest("User2", "duplicate@example.com", 25);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(duplicateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should throw exception when updating non-existing user")
    void updateUser_NonExistingUser_ShouldThrowException() {
        // Given
        UserRequest updateRequest = new UserRequest("New Name", "new@example.com", 30);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(999L, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing user")
    void deleteUser_NonExistingUser_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should handle empty user list")
    void getAllUsers_NoUsers_ShouldReturnEmptyList() {
        // When
        List<UserResponse> users = userService.getAllUsers();

        // Then
        assertThat(users).isEmpty();
    }
}