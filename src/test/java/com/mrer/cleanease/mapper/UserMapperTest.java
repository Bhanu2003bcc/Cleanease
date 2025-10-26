package com.mrer.cleanease.mapper;

import com.mrer.cleanease.dto.request.RegisterUserRequest;
import com.mrer.cleanease.dto.response.UserResponse;
import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void testToEntity() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setFirstname("Bhanu");
        request.setLastname("Pratap Singh");
        request.setEmail("bhanu7507@gmail.com");
        request.setPhoneNumber("7507030770");
        request.setPassword("Bhanu@0987");
        request.setAddress("Kushi-nagar");
        request.setRole(Enums.Role.CUSTOMER);

        User user = mapper.toEntity(request);
        assertNotNull(user);
        assertEquals("Bhanu", user.getFirstname());
        assertEquals("Pratap Singh", user.getLastname());
        assertEquals("bhanu7507@gmail.com", user.getEmail());
        assertEquals("7507030770", user.getPhoneNumber());
        assertEquals("Bhanu@0987", user.getPassword());
        assertEquals("Kushi-nagar", user.getAddress());
        assertEquals(Enums.Role.CUSTOMER, user.getRole());
    }
    @Test
    void testToResponse() {
        User user = new User();
        user.setId(1L);
        user.setFirstname("Jane");
        user.setLastname("Doe");
        user.setEmail("jane@example.com");
        user.setRole(Enums.Role.ADMIN);

        UserMapper userMapper = new UserMapper();
        UserResponse response = userMapper.toResponse(user);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Jane", response.getFirstname());assertEquals("Doe", response.getLastname());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals(Enums.Role.ADMIN, response.getRole()); // ✅ Validate enum
    }


}
