package dev.ashu.userservice.mapper;

import dev.ashu.userservice.dto.UserDto;
import dev.ashu.userservice.model.User;

public class UserEntityDTOMapper {
    public static UserDto getUserDTOFromUserEntity(User user) {
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());
        return userDto;
    }
}
