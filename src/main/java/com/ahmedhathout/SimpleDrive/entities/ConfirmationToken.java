package com.ahmedhathout.SimpleDrive.entities;

import lombok.*;
import org.bson.types.ObjectId;
import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
@Data
public class ConfirmationToken {

    private static final int daysTillExpiry = 2;

    @Id
    @Setter(AccessLevel.NONE)
    private ObjectId tokenId;

    @Indexed
    @Builder.Default
    private String confirmationToken = UUID.randomUUID().toString();

    @Setter(AccessLevel.NONE)
    @Builder.Default
    private Instant createdDate = Instant.now();

    @DBRef
    @Unique
    private User user;

    public boolean isExpired() {
        return Duration.between(createdDate, createdDate).toDays() >= 2;
    }
}
