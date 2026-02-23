package com.example.ecommerce.user.infrastructure.cache.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.user.api.dto.UserDTO;
import com.example.ecommerce.user.domain.User;
import com.example.ecommerce.user.infrastructure.persistence.user.IUserRepository;
import com.example.ecommerce.user.infrastructure.persistence.user.UserTable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
    public List<User> findAll() {
        List<User> users = userTable.findAll();
        users.forEach(this::cacheUser);
        return users;
    }
    
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<UserDTO> findAllDTO() {
        List<UserDTO> cached = (List<UserDTO>) redisTemplate.opsForValue().get(ALL_USERS_KEY);
        
        if (cached != null) {
            return cached;
        }
        
        List<User> users = userTable.findAll();
        
        if (!users.isEmpty()) {
            List<UserDTO> dtos = users.stream()
                    .map(this::userToDTO)
                    .collect(Collectors.toList());
            redisTemplate.opsForValue().set(ALL_USERS_KEY, dtos);
            users.forEach(this::cacheUser);
            return dtos;
        }
        
        return List.of();
    }
    
    @Override
    @NonNull
    public Optional<User> findById(@NonNull Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            UserDTO dto = (UserDTO) cached;
            return Optional.of(dtoToUser(dto));
        }
        
        Optional<User> userOpt = userTable.findById(id);
        
        userOpt.ifPresent(this::cacheUser);
        
        return userOpt;
    }
    
    @Override
    @NonNull
    public Optional<UserDTO> findByIdDTO(@NonNull Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return Optional.of((UserDTO) cached);
        }
        
        Optional<User> userOpt = userTable.findById(id);
        
        userOpt.ifPresent(this::cacheUser);
        
        return userOpt.map(this::userToDTO);
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
        UserDTO dto = userToDTO(user);
        redisTemplate.opsForValue().set(cacheKey, dto);
    }
    
    private void evictFromCache(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
    }
    
    private void evictUsernameCache(String username) {
        String cacheKey = USERNAME_KEY_PREFIX + username;
        redisTemplate.delete(cacheKey);
    }
    
    private void invalidateAllUsersCache() {
        redisTemplate.delete(ALL_USERS_KEY);
    }
    
    private UserDTO userToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
    
    private User dtoToUser(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        return user;
    }
}

