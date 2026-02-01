package com.example.ecommerce.User;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for UserService beans.
 * Creates two service instances - one with caching, one without.
 */
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

