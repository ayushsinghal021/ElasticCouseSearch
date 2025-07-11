package com.undoschool.elastic.dto;
import com.undoschool.elastic.document.CourseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDto {

    private Long total;
    private List<CourseDocument> courses;
    private Integer page;
    private Integer size;
    private Integer totalPages;
}