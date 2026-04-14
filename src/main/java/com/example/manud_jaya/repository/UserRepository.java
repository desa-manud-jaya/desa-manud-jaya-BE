package com.example.manud_jaya.repository;

import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.model.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRoleAndVendorProfileApprovalStatus(
            String role,
            ApprovalStatus approvalStatus
    );

    List<User> findByRoleAndVendorProfileApprovalStatus(
            String role,
            String approvalStatus
    );

    Optional<User> findByIdAndRoleAndVendorProfileApprovalStatus(
            String id,
            String role,
            String approvalStatus
    );

    List<User> findByRoleAndStatus(String role, String status);

    Optional<User> findByIdAndRole(String id, String role);

    long countByRoleAndStatus(String role, String status);
}
