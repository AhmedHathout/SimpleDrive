package com.ahmedhathout.SimpleDrive.repositories;

import com.ahmedhathout.SimpleDrive.entities.files.DirectoryFile;
import com.ahmedhathout.SimpleDrive.entities.files.UserFile;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DirectoryFileRepository extends ReactiveMongoRepository<DirectoryFile, UserFile.Key> {
}
