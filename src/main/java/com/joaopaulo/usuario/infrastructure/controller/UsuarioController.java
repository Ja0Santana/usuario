package com.joaopaulo.usuario.infrastructure.controller;

import com.joaopaulo.usuario.infrastructure.business.UsuarioService;
import com.joaopaulo.usuario.infrastructure.business.dto.EnderecoDTO;
import com.joaopaulo.usuario.infrastructure.business.dto.TelefoneDTO;
import com.joaopaulo.usuario.infrastructure.business.dto.UsuarioDTO;
import com.joaopaulo.usuario.infrastructure.entitiy.Usuario;
import com.joaopaulo.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtutil;

    @PostMapping
    public ResponseEntity<UsuarioDTO> salvarUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.salvarUsuario(usuarioDTO));
    }

    @PostMapping("/login")
    public String login(@RequestBody UsuarioDTO usuarioDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usuarioDTO.getEmail(), usuarioDTO.getSenha())
        );
        return "Bearer " + jwtutil.generateToken(authentication.getName());
    }

    @GetMapping
    public ResponseEntity<UsuarioDTO> buscarUsuarioPorEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorEmail(email));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deletarUsuarioPorEmail(@PathVariable String email) {
        usuarioService.deletarUsuarioPorEmail(email);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<UsuarioDTO> atualizarUsuario(@RequestBody UsuarioDTO usuarioDTO, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(usuarioService.atualizaDadosUsuario(token, usuarioDTO));
    }

    @PutMapping("/endereco")
    public ResponseEntity<EnderecoDTO> autalizarEnderecoUsuario(@RequestBody EnderecoDTO enderecoDTO, @RequestParam("id") Long id) {
        return ResponseEntity.ok(usuarioService.atualizarEndereco(id, enderecoDTO));
    }

    @PutMapping("/telefone")
    public ResponseEntity<TelefoneDTO> autalizarTelefoneUsuario(@RequestBody TelefoneDTO telefoneDTO, @RequestParam("id") Long idTelefone) {
        return ResponseEntity.ok(usuarioService.atualizarTelefone(idTelefone, telefoneDTO));
    }

    @PostMapping("/endereco")
    public ResponseEntity<EnderecoDTO> cadastrarEndereco(@RequestBody EnderecoDTO enderecoDTO, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(usuarioService.cadastrarEndereco(token, enderecoDTO));
    }

    @PostMapping("/telefone")
    public ResponseEntity<TelefoneDTO> cadastrarTelefone(@RequestBody TelefoneDTO telefoneDTO, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(usuarioService.cadastrarTelefone(token, telefoneDTO));
    }
}
