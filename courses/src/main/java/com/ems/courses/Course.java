package com.ems.courses;

import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String code;
    private String title;
    private int units;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getUnits() { return units; }

}
