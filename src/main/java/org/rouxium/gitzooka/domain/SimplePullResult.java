package org.rouxium.gitzooka.domain;

import lombok.*;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class SimplePullResult {
    private boolean success;
    private String fetchMessage;
    private boolean mergeSuccess;
    private Map<String, int[][]> conflicts;
}
