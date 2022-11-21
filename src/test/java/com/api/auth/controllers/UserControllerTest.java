package com.api.auth.controllers;

import com.api.auth.models.UserModel;
import com.api.auth.services.UserMessageService;
import com.api.auth.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;


    @MockBean
    UserMessageService userMessageService;

    @Test
    public void userTestGetUser() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserModel userModel = new UserModel();
        userModel.setId(uuid);
        userModel.setFirstName("Foo");
        userModel.setLastName("Bar");
        userModel.setPassword("Teste123");
        userModel.setEmail("foo.bar@test.com");
        userModel.setPassword("Teste12");
        when(userService.findById(uuid)).thenReturn(Optional.of(userModel));
        mockMvc.perform(get("/user/" + uuid.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(userModel)));
    }

    @Test
    public void deleteTestUser() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserModel userModel = new UserModel();
        userModel.setId(uuid);
        userModel.setFirstName("Foo");
        userModel.setLastName("Bar");
        userModel.setPassword("Teste123");
        userModel.setEmail("foo.bar@test.com");
        userModel.setPassword("Teste12");
        when(userService.findById(uuid)).thenReturn(Optional.of(userModel));

        Mockito.doNothing().when(userMessageService).sendMessage(objectMapper.writeValueAsString(userModel));
        when(userService.save(userModel)).thenReturn(userModel);
        mockMvc.perform(delete("/user/" + uuid))
                .andExpect(status().isOk());
    }
}