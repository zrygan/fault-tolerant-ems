package com.ems.courses;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
}