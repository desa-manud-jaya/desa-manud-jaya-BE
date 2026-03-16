package com.example.manud_jaya.model.inbound.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBusinessRequest {

    private String name;
    private String description;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;

}
