package com.ahmedhathout.SimpleDrive.exceptions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
public class NoSuchFileException extends IllegalFileAccessException {

    public NoSuchFileException(String shareableLink) {
        super("No file with the link: " + shareableLink, shareableLink);
    }
}
