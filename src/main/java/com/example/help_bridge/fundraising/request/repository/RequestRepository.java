package com.example.help_bridge.fundraising.request.repository;

import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    @EntityGraph(attributePaths = {"documents", "requester", "fund"})
    List<Request> findByStatus(RequestStatus status);

    @EntityGraph(attributePaths = {"documents", "requester", "fund"})
    List<Request> findByInstitutionNameIgnoreCase(String institutionName);

    boolean existsByApplicationNumber(String applicationNumber);

    long countByStatus(RequestStatus status);

    @Query("SELECT DISTINCT r FROM Request r " +
            "LEFT JOIN FETCH r.documents " +
            "LEFT JOIN FETCH r.requester " +
            "LEFT JOIN FETCH r.fund " +
            "WHERE r.status = :status")
    List<Request> findAllByStatusWithDetails(@Param("status") RequestStatus status);

    @Query("SELECT DISTINCT r FROM Request r " +
            "LEFT JOIN FETCH r.documents " +
            "LEFT JOIN FETCH r.requester " +
            "LEFT JOIN FETCH r.fund")
    List<Request> findAllWithDetails();

    @Query("SELECT r FROM Request r " +
            "LEFT JOIN FETCH r.documents " +
            "LEFT JOIN FETCH r.requester " +
            "LEFT JOIN FETCH r.fund " +
            "WHERE r.id = :id")
    Optional<Request> findByIdWithDetails(@Param("id") Long id);

    // Лише документи: для операцій з документами не потрібні requester і fund
    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.documents WHERE r.id = :id")
    Optional<Request> findByIdWithDocuments(@Param("id") Long id);

    @Query("SELECT DISTINCT r FROM Request r " +
            "LEFT JOIN FETCH r.documents " +
            "LEFT JOIN FETCH r.requester " +
            "LEFT JOIN FETCH r.fund " +
            "WHERE r.requester.id = :userId " +
            "ORDER BY r.createdAt DESC")
    List<Request> findByRequesterIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT DISTINCT r FROM Request r " +
            "LEFT JOIN FETCH r.documents " +
            "LEFT JOIN FETCH r.requester " +
            "LEFT JOIN FETCH r.fund " +
            "WHERE LOWER(r.institutionName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Request> searchByInstitutionWithDetails(@Param("name") String name);
}