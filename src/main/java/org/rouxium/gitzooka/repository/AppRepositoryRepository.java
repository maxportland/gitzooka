package org.rouxium.gitzooka.repository;

import org.rouxium.gitzooka.domain.AppRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppRepositoryRepository extends MongoRepository<AppRepository, String> {
}
