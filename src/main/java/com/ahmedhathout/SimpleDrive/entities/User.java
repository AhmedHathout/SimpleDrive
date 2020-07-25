package com.ahmedhathout.SimpleDrive.entities;

import com.ahmedhathout.SimpleDrive.listeners.UserFileListener;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.*;

// TODO Delete UserFileListener or make use of it
@Log4j2
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
@Data
public class User implements UserDetails, UserFileListener {

    @Id
    @Indexed
    @Email
    @NonNull
    @NotEmpty
    private String email;

    @Size(min = 8, max = 250)
    private String password;

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Set<GrantedAuthority> authorities = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Instant dateCreated = Instant.now();

    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Instant lastLoginDate = Instant.now();

    @DBRef(lazy = true)
    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Set<UserFile> ownedFiles = new HashSet<>();

    @DBRef
    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Set<UserFile> filesWithAccess = new HashSet<>();

    @Builder.Default
    boolean accountNonExpired = true;

    @Builder.Default
    boolean accountNonLocked = true;

    @Builder.Default
    boolean credentialsNonExpired = true;

    @Builder.Default
    boolean enabled = false;

    // Make sure that any file that was deleted is removed from the list of owned files
    public Set<UserFile> getOwnedFiles() {
        this.ownedFiles.removeIf(Objects::isNull);
        log.debug("getting owned files");
        return this.ownedFiles;
    }

    public Set<UserFile> getFilesWithAccess() {
        this.filesWithAccess.removeIf(Objects::isNull);
        return this.filesWithAccess;
    }

    @Override
    public void onUserFileCreated(UserFile userFile) {
        this.ownedFiles.add(userFile);
    }

    @Override
    public void onUserFileDeleted(UserFile userFile) {
        this.ownedFiles.remove(userFile);
        this.filesWithAccess.remove(userFile);
    }

    @Override
    public String getUsername() {
        return email;
    }
}
