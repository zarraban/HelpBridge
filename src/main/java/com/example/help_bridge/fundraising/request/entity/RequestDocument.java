package com.example.help_bridge.fundraising.request.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "request_documents")
public class RequestDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    protected RequestDocument() {}

    public RequestDocument(String fileName, String fileUrl) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public void update(String fileName, String fileUrl) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getFileUrl() { return fileUrl; }
    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestDocument that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}