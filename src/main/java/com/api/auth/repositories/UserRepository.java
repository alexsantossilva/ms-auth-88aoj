package com.api.auth.repositories;

import com.api.auth.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserModel, UUID> {

    boolean existsByEmail(String email);

    Optional<UserModel> findByEmail(String email);
}
