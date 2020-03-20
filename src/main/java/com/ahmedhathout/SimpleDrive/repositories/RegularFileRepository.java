package com.ahmedhathout.SimpleDrive.repositories;

import com.ahmedhathout.SimpleDrive.entities.files.RegularFile;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RegularFileRepository extends ReactiveMongoRepository<RegularFile, RegularFile.Key> {
}
