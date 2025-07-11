package com.undoschool.elastic.dto;
import com.undoschool.elastic.document.CourseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDto {

    private String q; // search keyword
    private Integer minAge;
    private Integer maxAge;
    private String category;
    private CourseDocument.CourseType type;
    private Double minPrice;
    private Double maxPrice;
    private LocalDateTime startDate;
    private String sort; // upcoming, priceAsc, priceDesc

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;
}