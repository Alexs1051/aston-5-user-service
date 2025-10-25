package org.aston.learning.stage2.controller;

import org.aston.learning.stage2.dto.UserRequest;
import org.aston.learning.stage2.dto.UserResponse;
import org.aston.learning.stage2.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        // Given
        List<UserResponse> users = Arrays.asList(
                new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now()),
                new UserResponse(2L, "Jane Smith", "jane@example.com", 25, LocalDateTime.now())
        );

        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUser() throws Exception {
        // Given
        Long userId = 1L;
        UserResponse user = new UserResponse(userId, "John Doe", "john@example.com", 30, LocalDateTime.now());

        when(userService.getUserById(userId)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUserById_NonExistingUser_ShouldReturnNotFound() throws Exception {
        // Given
        Long userId = 999L;

        when(userService.getUserById(userId))
                .thenThrow(new RuntimeException("User not found with id: " + userId));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found with id: " + userId));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void createUser_ValidRequest_ShouldReturnCreatedUser() throws Exception {
        // Given
        UserRequest userRequest = new UserRequest("John Doe", "john@example.com", 30);
        UserResponse userResponse = new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());

        when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        verify(userService, times(1)).createUser(any(UserRequest.class));
    }

    @Test
    void createUser_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        UserRequest invalidRequest = new UserRequest("", "invalid-email", -5);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserRequest.class));
    }

    @Test
    void updateUser_ValidRequest_ShouldReturnUpdatedUser() throws Exception {
        // Given
        Long userId = 1L;
        UserRequest userRequest = new UserRequest("John Updated", "john.updated@example.com", 35);
        UserResponse userResponse = new UserResponse(userId, "John Updated", "john.updated@example.com", 35, LocalDateTime.now());

        when(userService.updateUser(eq(userId), any(UserRequest.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"))
                .andExpect(jsonPath("$.age").value(35));

        verify(userService, times(1)).updateUser(eq(userId), any(UserRequest.class));
    }

    @Test
    void deleteUser_ExistingUser_ShouldReturnNoContent() throws Exception {
        // Given
        Long userId = 1L;

        doNothing().when(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void deleteUser_NonExistingUser_ShouldReturnBadRequest() throws Exception {
        // Given
        Long userId = 999L;

        doThrow(new RuntimeException("User not found with id: " + userId))
                .when(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found with id: " + userId));

        verify(userService, times(1)).deleteUser(userId);
    }
}