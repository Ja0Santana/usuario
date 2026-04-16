package com.joaopaulo.usuario.infrastructure.controller;

import com.joaopaulo.usuario.infrastructure.exceptions.ConflictException;
import com.joaopaulo.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.joaopaulo.usuario.infrastructure.exceptions.UnauthorizedException;
import com.joaopaulo.usuario.infrastructure.exceptions.BusinessException;
import com.joaopaulo.usuario.infrastructure.exceptions.IllegalArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        public void throwNotFound() { throw new ResourceNotFoundException("Not Found"); }

        @GetMapping("/test/conflict")
        public void throwConflict() { throw new ConflictException("Conflict"); }

        @GetMapping("/test/unauthorized")
        public void throwUnauthorized() { throw new UnauthorizedException("Unauthorized"); }

        @GetMapping("/test/business")
        public void throwBusiness() { throw new BusinessException("Business Error"); }

        @GetMapping("/test/illegal-argument")
        public void throwIllegalArgument() { throw new IllegalArgumentException("Illegal Argument"); }
    }

    @Test
    @DisplayName("Deve retornar 404 para ResourceNotFoundException")
    void deveRetornar404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"));
    }

    @Test
    @DisplayName("Deve retornar 409 para ConflictException")
    void deveRetornar409() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Conflict"));
    }

    @Test
    @DisplayName("Deve retornar 401 para UnauthorizedException")
    void deveRetornar401() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    @DisplayName("Deve retornar 400 para BusinessException")
    void deveRetornar400Business() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Business Error"));
    }

    @Test
    @DisplayName("Deve retornar 400 para IllegalArgumentException")
    void deveRetornar400Illegal() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Illegal Argument"));
    }
}
