package org.aston.learning.stage2.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Should create user with constructor and set all fields")
    void createUser_WithConstructor_ShouldSetAllFields() {
        // Given
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 30;

        // When
        User user = new User(name, email, age);

        // Then
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getAge()).isEqualTo(age);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create user with default constructor and null fields")
    void createUser_DefaultConstructor_ShouldHaveNullFields() {
        // When
        User user = new User();

        // Then
        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getAge()).isNull();
        assertThat(user.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should handle null age in constructor")
    void createUser_WithNullAge_ShouldWork() {
        // When
        User user = new User("John", "john@test.com", null);

        // Then
        assertThat(user.getAge()).isNull();
        assertThat(user.getName()).isEqualTo("John");
        assertThat(user.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    @DisplayName("Should update user fields with setters")
    void updateUser_WithSetters_ShouldChangeFields() {
        // Given
        User user = new User("Old Name", "old@test.com", 25);
        LocalDateTime newCreatedAt = LocalDateTime.now().minusDays(1);

        // When
        user.setId(1L);
        user.setName("New Name");
        user.setEmail("new@test.com");
        user.setAge(30);
        user.setCreatedAt(newCreatedAt);

        // Then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("New Name");
        assertThat(user.getEmail()).isEqualTo("new@test.com");
        assertThat(user.getAge()).isEqualTo(30);
        assertThat(user.getCreatedAt()).isEqualTo(newCreatedAt);
    }

    @Test
    @DisplayName("Should set createdAt on pre-persist if null")
    void prePersist_WhenCreatedAtIsNull_ShouldSetCurrentTime() {
        // Given
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);

        // When
        user.onCreate();

        // Then
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should not change createdAt on pre-persist if already set")
    void prePersist_WhenCreatedAtIsSet_ShouldNotChange() {
        // Given
        LocalDateTime fixedTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        User user = new User();
        user.setCreatedAt(fixedTime);

        // When
        user.onCreate();

        // Then
        assertThat(user.getCreatedAt()).isEqualTo(fixedTime);
    }

    @Test
    @DisplayName("Should handle edge cases for age")
    void createUser_WithEdgeCaseAges_ShouldWork() {
        // Test minimum age
        User user1 = new User("User1", "user1@test.com", 0);
        assertThat(user1.getAge()).isEqualTo(0);

        // Test reasonable maximum age
        User user2 = new User("User2", "user2@test.com", 150);
        assertThat(user2.getAge()).isEqualTo(150);
    }

    @Test
    @DisplayName("Should handle email with special characters")
    void createUser_WithSpecialEmail_ShouldWork() {
        // Given
        String email = "user.name+tag@example.co.uk";

        // When
        User user = new User("Test User", email, 30);

        // Then
        assertThat(user.getEmail()).isEqualTo(email);
    }
}