package com.ahmedhathout.SimpleDrive.repositories;

import com.ahmedhathout.SimpleDrive.entities.User;
import com.ahmedhathout.SimpleDrive.entities.UserFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFileRepository extends MongoRepository<UserFile, String> {

    List<UserFile> findUserFileByFileNameContains(String name);
    void deleteByFileName(String fileName);
}
