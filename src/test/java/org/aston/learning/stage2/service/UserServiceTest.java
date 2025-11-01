package org.aston.learning.stage2.service;

import org.aston.learning.stage2.dto.UserRequest;
import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventPublisher userEventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_WithMinAge_ShouldWork() {
        // Given
        UserRequest userRequest = new UserRequest("Young User", "young@example.com", 0);
        User savedUser = new User("Young User", "young@example.com", 0);
        savedUser.setId(1L);

        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When & Then
        assertThatNoException().isThrownBy(() -> userService.createUser(userRequest));

        // Verify that event was published
        verify(userEventPublisher).publishUserCreated(userRequest.getEmail(), userRequest.getName());
    }

    @Test
    void createUser_WithMaxReasonableAge_ShouldWork() {
        // Given
        UserRequest userRequest = new UserRequest("Old User", "old@example.com", 150);
        User savedUser = new User("Old User", "old@example.com", 150);
        savedUser.setId(1L);

        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When & Then
        assertThatNoException().isThrownBy(() -> userService.createUser(userRequest));

        // Verify that event was published
        verify(userEventPublisher).publishUserCreated(userRequest.getEmail(), userRequest.getName());
    }

    @Test
    void updateUser_WithSameEmail_ShouldWork() {
        // Given
        Long userId = 1L;
        String sameEmail = "same@example.com";
        UserRequest userRequest = new UserRequest("Updated Name", sameEmail, 35);

        User existingUser = new User("Original Name", sameEmail, 30);
        existingUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot(sameEmail, userId)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When & Then
        assertThatNoException().isThrownBy(() -> userService.updateUser(userId, userRequest));
    }

    @Test
    void deleteUser_WithMultipleCalls_ShouldHandleGracefully() {
        // Given
        Long userId = 1L;
        User userToDelete = new User("Test User", "test@example.com", 25);
        userToDelete.setId(userId);

        // First call setup - user exists
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
        doNothing().when(userRepository).deleteById(userId);

        // When - First call (should succeed)
        userService.deleteUser(userId);

        // Then - Second call setup - user doesn't exist
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then - Second call should fail
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: " + userId);

        // Verify interactions
        verify(userRepository, times(2)).findById(userId);
        verify(userRepository, times(1)).deleteById(userId);
        verify(userEventPublisher, times(1)).publishUserDeleted("test@example.com", "Test User");
    }

    @Test
    void updateUser_NonExistingUser_ShouldThrowException() {
        // Given
        Long userId = 999L;
        UserRequest userRequest = new UserRequest("New Name", "new@example.com", 30);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, userRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }
}