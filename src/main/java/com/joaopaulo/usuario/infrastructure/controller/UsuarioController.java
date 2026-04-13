package com.joaopaulo.usuario.infrastructure.controller;

import com.joaopaulo.usuario.infrastructure.business.UsuarioService;
import com.joaopaulo.usuario.infrastructure.business.dto.in.*;
import com.joaopaulo.usuario.infrastructure.business.dto.out.EnderecoDTOResponse;
import com.joaopaulo.usuario.infrastructure.business.dto.out.TelefoneDTOResponse;
import com.joaopaulo.usuario.infrastructure.business.dto.out.UsuarioDTOResponse;
import com.joaopaulo.usuario.infrastructure.clients.dtos.out.CepDTOResponse;
import com.joaopaulo.usuario.infrastructure.clients.services.ViaCepService;
import com.joaopaulo.usuario.infrastructure.security.SecurityConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
@Tag(name = "Usuario", description = "Endpoints para criação e gerenciamento de usuários")
@SecurityRequirement(name = SecurityConfig.SECURITY_SCHEME)
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final ViaCepService viaCepService;

    @PostMapping
    @Operation(summary = "Criar um novo usuário", description = "Endpoint para criar um novo usuário no sistema.")
    @ApiResponse(responseCode = "200", description = "Usuário criado com sucesso")
    @ApiResponse(responseCode = "409", description = "Usuário já cadastrado")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<UsuarioDTOResponse> salvarUsuario(@RequestBody UsuarioDTOrequest usuarioDTOrequest) {
        return ResponseEntity.ok(usuarioService.salvarUsuario(usuarioDTOrequest));
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuário", description = "Endpoint para autenticar um usuário e obter um token de acesso.")
    @ApiResponse(responseCode = "200", description = "Usuário logado com sucesso")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @ApiResponse(responseCode = "403", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<String> login(@RequestBody LoginDTORequest loginDTORequest) {
        return ResponseEntity.ok(usuarioService.autenticarUsuario(loginDTORequest));
    }

    @GetMapping("/me")
    @Operation(summary = "Buscar dados do usuário autenticado", description = "Retorna os dados do usuário identificado pelo token JWT.")
    @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso")
    @ApiResponse(responseCode = "401", description = "Token inválido ou ausente")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<UsuarioDTOResponse> buscarUsuarioAutenticado() {
        return ResponseEntity.ok(usuarioService.buscarUsuarioAutenticado());
    }

    @GetMapping
    @Operation(summary = "Buscar usuário por email", description = "Endpoint interno para buscar dados de um usuário pelo email.")
    @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso")
    @ApiResponse(responseCode = "403", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<UsuarioDTOResponse> buscarUsuarioPorEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorEmail(email));
    }

    @DeleteMapping("/{email}")
    @Operation(summary = "Deletar usuário por email", description = "Endpoint para deletar a conta do usuário autenticado.")
    @ApiResponse(responseCode = "200", description = "Usuário deletado com sucesso")
    @ApiResponse(responseCode = "401", description = "Sem permissão para deletar este usuário")
    @ApiResponse(responseCode = "403", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<Void> deletarUsuarioPorEmail(@PathVariable String email) {
        usuarioService.deletarUsuarioPorEmail(email);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    @Operation(summary = "Atualizar dados do usuário", description = "Endpoint para atualizar os dados de um usuário.")
    @ApiResponse(responseCode = "200", description = "Dados do usuário atualizados com sucesso")
    @ApiResponse(responseCode = "403", description = "Credenciais inválidas")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<UsuarioDTOResponse> atualizarUsuario(@RequestBody UsuarioModDTOrequest usuarioModDTOrequest, @RequestHeader(name = "Authorization", required = false) String token) {
        return ResponseEntity.ok(usuarioService.atualizaDadosUsuario(token, usuarioModDTOrequest));
    }

    @PutMapping("/endereco")
    @Operation(summary = "Atualizar endereço do usuário", description = "Endpoint para atualizar o endereço do usuário autenticado.")
    @ApiResponse(responseCode = "200", description = "Endereço do usuário atualizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Sem permissão para alterar este endereço")
    @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<EnderecoDTOResponse> autalizarEnderecoUsuario(@RequestBody EnderecoDTOrequest enderecoDTOrequest, @RequestParam("id") Long id) {
        return ResponseEntity.ok(usuarioService.atualizarEndereco(id, enderecoDTOrequest));
    }

    @PutMapping("/telefone")
    @Operation(summary = "Atualizar telefone do usuário", description = "Endpoint para atualizar o telefone do usuário autenticado.")
    @ApiResponse(responseCode = "200", description = "Telefone do usuário atualizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Sem permissão para alterar este telefone")
    @ApiResponse(responseCode = "404", description = "Telefone não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<TelefoneDTOResponse> autalizarTelefoneUsuario(@RequestBody TelefoneDTOrequest telefoneDTOrequest, @RequestParam("id") Long idTelefone) {
        return ResponseEntity.ok(usuarioService.atualizarTelefone(idTelefone, telefoneDTOrequest));
    }

    @PostMapping("/endereco")
    @Operation(summary = "Cadastrar novo endereço para o usuário", description = "Endpoint para cadastrar um novo endereço para o usuário.")
    @ApiResponse(responseCode = "200", description = "Endereço cadastrado com sucesso")
    @ApiResponse(responseCode = "403", description = "Credenciais inválidas")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<EnderecoDTOResponse> cadastrarEndereco(@RequestBody EnderecoDTOrequest enderecoDTOrequest, @RequestHeader(name = "Authorization", required = false) String token) {
        return ResponseEntity.ok(usuarioService.cadastrarEndereco(token, enderecoDTOrequest));
    }

    @PostMapping("/telefone")
    @Operation(summary = "Cadastrar novo telefone para o usuário", description = "Endpoint para cadastrar um novo telefone para o usuário.")
    @ApiResponse(responseCode = "200", description = "Telefone cadastrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "403", description = "Credenciais inválidas")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<TelefoneDTOResponse> cadastrarTelefone(@RequestBody TelefoneDTOrequest telefoneDTOrequest, @RequestHeader(name = "Authorization", required = false) String token) {
        return ResponseEntity.ok(usuarioService.cadastrarTelefone(token, telefoneDTOrequest));
    }

    @GetMapping("/endereco/{cep}")
    @Operation(summary = "Busca endereço pelo CEP", description = "Endpoint para buscar endereço pelo CEP.")
    @ApiResponse(responseCode = "200", description = "Endereço encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    @ApiResponse(responseCode = "400", description = "CEP inválido")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<CepDTOResponse> buscarDadosDeEnderecoPorCep(@PathVariable("cep") String cep) {
        return ResponseEntity.ok(viaCepService.buscarDadosDeEnderecoPorCep(cep));
    }
}
