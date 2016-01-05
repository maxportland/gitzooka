package org.rouxium.gitzooka.service;

import org.rouxium.gitzooka.domain.AppRepository;
import org.rouxium.gitzooka.repository.AppRepositoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppRepositoryService {

    @Autowired
    AppRepositoryRepository appRepositoryRepository;

    public List<AppRepository> getAppRepositories() {
        return appRepositoryRepository.findAll();
    }

    public AppRepository addAppRepository(AppRepository appRepository) {
        return appRepositoryRepository.save(appRepository);
    }

}
