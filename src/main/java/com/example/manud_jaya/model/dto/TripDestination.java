package com.example.manud_jaya.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDestination {

    private String destinationId;
    private String destinationName;
    private LocalDate visitDate;
    private Double price;
}
