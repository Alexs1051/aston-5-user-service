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
class UserServiceEdgeCasesTest {

    @Mock
    private UserRepository userRepository;

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

        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        // When - First call
        userService.deleteUser(userId);

        // Then - Second call should fail if user doesn't exist
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: " + userId);

        verify(userRepository, times(2)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }
}