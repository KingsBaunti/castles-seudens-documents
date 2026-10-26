package com.castles.seudensdocuments.core.dto.csv;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CsvUploadResponse {
    private int successCount;
    private List<CsvRowError> errors;

}
