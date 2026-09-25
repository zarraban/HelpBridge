package com.example.help_bridge.notification.repository;

import com.example.help_bridge.notification.entity.MailingTask;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryMailingTaskRepository implements MailingTaskRepository {

    private final Map<Long, MailingTask> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<MailingTask> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<MailingTask> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public MailingTask save(MailingTask mailingTask) {
        mailingTask.setId(idGenerator.getAndIncrement());
        storage.put(mailingTask.getId(), mailingTask);
        return mailingTask;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}
