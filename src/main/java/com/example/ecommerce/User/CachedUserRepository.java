package com.example.ecommerce.User;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.User.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Cached repository implementation for User CRUD operations.
 * Uses optimistic caching strategy - checks cache first, falls back to database.
 * Caches entire User objects with key pattern: user:{userId}
 * Username lookups cached with key pattern: user:username:{username}
 */
@Repository
@Qualifier("cachedUserRepository")
@RequiredArgsConstructor
public class CachedUserRepository implements IUserRepository {
    
    private final UserTable userTable;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "user:";
    private static final String USERNAME_KEY_PREFIX = "user:username:";
    private static final String ALL_USERS_KEY = "users:all";
    
    // -------------------
    // CREATE operations
    // -------------------
    
    @Override
    @NonNull
    public User create(@NonNull User user) {
        User saved = userTable.save(user);
        cacheUser(saved);
        cacheByUsername(saved);
        invalidateAllUsersCache();
        return saved;
    }
    
    // -------------------
    // READ operations
    // -------------------
    
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<User> findAll() {
        List<User> cached = (List<User>) redisTemplate.opsForValue().get(ALL_USERS_KEY);
        
        if (cached != null) {
            return cached;
        }
        
        // Cache miss - fetch from database
        List<User> users = userTable.findAll();
        
        // Cache all users
        cacheAllUsers(users);
        
        return users;
    }
    
    @Override
    @NonNull
    public Optional<User> findById(@NonNull Long id) {
        // Check cache first (optimistic caching)
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return Optional.of((User) cached);
        }
        
        // Cache miss - fetch from database
        Optional<User> userOpt = userTable.findById(id);
        
        // Cache the result if found
        userOpt.ifPresent(user -> {
            cacheUser(user);
            cacheByUsername(user);
        });
        
        return userOpt;
    }
    
    @Override
    @NonNull
    public Optional<User> findByUsername(@NonNull String username) {
        // Check cache first
        String cacheKey = USERNAME_KEY_PREFIX + username;
        Object cachedId = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedId != null) {
            // Get user by cached ID
            Long userId = ((Number) cachedId).longValue();
            return findById(userId);
        }
        
        // Cache miss - fetch from database
        Optional<User> userOpt = userTable.findByUsername(username);
        
        // Cache the result if found
        userOpt.ifPresent(user -> {
            cacheUser(user);
            cacheByUsername(user);
        });
        
        return userOpt;
    }
    
    // -------------------
    // UPDATE operations
    // -------------------
    
    @Override
    @NonNull
    public User update(@NonNull User user) {
        // Get old user to invalidate old username cache if changed
        Optional<User> oldUserOpt = userTable.findById(user.getId());
        oldUserOpt.ifPresent(oldUser -> {
            if (!oldUser.getUsername().equals(user.getUsername())) {
                evictUsernameCache(oldUser.getUsername());
            }
        });
        
        User updated = userTable.save(user);
        cacheUser(updated);
        cacheByUsername(updated);
        invalidateAllUsersCache();
        return updated;
    }
    
    // -------------------
    // DELETE operations
    // -------------------
    
    @Override
    public void delete(@NonNull Long id) {
        // Get user to invalidate username cache
        Optional<User> userOpt = userTable.findById(id);
        userOpt.ifPresent(user -> evictUsernameCache(user.getUsername()));
        
        userTable.deleteById(id);
        evictFromCache(id);
        invalidateAllUsersCache();
    }
    
    @Override
    public boolean existsById(@NonNull Long id) {
        // Check cache first
        String cacheKey = CACHE_KEY_PREFIX + id;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
            return true;
        }
        return userTable.existsById(id);
    }
    
    // -------------------
    // Cache helper methods
    // -------------------
    
    private void cacheUser(User user) {
        String cacheKey = CACHE_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(cacheKey, user);
    }
    
    private void cacheByUsername(User user) {
        String cacheKey = USERNAME_KEY_PREFIX + user.getUsername();
        redisTemplate.opsForValue().set(cacheKey, user.getId());
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
        // Also cache individual users
        users.forEach(user -> {
            cacheUser(user);
            cacheByUsername(user);
        });
    }
    
    private void invalidateAllUsersCache() {
        redisTemplate.delete(ALL_USERS_KEY);
    }
}

