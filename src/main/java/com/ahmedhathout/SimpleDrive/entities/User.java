package com.ahmedhathout.SimpleDrive.entities;

import com.ahmedhathout.SimpleDrive.entities.files.UserFile;
import com.ahmedhathout.SimpleDrive.listeners.UserFileListener;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
@Data
public class User implements UserDetails, UserFileListener {

    // TODO Delete
//    @Transient
//    public static final String SEQUENCE_NAME = "users_sequence";

    @Id
    @Indexed
    @Email
    @NotNull
    private String email;

    @Size(min = 8, max = 250)
    @Getter
    private String password;

    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;

    @Id
    private ObjectId Id;

    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Set<GrantedAuthority> authorities = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Instant dateCreated = Instant.now();

    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Instant lastLoginDate = Instant.now();

    @DBRef
    @Setter(AccessLevel.NONE)
    @Builder.Default
    private List<UserFile> ownedFiles = new ArrayList<>();

    @DBRef
    @Setter(AccessLevel.NONE)
    @Builder.Default
    private List<UserFile> filesWithAccess = new ArrayList<>();

    @Builder.Default
    boolean accountNonExpired = true;

    @Builder.Default
    boolean accountNonLocked = true;

    @Builder.Default
    boolean credentialsNonExpired = true;

    @Builder.Default
    boolean enabled = true;

    @Override
    public void onUserFileCreated(UserFile userFile) {
        this.ownedFiles.add(userFile);
    }

    @Override
    public void onUserFileDeleted(UserFile userFile) {
        this.ownedFiles.remove(userFile);
        this.filesWithAccess.remove(userFile);
    }

    // Dummy method. No username
    @Override
    public String getUsername() {
        return null;
    }

}
