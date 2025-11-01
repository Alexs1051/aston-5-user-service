package org.aston.learning.stage2.event;

import java.time.LocalDateTime;

public class UserEvent {
    private String eventType; // CREATED, DELETED
    private String email;
    private String userName;
    private LocalDateTime timestamp;

    // Constructs
    public UserEvent() {}

    public UserEvent(String eventType, String email, String userName) {
        this.eventType = eventType;
        this.email = email;
        this.userName = userName;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}