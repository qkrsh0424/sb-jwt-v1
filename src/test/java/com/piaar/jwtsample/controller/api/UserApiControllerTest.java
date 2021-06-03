package com.piaar.jwtsample.controller.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.piaar.jwtsample.model.user.dto.SignupReqDto;
import com.piaar.jwtsample.model.user.dto.UserDefDto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class UserApiControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    public void searchOneTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/one")).andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createOneTest() throws Exception {
        SignupReqDto signupReqDto = new SignupReqDto();
        signupReqDto.setUsername("user222");
        signupReqDto.setPassword("user222!");

        ObjectMapper om = new ObjectMapper();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/one")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(signupReqDto)))
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void loginTest() throws Exception{
        UserDefDto userDefDto = new UserDefDto();
        userDefDto.setUsername("user222");
        userDefDto.setPassword("user222!");

        ObjectMapper om = new ObjectMapper();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(userDefDto)))
            .andDo(MockMvcResultHandlers.print());
    }
}
