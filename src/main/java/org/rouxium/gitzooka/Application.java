package org.rouxium.gitzooka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBanner((environment, sourceClass, out) ->
                out.println("Version: " + environment.getProperty("app.version")));
        app.run(args);
    }
}
