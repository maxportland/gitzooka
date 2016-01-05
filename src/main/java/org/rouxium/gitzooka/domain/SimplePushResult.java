package org.rouxium.gitzooka.domain;

import lombok.*;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class SimplePushResult {
    private String messages;
}
