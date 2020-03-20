package com.ahmedhathout.SimpleDrive.entities.files;

import lombok.*;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.utility.RandomString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@Document("regular_files")
public class RegularFile extends UserFile {

    @NotNull
    private String content;

    public String getExtension() {
        String[] afterSplit = this.getId().getFileName().split(".");
        if (afterSplit.length == 1) {
            return "";
        }

        return afterSplit[afterSplit.length - 1];
    }

    @Override
    public void onSharingEnabled() {

    }

    @Override
    public void onSharingDisabled() {

    }
}