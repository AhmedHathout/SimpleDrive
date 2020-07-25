package com.ahmedhathout.SimpleDrive.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
public abstract class IllegalFileAccessException extends IOException {

    @Setter(AccessLevel.NONE)
    private String shareableLink;

    public IllegalFileAccessException(String message, String shareableLink) {
        super(message);
        this.shareableLink = shareableLink;
    }
}
