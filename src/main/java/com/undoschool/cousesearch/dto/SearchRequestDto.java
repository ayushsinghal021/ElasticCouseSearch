package com.undoschool.cousesearch.dto;
import com.undoschool.cousesearch.document.CourseDocument;
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

    private String q;
    private Integer minAge;
    private Integer maxAge;
    private String category;
    private CourseDocument.CourseType type;
    private Double minPrice;
    private Double maxPrice;
    private LocalDateTime startDate;
    private String sort;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;
}