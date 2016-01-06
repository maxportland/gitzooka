package org.rouxium.gitzooka.domain;

import lombok.*;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class Branch {
    private String name;
    private String fullName;
    private String repoId;
}
