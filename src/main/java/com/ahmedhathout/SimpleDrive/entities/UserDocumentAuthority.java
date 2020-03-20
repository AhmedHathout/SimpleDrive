package com.ahmedhathout.SimpleDrive.entities;

import com.ahmedhathout.SimpleDrive.entities.files.RegularFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDocumentAuthority {
    public enum AuthorityName {
        VIEW, COMMENT, EDIT
    }

    private RegularFile regularFile;
    private AuthorityName authorityName;
}
