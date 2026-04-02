package com.example.manud_jaya.model.inbound.response;

import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.model.entity.Package;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovedListingsResponse {

    private List<Package> packages;
    private List<Destination> destinations;
    private int page;
    private int size;
    private long totalPackages;
    private long totalDestinations;
}
