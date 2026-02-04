package com.example.ecommerce.user.infrastructure.cache.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.user.domain.User;
import com.example.ecommerce.user.infrastructure.persistence.user.IUserRepository;
import com.example.ecommerce.user.infrastructure.persistence.user.UserTable;

import java.util.List;
import java.util.Optional;


@Repository
@Qualifier("cachedUserRepository")
@RequiredArgsConstructor
public class CachedUserRepository implements IUserRepository {
    
    private final UserTable userTable;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "user:";
    private static final String USERNAME_KEY_PREFIX = "user:username:";
    private static final String ALL_USERS_KEY = "users:all";
    
    
    @Override
    @NonNull
    public User create(@NonNull User user) {
        User saved = userTable.save(user);
        cacheUser(saved);
        invalidateAllUsersCache();
        return saved;
    }
    
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<User> findAll() {
        List<User> cached = (List<User>) redisTemplate.opsForValue().get(ALL_USERS_KEY);
        
        if (cached != null) {
            return cached;
        }
        
        List<User> users = userTable.findAll();
        
        cacheAllUsers(users);
        
        return users;
    }
    
    @Override
    @NonNull
    public Optional<User> findById(@NonNull Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return Optional.of((User) cached);
        }
        
        Optional<User> userOpt = userTable.findById(id);
        
        userOpt.ifPresent(user -> {
            cacheUser(user);
        });
        
        return userOpt;
    }
    
    @Override
    @NonNull
    public Optional<User> findByUsername(@NonNull String username) {
        return userTable.findByUsername(username);
    }
    
    @Override
    public boolean existsById(@NonNull Long id) {
        return userTable.existsById(id);
    }
    
    @Override
    @NonNull
    public User update(@NonNull User user) {
        Optional<User> oldUserOpt = userTable.findById(user.getId());
        oldUserOpt.ifPresent(oldUser -> {
            if (!oldUser.getUsername().equals(user.getUsername())) {
                evictUsernameCache(oldUser.getUsername());
            }
        });
        
        User updated = userTable.save(user);
        cacheUser(updated);
        invalidateAllUsersCache();
        return updated;
    }
    
    @Override
    public void delete(@NonNull Long id) {
        Optional<User> userOpt = userTable.findById(id);
        userOpt.ifPresent(user -> evictUsernameCache(user.getUsername()));
        
        userTable.deleteById(id);
        evictFromCache(id);
        invalidateAllUsersCache();
    }
    
    private void cacheUser(User user) {
        String cacheKey = CACHE_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(cacheKey, user);
    }
    
    private void evictFromCache(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
    }
    
    private void evictUsernameCache(String username) {
        String cacheKey = USERNAME_KEY_PREFIX + username;
        redisTemplate.delete(cacheKey);
    }
    
    private void cacheAllUsers(List<User> users) {
        redisTemplate.opsForValue().set(ALL_USERS_KEY, users);
        users.forEach(user -> {
            cacheUser(user);
        });
    }
    
    private void invalidateAllUsersCache() {
        redisTemplate.delete(ALL_USERS_KEY);
    }
}

