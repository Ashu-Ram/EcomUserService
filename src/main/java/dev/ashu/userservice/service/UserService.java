package dev.ashu.userservice.service;

import dev.ashu.userservice.dto.UserDto;
import dev.ashu.userservice.model.Role;
import dev.ashu.userservice.model.User;
import dev.ashu.userservice.repository.RoleRepository;
import dev.ashu.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
    public UserDto getUserDetails (Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return null;
        }

        return UserDto.from(userOptional.get());
    }

    public UserDto setUserRoles(Long userId , List<Long>rolesIds)
    {
        Optional<User> userOptional = userRepository.findById(userId);
        Set<Role> roles = roleRepository.findAllByIdIn(rolesIds);
        if(userOptional.isEmpty())
        {
            return null;
        }
        User user= userOptional.get();
         user.setRoles(roles);

        User savedUser = userRepository.save(user);

        return UserDto.from(savedUser);
    }


}
