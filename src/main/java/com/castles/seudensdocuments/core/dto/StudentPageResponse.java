package com.castles.seudensdocuments.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentPageResponse{
    private List<StudentResponseWithoutAvatar> content;
    private int currentPage;
    private int totalPages;
    private Long totalItems;
}
