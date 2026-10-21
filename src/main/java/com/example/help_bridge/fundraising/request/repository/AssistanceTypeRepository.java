package com.example.help_bridge.fundraising.request.repository;

import com.example.help_bridge.fundraising.request.entity.AssistanceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssistanceTypeRepository extends JpaRepository<AssistanceType, String> {

    List<AssistanceType> findAllByOrderBySortOrderAsc();
}