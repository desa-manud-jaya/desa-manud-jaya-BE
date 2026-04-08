package com.example.manud_jaya.service;

import com.example.manud_jaya.exception.ConflictException;
import com.example.manud_jaya.exception.ResourceNotFoundException;
import com.example.manud_jaya.model.dto.VendorProfile;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.CreateBusinessRequest;
import com.example.manud_jaya.model.inbound.response.VendorBusinessDetailResponse;
import com.example.manud_jaya.model.inbound.response.VendorBusinessResponse;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;



    public Business createBusiness(CreateBusinessRequest request, String username) {

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        if (businessRepository.findFirstByVendorId(vendor.getId()).isPresent()) {
            throw new ConflictException("Vendor already has a business");
        }

        Business business = Business.builder()
                .vendorId(vendor.getId())
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .approvalStatus("APPROVED")
                .createdAt(LocalDateTime.now())
                .build();

        Business savedBusiness = businessRepository.save(business);
        VendorProfile vendorProfile = vendor.getVendorProfile();
        if (vendorProfile != null) {
            vendorProfile.setBusinessId(savedBusiness.getId());
        }
        userRepository.save(vendor);

        return savedBusiness;
    }


    public Business getBusinessDetail(String businessId, String username) {

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        if (!business.getVendorId().equals(vendor.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized access");
        }

        return business;
    }
}
