package org.rouxium.gitzooka.domain;

import lombok.*;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class CommitFile {
    private String fileName;
    private String appName;
}
