package com.example.help_bridge.notification.repository;
import com.example.help_bridge.notification.entity.MailingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailingTaskJpaRepository extends JpaRepository<MailingTask, Long> {
}