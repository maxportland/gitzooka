package org.rouxium.gitzooka.web;

import org.rouxium.gitzooka.domain.User;
import org.rouxium.gitzooka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    List<User> list() {
        return userService.findAll();
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    User save(@RequestBody User user) {
        return userService.save(user);
    }

}
