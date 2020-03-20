package com.ahmedhathout.SimpleDrive.entities.files;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Document("directory_files")
@SuperBuilder
public class DirectoryFile extends UserFile{

    @DBRef
    @Builder.Default
    private List<UserFile> children = new ArrayList<>();

    @Override
    public void onSharingEnabled() {

    }

    @Override
    public void onSharingDisabled() {

    }
}

