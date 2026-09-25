package com.example.help_bridge.fundraiser.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Fundraiser {
    private Long id;
    private Long requestId;
    private FundraiserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private List<Evidence> evidences;
}
