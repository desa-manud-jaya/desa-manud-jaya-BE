package com.example.manud_jaya.service;

import com.example.manud_jaya.exception.ResourceNotFoundException;
import com.example.manud_jaya.exception.ValidationException;
import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicDestinationService {

    private final DestinationRepository destinationRepository;
    private final BusinessRepository businessRepository;

    public List<Destination> getAllApprovedDestinations() {
        return destinationRepository.findByApprovalStatus(ApprovalStatus.APPROVED.toString());
    }

    public List<Destination> getAllApprovedDestinations(int page, int size) {
        validatePagination(page, size);
        return destinationRepository.findByApprovalStatus(
                ApprovalStatus.APPROVED.toString(),
                PageRequest.of(page, size)
        );
    }

    public long countAllApprovedDestinations() {
        return destinationRepository.countByApprovalStatus(ApprovalStatus.APPROVED.toString());
    }

    public List<Destination> getBusinessDestinations(String businessId) {

        Business business = businessRepository
                .findByIdAndApprovalStatus(businessId, "APPROVED")
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        return destinationRepository.findByBusinessIdAndApprovalStatus(business.getId(), "APPROVED");
    }

    public Destination getDestinationDetail(String destinationId) {

        return destinationRepository.findByIdAndApprovalStatus(destinationId, "APPROVED")
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found"));
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ValidationException("page must be greater than or equal to 0");
        }

        if (size <= 0) {
            throw new ValidationException("size must be greater than 0");
        }
    }
}
