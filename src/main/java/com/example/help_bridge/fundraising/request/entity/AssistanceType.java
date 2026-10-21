package com.example.help_bridge.fundraising.request.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assistance_type")
public class AssistanceType {

    @Id
    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected AssistanceType() {
    }

    public AssistanceType(String code, String name, int sortOrder) {
        this.code = code;
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}