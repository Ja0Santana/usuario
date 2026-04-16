package com.joaopaulo.usuario.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joaopaulo.usuario.infrastructure.business.UsuarioService;
import com.joaopaulo.usuario.infrastructure.business.VerificationService;
import com.joaopaulo.usuario.infrastructure.business.dto.in.*;
import com.joaopaulo.usuario.infrastructure.clients.services.ViaCepService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsuarioController.class,
            excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private ViaCepService viaCepService;

    @MockitoBean
    private VerificationService verificationService;

    @Test
    @DisplayName("Deve chamar salvarUsuario do service")
    void deveSalvarUsuario() throws Exception {
        UsuarioDTOrequest request = new UsuarioDTOrequest();
        mockMvc.perform(post("/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(usuarioService).salvarUsuario(any());
    }

    @Test
    @DisplayName("Deve chamar autenticarUsuario no login")
    void deveFazerLogin() throws Exception {
        LoginDTORequest request = new LoginDTORequest("email@test.com", "senha", false);
        mockMvc.perform(post("/usuario/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(usuarioService).autenticarUsuario(any());
    }

    @Test
    @DisplayName("Deve buscar usuário autenticado")
    void deveBuscarMe() throws Exception {
        mockMvc.perform(get("/usuario/me"))
                .andExpect(status().isOk());
        verify(usuarioService).buscarUsuarioAutenticado();
    }

    @Test
    @DisplayName("Deve buscar usuário por email")
    void deveBuscarPorEmail() throws Exception {
        mockMvc.perform(get("/usuario")
                .param("email", "test@test.com"))
                .andExpect(status().isOk());
        verify(usuarioService).buscarUsuarioPorEmail("test@test.com");
    }

    @Test
    @DisplayName("Deve deletar usuário por email")
    void deveDeletarPorEmail() throws Exception {
        mockMvc.perform(delete("/usuario/test@test.com"))
                .andExpect(status().isOk());
        verify(usuarioService).deletarUsuarioPorEmail("test@test.com");
    }

    @Test
    @DisplayName("Deve atualizar dados do usuário")
    void deveAtualizarUsuario() throws Exception {
        UsuarioModDTOrequest request = new UsuarioModDTOrequest();
        mockMvc.perform(put("/usuario")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(usuarioService).atualizaDadosUsuario(eq("Bearer token"), any());
    }

    @Test
    @DisplayName("Deve atualizar endereço")
    void deveAtualizarEndereco() throws Exception {
        EnderecoDTOrequest request = new EnderecoDTOrequest();
        mockMvc.perform(put("/usuario/endereco")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(usuarioService).atualizarEndereco(eq(1L), any());
    }

    @Test
    @DisplayName("Deve buscar CEP")
    void deveBuscarCep() throws Exception {
        mockMvc.perform(get("/usuario/endereco/12345678"))
                .andExpect(status().isOk());
        verify(viaCepService).buscarDadosDeEnderecoPorCep("12345678");
    }

    @Test
    @DisplayName("Deve verificar email")
    void deveVerificarEmail() throws Exception {
        VerificationDTORequest request = new VerificationDTORequest("test@test.com", "123456");
        mockMvc.perform(post("/usuario/verificar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(verificationService).validarCodigo("test@test.com", "123456");
    }

    @Test
    @DisplayName("Deve desativar conta")
    void deveDesativarConta() throws Exception {
        VerificationDTORequest request = new VerificationDTORequest("test@test.com", "123456");
        mockMvc.perform(post("/usuario/desativar-conta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(verificationService).validarCodigo("test@test.com", "123456");
        verify(usuarioService).desativarUsuario("test@test.com");
    }

    @Test
    @DisplayName("Deve reenviar código")
    void deveReenviarCodigo() throws Exception {
        mockMvc.perform(post("/usuario/reenviar-codigo")
                .param("email", "test@test.com"))
                .andExpect(status().isOk());
        verify(verificationService).criarCodigoVerificacao(any());
    }
}
