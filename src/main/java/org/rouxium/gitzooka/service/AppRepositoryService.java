package org.rouxium.gitzooka.service;

import org.rouxium.gitzooka.config.CustomConfigSessionFactory;
import org.rouxium.gitzooka.domain.AppRepository;
import org.rouxium.gitzooka.repository.AppRepositoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppRepositoryService {

    @Autowired
    AppRepositoryRepository appRepositoryRepository;

    @Cacheable("repos")
    public List<AppRepository> getAppRepositories() {
        return appRepositoryRepository.findAll();
    }

    public AppRepository addAppRepository(AppRepository appRepository) {
        return appRepositoryRepository.save(appRepository);
    }

    @Cacheable("repo")
    public AppRepository getAppRepository(String id) {
        return appRepositoryRepository.findOne(id);
    }

    public CustomConfigSessionFactory sessionFactoryFromAppRepository(AppRepository appRepository) {
        CustomConfigSessionFactory sessionFactory = new CustomConfigSessionFactory();
        sessionFactory.setUsername(appRepository.getUsername());
        sessionFactory.setPassword(appRepository.getPassword());
        sessionFactory.setKnownHostsFile(appRepository.getKnownHostsFile());
        sessionFactory.setPrivateKeyFile(appRepository.getPrivateKeyFile());
        sessionFactory.setPrivateKeyPassphrase(appRepository.getPrivateKeyPassphrase());
        return sessionFactory;
    }

}
