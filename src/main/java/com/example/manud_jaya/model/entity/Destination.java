package com.example.manud_jaya.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "destinations")
public class Destination {

    @Id
    private String id;
    private String businessId;
    private String name;
    private String type;
    private String subTitle;
    private String description;
    private String ecoValue;
    private List<String> activities;
    private String approvalStatus;
    private List<String> images;
    private LocalDateTime createdAt;
}
