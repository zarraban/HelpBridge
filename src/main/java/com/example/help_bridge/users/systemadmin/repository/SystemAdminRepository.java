package com.example.help_bridge.users.systemadmin.repository;

import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SystemAdminRepository extends JpaRepository<SystemAdmin, Long> {

    boolean existsByEmail(String email);

    Optional<SystemAdmin> findByEmail(String email);

    //@Query("SELECT DISTINCT a FROM SystemAdmin a LEFT JOIN FETCH a.verificationActs")
    //List<SystemAdmin> findAllWithVerificationActs();
}