package com.example.manud_jaya.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItem {

    private String destinationId;

    private String vendorId;

    private String destinationName;

    private Double price;

    private Integer qty;
}
