package com.ahmedhathout.SimpleDrive.exceptions;

import com.mongodb.lang.Nullable;
import lombok.Getter;
import lombok.NonNull;
import org.bson.types.ObjectId;

import java.io.IOException;

@Getter
public class GridFsFileNotFoundException extends IOException {

    @Nullable
    private ObjectId gridFsFileId;

    @NonNull
    private String fileName;

    public GridFsFileNotFoundException(ObjectId gridFsFileId, String fileName) {
        super("Could not find a GridFsFile with ID: " + gridFsFileId + " which should have the name: " + fileName);
        this.gridFsFileId = gridFsFileId;
        this.fileName = fileName;
    }
}
