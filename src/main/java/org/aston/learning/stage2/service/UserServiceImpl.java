package org.aston.learning.stage2.service;

import org.aston.learning.stage2.dto.UserRequest;
import org.aston.learning.stage2.dto.UserResponse;
import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToResponse(user);
    }

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        // Check unique email
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("User with email " + userRequest.getEmail() + " already exists");
        }

        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());

        User savedUser = userRepository.save(user);

        // Send event to Kafka
        userEventPublisher.publishUserCreated(savedUser.getEmail(), savedUser.getName());

        return convertToResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Check unique email (except current user)
        if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
            throw new RuntimeException("User with email " + userRequest.getEmail() + " already exists");
        }

        existingUser.setName(userRequest.getName());
        existingUser.setEmail(userRequest.getEmail());
        existingUser.setAge(userRequest.getAge());

        User updatedUser = userRepository.save(existingUser);
        return convertToResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.deleteById(id);

        // Send event to Kafka
        userEventPublisher.publishUserDeleted(user.getEmail(), user.getName());
    }

    private UserResponse convertToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt()
        );
    }
}