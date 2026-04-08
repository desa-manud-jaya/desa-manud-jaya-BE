package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDestinationService {

    private final DestinationRepository destinationRepository;

    public Destination approveDestination(String destinationId) {

        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new RuntimeException("Destination not found"));

        destination.setApprovalStatus("APPROVED");

        return destinationRepository.save(destination);
    }

    public Destination rejectDestination(String destinationId) {

        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new RuntimeException("Destination not found"));

        destination.setApprovalStatus("REJECTED");

        return destinationRepository.save(destination);
    }

    public List<Destination> getPendingDestinations() {
        return destinationRepository.findByApprovalStatus("PENDING");
    }
}
