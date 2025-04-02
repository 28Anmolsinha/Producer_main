package com.backend.demo.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "complaints")
public class Complaint implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id; // UUID from frontend

    private String name;
    private String issueType;
    private String description;

    public Complaint() {}

    public Complaint(String id, String name, String issueType, String description) {
        this.id = id;
        this.name = name;
        this.issueType = issueType;
        this.description = description;
    }

    public String getId() { // Changed return type from Long to String
        return id;
    }

    public void setId(String id) { // Accepting frontend UUID
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Complaint{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", issueType='" + issueType + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
