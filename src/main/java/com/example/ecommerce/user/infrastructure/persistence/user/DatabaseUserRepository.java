package com.example.ecommerce.user.infrastructure.persistence.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.user.api.dto.UserDTO;
import com.example.ecommerce.user.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
@Qualifier("dbUserRepository")
@RequiredArgsConstructor
public class DatabaseUserRepository implements IUserRepository {
    
    private final UserTable userTable;
    
    @Override
    @NonNull
    public User create(@NonNull User user) {
        return userTable.save(user);
    }
    
    @Override
    @NonNull
    public List<User> findAll() {
        return userTable.findAll();
    }
    
    @Override
    @NonNull
    public List<UserDTO> findAllDTO() {
        return userTable.findAll().stream()
                .map(this::userToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @NonNull
    public Optional<User> findById(@NonNull Long id) {
        return userTable.findById(id);
    }
    
    @Override
    @NonNull
    public Optional<UserDTO> findByIdDTO(@NonNull Long id) {
        return userTable.findById(id).map(this::userToDTO);
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
        return userTable.save(user);
    }
    
    @Override
    public void delete(@NonNull Long id) {
        userTable.deleteById(id);
    }
    
    private UserDTO userToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
}

