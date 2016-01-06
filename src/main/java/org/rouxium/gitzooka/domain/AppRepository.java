package org.rouxium.gitzooka.domain;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
@ToString
public class AppRepository {
    @Id
    private String id;
    private String name;
    private String path;
    private String username;
    private String password;
    private String privateKeyFile;
    private String knownHostsFile;
    private String privateKeyPassphrase;
}
