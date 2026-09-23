package com.example.help_bridge.donor.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Donor {
    private Long id;
    private Long fundraiserId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}
