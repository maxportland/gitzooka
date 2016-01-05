package org.rouxium.gitzooka.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Location {
    private String latitude;
    private String longitude;
}
