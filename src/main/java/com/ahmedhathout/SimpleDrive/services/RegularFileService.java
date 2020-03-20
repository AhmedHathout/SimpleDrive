package com.ahmedhathout.SimpleDrive.services;

import com.ahmedhathout.SimpleDrive.entities.User;
import com.ahmedhathout.SimpleDrive.entities.files.DirectoryFile;
import com.ahmedhathout.SimpleDrive.entities.files.RegularFile;
import com.ahmedhathout.SimpleDrive.entities.files.UserFile;
import com.ahmedhathout.SimpleDrive.repositories.DirectoryFileRepository;
import com.ahmedhathout.SimpleDrive.repositories.RegularFileRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class RegularFileService {

    @Autowired
    private RegularFileRepository regularFileRepository;

    @Autowired
    private DirectoryFileRepository directoryFileRepository;

    public Mono<RegularFile> createRegularFile(String name, DirectoryFile parentDirectory, User owner, String content) {
        RegularFile regularFile = RegularFile.builder()
                .id(UserFile.Key.builder()
                        .fileName(name)
                        .parentDirectory(parentDirectory)
                        .build())
                .owner(owner)
                .content(content)
                .build();

        return regularFileRepository.save(regularFile);
    }

    public void deleteRegularFile(RegularFile regularFile) {

        // Update Users
        regularFile.getOwner().getOwnedFiles().remove(regularFile);
        regularFile.getUsersWithAccess().forEach(user -> user.getFilesWithAccess().remove(regularFile));

        // Update Parents
        DirectoryFile parent = regularFile.getId().getParentDirectory();
        parent.getChildren().remove(regularFile);

        // Update DB
        regularFileRepository.delete(regularFile);
        directoryFileRepository.save(parent);
    }

    public Mono<RegularFile> duplicateRegularFile(RegularFile regularFile, String newName) {
        return createRegularFile(newName,
                regularFile.getId().getParentDirectory(),
                regularFile.getOwner(),
                regularFile.getContent());
    }

    public Mono<RegularFile> moveRegularFile(RegularFile regularFile, DirectoryFile newDirectory) {

        // Update old parent
        DirectoryFile oldParent = regularFile.getId().getParentDirectory();
        oldParent.getChildren().remove(regularFile);

        regularFile.getId().setParentDirectory(newDirectory);

        // Update DB
        directoryFileRepository.save(oldParent);
        return regularFileRepository.save(regularFile);
    }
}
