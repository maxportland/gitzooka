package org.rouxium.gitzooka.repository;

import org.rouxium.gitzooka.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}