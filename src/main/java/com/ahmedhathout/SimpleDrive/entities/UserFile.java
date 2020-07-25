package com.ahmedhathout.SimpleDrive.entities;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.*;

@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserFile {

    // Todo Do not hardcode shareable link prefix
    private static final String SHAREABLE_LINK_PREFIX = "192.168.1.200:8080/files/get?shareableLink=";

    @Id
    @Setter(AccessLevel.NONE)
    @Builder.Default
    @EqualsAndHashCode.Include
    private String shareableLink = generateRandomShareableLink();

    private String fileName;

    @NonNull
    @DBRef(lazy = true)
    private User owner;

    @Builder.Default
    @DBRef(lazy = true)
    @Setter(AccessLevel.NONE)
    private Set<User> peopleWithAccess = new HashSet<>();

    @NonNull
    private ObjectId gridFsFileId;

    @NonNull
    @Setter(AccessLevel.NONE)
    private long size;

    @NonNull
    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Builder.Default
    private boolean shareableWithLink = false;

    /**
     * Can be different than owner if transferring ownership is allowed
     */
    @Setter(AccessLevel.NONE)
    @DBRef(lazy = true)
    @NonNull
    private User createdBy;

    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Instant dateCreated = Instant.now();

    // Last time this file was modified by any user.
    @Builder.Default
    private Instant lastModificationDate = null;

    // Last time this file was downloaded by any user.
    @Builder.Default
    private Instant lastDownloadDate = null;

    public String getExtension() {
        String[] afterSplit = this.getFileName().split("\\.");
        if (afterSplit.length == 1) {
            return "";
        }

        return afterSplit[afterSplit.length - 1];
    }

    public String getShareableLinkWithPrefix() {
        return SHAREABLE_LINK_PREFIX + this.shareableLink;
    }

    public List<String> addOrRemoveTags(String commaSeparatedTags, AddORRemoveTag addORRemoveTag) {
        List<String> stringTags = Arrays.asList(commaSeparatedTags.trim().split("\\s*,\\s*"));
        List<String> outputTags = new ArrayList<>();

        stringTags
                .forEach(stringTag -> {
                    String tag = stringTag.trim().replaceAll(" +", "_").replace("#", " ");
                    if (tag.isEmpty()) {
                        return;
                    }
                    if (addORRemoveTag.equals(AddORRemoveTag.ADD_TAG)) {
                        tags.add(tag);
                        outputTags.add(tag);
                    }
                    else if (addORRemoveTag.equals(AddORRemoveTag.REMOVE_TAG)) {
                        tags.remove(tag);
                        outputTags.add(tag);
                    }

                    // Other options would mean that something went wrong. Maybe some error should be thrown here or to
                    // just use else without if
                });

        return outputTags;
    }

    public boolean containsKeywords(List<String> keywords) {
        return keywords
                .stream()
                .allMatch(keyword -> this
                        .getFileName()
                        .contains(keyword)
                        || this.getTags()
                        .stream()
                        .anyMatch(keyword::equals));
    }

    private static String generateRandomShareableLink() {
        final char leftLimit = '0';
        final char rightLimit = 'z';
        final int stringLength = 64;

        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                // Filter out character between the nums and the letters.
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(stringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static enum AddORRemoveTag {
        ADD_TAG, REMOVE_TAG
    }
}

