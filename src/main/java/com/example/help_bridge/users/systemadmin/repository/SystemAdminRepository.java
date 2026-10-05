package com.example.help_bridge.users.systemadmin.repository;

import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemAdminRepository extends JpaRepository<SystemAdmin, Long> {

    boolean existsByEmail(String email);

    Optional<SystemAdmin> findByEmail(String email);

    @Query("SELECT a FROM SystemAdmin a WHERE LOWER(a.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')) ORDER BY a.lastName")
    List<SystemAdmin> searchByLastName(@Param("lastName") String lastName);
}