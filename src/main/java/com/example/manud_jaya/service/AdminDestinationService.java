package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDestinationService {

    private final DestinationRepository destinationRepository;

    @Autowired(required = false)
    private ModerationAuditService moderationAuditService;

    public Destination approveDestination(String destinationId) {
        return approveDestination(destinationId, null);
    }

    public Destination approveDestination(String destinationId, String adminId) {

        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new RuntimeException("Destination not found"));

        destination.setApprovalStatus("APPROVED");

        Destination saved = destinationRepository.save(destination);
        if (moderationAuditService != null && adminId != null) {
            moderationAuditService.log("DESTINATION", "APPROVE", adminId, saved.getId(), "Destination approved");
        }

        return saved;
    }

    public Destination rejectDestination(String destinationId) {
        return rejectDestination(destinationId, null);
    }

    public Destination rejectDestination(String destinationId, String adminId) {

        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new RuntimeException("Destination not found"));

        destination.setApprovalStatus("REJECTED");

        Destination saved = destinationRepository.save(destination);
        if (moderationAuditService != null && adminId != null) {
            moderationAuditService.log("DESTINATION", "REJECT", adminId, saved.getId(), "Destination rejected");
        }

        return saved;
    }

    public List<Destination> getPendingDestinations() {
        return destinationRepository.findByApprovalStatus("PENDING");
    }
}
