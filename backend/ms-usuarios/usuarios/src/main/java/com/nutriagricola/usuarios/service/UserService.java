package com.nutriagricola.usuarios.service;

import com.nutriagricola.usuarios.dto.UserResponse;
import com.nutriagricola.usuarios.exception.ResourceNotFoundException;
import com.nutriagricola.usuarios.mapper.UserMapper;
import com.nutriagricola.usuarios.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll(){
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id){
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("usuario no encontrado por ID " + id));
    }

    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email){
        return userRepository.findByEmailIgnoreCase(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado por email " + email));
    }

    public void delete(UUID id){
        if (!userRepository.existsById(id)){
            throw new ResourceNotFoundException("usuario no encontrado por ID " + id);
        }
        userRepository.deleteById(id);
    }

}
