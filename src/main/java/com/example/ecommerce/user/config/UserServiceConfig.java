package com.example.ecommerce.user.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ecommerce.user.infrastructure.persistence.user.IUserRepository;
import com.example.ecommerce.user.app.UserService;

@Configuration
public class UserServiceConfig {
    
    @Bean
    @Qualifier("dbUserService")
    public UserService dbUserService(
            @Qualifier("dbUserRepository") IUserRepository repository) {
        return new UserService(repository);
    }
    
    @Bean
    @Qualifier("cachedUserService")
    public UserService cachedUserService(
            @Qualifier("cachedUserRepository") IUserRepository repository) {
        return new UserService(repository);
    }
}

