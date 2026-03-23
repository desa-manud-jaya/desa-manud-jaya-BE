package com.example.manud_jaya.model.inbound.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDestinationRequest {

    private String name;
    private String type;
    private String subTitle;
    private String description;
    private String ecoValue;
    private List<String> activities;
}
