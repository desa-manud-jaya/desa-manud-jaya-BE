package com.example.manud_jaya.model.inbound.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDestinationRequest {

    private String name;
    private String description;
    private String location;
    private Double price;
    private Integer capacity;

}
