package com.example.manud_jaya.service;

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

    public List<VendorBusinessResponse> getApprovedBusinesses() {

        List<User> vendors =
                userRepository.findByRoleAndVendorProfileApprovalStatus(
                        "VENDOR",
                        "APPROVED"
                );

        return vendors.stream()
                .map(user -> VendorBusinessResponse.builder()
                        .userId(user.getId())
                        .vendorName(user.getVendorProfile().getVendorName())
                        .phone(user.getVendorProfile().getPhone())
                        .address(user.getVendorProfile().getAddress())
                        .build())
                .toList();
    }

    public VendorBusinessDetailResponse getBusinessById(String vendorId) {

        User vendor = userRepository
                .findByIdAndRoleAndVendorProfileApprovalStatus(
                        vendorId,
                        "VENDOR",
                        "APPROVED"
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Business not found"
                ));

        VendorProfile profile = vendor.getVendorProfile();

        return VendorBusinessDetailResponse.builder()
                .userId(vendor.getId())
                .vendorName(profile.getVendorName())
                .phone(profile.getPhone())
                .address(profile.getAddress())
                .ktpNumber(profile.getKtpNumber())
                .approvalStatus(profile.getApprovalStatus())
                .approvedAt(profile.getApprovedAt())
                .build();
    }

    public Business createBusiness(CreateBusinessRequest request, String username) {

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        Business business = Business.builder()
                .vendorId(vendor.getId())
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .approvalStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        return businessRepository.save(business);
    }

    public List<Business> getVendorBusinesses(String username) {

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        return businessRepository.findByVendorId(vendor.getId());
    }

    public Business getBusinessDetail(String businessId, String username) {

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (!business.getVendorId().equals(vendor.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        return business;
    }
}
