package com.joaopaulo.usuario.infrastructure.business;

import com.joaopaulo.usuario.infrastructure.business.converter.UsuarioConverter;
import com.joaopaulo.usuario.infrastructure.business.dto.in.*;
import com.joaopaulo.usuario.infrastructure.business.dto.out.EnderecoDTOResponse;
import com.joaopaulo.usuario.infrastructure.business.dto.out.TelefoneDTOResponse;
import com.joaopaulo.usuario.infrastructure.business.dto.out.UsuarioDTOResponse;
import com.joaopaulo.usuario.infrastructure.entitiy.Endereco;
import com.joaopaulo.usuario.infrastructure.entitiy.Telefone;
import com.joaopaulo.usuario.infrastructure.entitiy.Usuario;
import com.joaopaulo.usuario.infrastructure.exceptions.ConflictException;
import com.joaopaulo.usuario.infrastructure.exceptions.IllegalArgumentException;
import com.joaopaulo.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.joaopaulo.usuario.infrastructure.exceptions.UnauthorizedException;
import com.joaopaulo.usuario.infrastructure.repository.EnderecoRepository;
import com.joaopaulo.usuario.infrastructure.repository.TelefoneRepository;
import com.joaopaulo.usuario.infrastructure.repository.UsuarioRepository;
import com.joaopaulo.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private static final String EMAIL_NAO_ENCONTRADO = "Email nao encontrado: ";
    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;

    public UsuarioDTOResponse salvarUsuario(UsuarioDTOrequest usuarioDTOrequest) {
        emailExiste(usuarioDTOrequest.getEmail());
        usuarioDTOrequest.setSenha(passwordEncoder.encode(usuarioDTOrequest.getSenha()));

        Usuario usuario = usuarioConverter.paraUsuarioEntity(usuarioDTOrequest);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public String autenticarUsuario(LoginDTORequest loginDTORequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTORequest.getEmail(), loginDTORequest.getSenha())
            );
            return "Bearer " + jwtUtil.generateToken(authentication.getName());
        }catch (BadCredentialsException | UsernameNotFoundException | AuthorizationDeniedException e){
            throw new UnauthorizedException("Credenciais invalidas", e.getCause());
        }
    }

    public void emailExiste (String email) {
        try {
            boolean existe = verificarEmailExistente(email);
            if (existe) {
                throw new ConflictException("Email já cadastrado: " + email);
            }
        } catch (ConflictException e) {
            throw new ConflictException("Email já cadastrado: " + email);
        }
    }

    public boolean verificarEmailExistente(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public UsuarioDTOResponse buscarUsuarioPorEmail(String email) {
        try {
            return usuarioConverter.paraUsuarioDTO(usuarioRepository.findByEmail(email).orElseThrow(() ->
                    new ResourceNotFoundException(EMAIL_NAO_ENCONTRADO + email)));
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(EMAIL_NAO_ENCONTRADO + email);
        }
    }

    public void deletarUsuarioPorEmail(String email) {
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTOResponse atualizaDadosUsuario(String token, UsuarioModDTOrequest usuarioModDTOrequest) {
        String email = jwtUtil.extractUsername(token.substring(7));
        usuarioModDTOrequest.setSenha(usuarioModDTOrequest.getSenha() != null ? passwordEncoder.encode(usuarioModDTOrequest.getSenha()) : null);
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException(EMAIL_NAO_ENCONTRADO + email));
        Usuario usuario = usuarioConverter.updateUsuario(usuarioModDTOrequest, usuarioEntity);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public EnderecoDTOResponse atualizarEndereco(Long idEndereco, EnderecoDTOrequest enderecoDTOrequest) {
        Endereco enderecoEntity = enderecoRepository.findById(idEndereco).orElseThrow(() ->
                new ResourceNotFoundException("Endereco nao encontrado: " + idEndereco));
        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTOrequest, enderecoEntity);
        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTOResponse atualizarTelefone(Long idTelefone, TelefoneDTOrequest telefoneDTOrequest) {
        Telefone telefoneEntity = telefoneRepository.findById(idTelefone).orElseThrow(() ->
                new ResourceNotFoundException("Telefone nao encontrado: " + idTelefone));
        Telefone telefone = usuarioConverter.updateTelefone(telefoneDTOrequest, telefoneEntity);
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTOResponse cadastrarEndereco(String token, EnderecoDTOrequest enderecoDTOrequest) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException(EMAIL_NAO_ENCONTRADO + email));
        Endereco endereco = usuarioConverter.paraEnderecoEntity(enderecoDTOrequest, usuario.getId());
        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTOResponse cadastrarTelefone(String token, TelefoneDTOrequest telefoneDTOrequest) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException(EMAIL_NAO_ENCONTRADO + email));
        Telefone endereco = usuarioConverter.paraTelefoneEntity(telefoneDTOrequest, usuario.getId());
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(endereco));
    }
}
