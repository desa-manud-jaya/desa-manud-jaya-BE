package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicDestinationService {

    private final DestinationRepository destinationRepository;
    private final BusinessRepository businessRepository;

    public List<Destination> getBusinessDestinations(String businessId) {

        Business business = businessRepository
                .findByIdAndApprovalStatus(businessId, "APPROVED")
                .orElseThrow(() -> new RuntimeException("Business not found"));

        return destinationRepository.findByBusinessId(business.getId());
    }

    public Destination getDestinationDetail(String destinationId) {

        return destinationRepository.findById(destinationId)
                .orElseThrow(() -> new RuntimeException("Destination not found"));
    }
}