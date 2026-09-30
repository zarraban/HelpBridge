package com.example.help_bridge.fundraising.request.repository;

import com.example.help_bridge.fundraising.request.entity.RequestDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestDocumentRepository extends JpaRepository<RequestDocument, Long> {
    List<RequestDocument> findByRequestId(Long requestId);
    Optional<RequestDocument> findByIdAndRequestId(Long id, Long requestId);
}