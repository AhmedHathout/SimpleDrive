package com.ahmedhathout.SimpleDrive.entities.files;

import com.ahmedhathout.SimpleDrive.entities.User;
import com.ahmedhathout.SimpleDrive.listeners.Shareable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.utility.RandomString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class UserFile implements Shareable {

    @Id
    private Key id;

    @NotNull
    @DBRef(lazy = true)
    private User owner;

    @NotNull
    @DBRef
    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Set<User> usersWithAccess = new HashSet<>();

    @Builder.Default
    private boolean shareableWithLink = false;

    @Setter(AccessLevel.NONE)
    @Builder.Default
    private String shareableLink = new RandomString(64).nextString();

    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Instant dateCreated = Instant.now();

    @NotNull
    @Builder.Default
    private Instant lastModificationDate = Instant.now();

    @Data
    @Builder
    public static class Key implements Serializable {
        private String fileName;
        private DirectoryFile parentDirectory;
    }
}
