package com.castles.seudensdocuments.core.dto.csv;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CsvRowError {
    private int rowNumber;
    private String message;
}
