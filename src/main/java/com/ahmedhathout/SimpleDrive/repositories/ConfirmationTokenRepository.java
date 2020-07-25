package com.ahmedhathout.SimpleDrive.repositories;

import com.ahmedhathout.SimpleDrive.entities.ConfirmationToken;
import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmationTokenRepository extends CrudRepository<ConfirmationToken, ObjectId> {
    Optional<ConfirmationToken> findByConfirmationToken(String confirmationToken);
    Optional<ConfirmationToken> findByUserEmail(String email);
    void deleteByUserEmail(String email);
}
