package com.ems.courses;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

public interface SectionRepository extends JpaRepository<Section, Long> {

    Sort SECTION_ORDER = Sort.by("id");
    List<Section> findByTerm(String term, Sort sort);
}
