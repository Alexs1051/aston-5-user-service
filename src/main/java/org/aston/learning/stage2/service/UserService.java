package org.aston.learning.stage2.service;

import org.aston.learning.stage2.dto.UserRequest;
import org.aston.learning.stage2.dto.UserResponse;
import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse createUser(UserRequest userRequest);
    UserResponse updateUser(Long id, UserRequest userRequest);
    void deleteUser(Long id);
}