package com.example.help_bridge.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mailing_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MailingTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fundraiser_id")
    private Long fundraiserId;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MailingRecipient> recipients = new ArrayList<>();

    @Column(nullable = false)
    private String subject;

    @Column(name = "message_body", columnDefinition = "TEXT", nullable = false)
    private String messageBody;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MailingStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    public void addRecipient(MailingRecipient recipient) {
        recipients.add(recipient);
        recipient.setTask(this);
    }
}