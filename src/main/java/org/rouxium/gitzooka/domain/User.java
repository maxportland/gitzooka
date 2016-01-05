package org.rouxium.gitzooka.domain;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
public class User {
    @Id private String id;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String password;
    private Location location;
}
