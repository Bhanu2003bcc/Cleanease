package com.mrer.cleanease.config;

import com.mrer.cleanease.ExceptionHandler.UserNotFoundException;
import com.mrer.cleanease.entity.User;
import com.mrer.cleanease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
         User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

}
