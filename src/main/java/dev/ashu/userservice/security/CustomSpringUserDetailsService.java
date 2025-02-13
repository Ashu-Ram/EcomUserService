package dev.ashu.userservice.security;

import dev.ashu.userservice.model.User;
import dev.ashu.userservice.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.OptionalInt;

@Service
public class CustomSpringUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomSpringUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username);
        if(user.isEmpty()) {
            throw new UsernameNotFoundException("User details with given username is not found");
        }
        User savedUser = user.get();
        return new CustomSpringUserDetails(savedUser);
    }
}
