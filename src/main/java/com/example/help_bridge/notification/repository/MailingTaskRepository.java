package com.example.help_bridge.notification.repository;

import com.example.help_bridge.notification.entity.MailingTask;

import java.util.List;
import java.util.Optional;

public interface MailingTaskRepository {
    List<MailingTask> findAll();
    Optional<MailingTask> findById(Long id);
    MailingTask save(MailingTask mailingTask);
    void deleteById(Long id);
}
