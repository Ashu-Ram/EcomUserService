package dev.ashu.userservice.service;

import dev.ashu.userservice.model.Role;
import dev.ashu.userservice.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role createRole(String name){
        Role role= new Role();
        role.setRole(name);

        return roleRepository.save(role);
    }
}
