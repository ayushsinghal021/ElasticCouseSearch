package com.undoschool.elastic.repository;

import com.undoschool.elastic.document.CourseDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseRepository extends ElasticsearchRepository<CourseDocument, String> {

    // Custom query methods can be added here if needed
    Page<CourseDocument> findByCategory(String category, Pageable pageable);

    Page<CourseDocument> findByType(CourseDocument.CourseType type, Pageable pageable);

    Page<CourseDocument> findByNextSessionDateAfter(LocalDateTime date, Pageable pageable);

    Page<CourseDocument> findByMinAgeGreaterThanEqualAndMaxAgeLessThanEqual(
            Integer minAge, Integer maxAge, Pageable pageable);

    Page<CourseDocument> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);
}