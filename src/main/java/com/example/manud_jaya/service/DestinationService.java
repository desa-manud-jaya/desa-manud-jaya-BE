package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.CreateDestinationRequest;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.DestinationRepository;
import com.example.manud_jaya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final BusinessRepository businessRepository;
    private final SupabaseStorageService storageService;
    private final UserRepository userRepository;

    public Destination createDestination(
            String businessId,
            CreateDestinationRequest request,
            List<MultipartFile> images,
            String username
    ) throws IOException {

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (!business.getVendorId().equals(vendor.getId())) {
            throw new RuntimeException("Unauthorized business access");
        }

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile image : images) {

            if (!image.getContentType().startsWith("image/")) {
                throw new RuntimeException("File must be image");
            }

            String url = storageService.uploadFile(image);
            imageUrls.add(url);
        }

        Destination destination = Destination.builder()
                .businessId(businessId)
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .price(request.getPrice())
                .capacity(request.getCapacity())
                .images(imageUrls)
                .createdAt(LocalDateTime.now())
                .build();

        return destinationRepository.save(destination);
    }

    public List<Destination> getBusinessDestinations(String businessId, String username) {

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (!business.getVendorId().equals(vendor.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        return destinationRepository.findByBusinessId(businessId);
    }
}
