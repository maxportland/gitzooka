package org.rouxium.gitzooka.service;

import org.rouxium.gitzooka.domain.User;
import org.rouxium.gitzooka.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public User findOne(String id) {
        return userRepository.findOne(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

}
