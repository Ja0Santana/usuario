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
import com.joaopaulo.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.joaopaulo.usuario.infrastructure.exceptions.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import com.joaopaulo.usuario.infrastructure.repository.EnderecoRepository;
import com.joaopaulo.usuario.infrastructure.repository.TelefoneRepository;
import com.joaopaulo.usuario.infrastructure.repository.UsuarioRepository;
import com.joaopaulo.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
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
    private final VerificationService verificationService;

    public UsuarioDTOResponse salvarUsuario(UsuarioDTOrequest usuarioDTOrequest) {
        emailExiste(usuarioDTOrequest.getEmail());
        usuarioDTOrequest.setSenha(passwordEncoder.encode(usuarioDTOrequest.getSenha()));

        Usuario usuario = usuarioConverter.paraUsuarioEntity(usuarioDTOrequest);
        Usuario usuarioSalvo = java.util.Objects.requireNonNull(usuarioRepository.save(usuario));
        
        // Gera e envia código de verificação
        verificationService.criarCodigoVerificacao(usuarioSalvo);
        
        return usuarioConverter.paraUsuarioDTO(usuarioSalvo);
    }

    public String autenticarUsuario(LoginDTORequest loginDTORequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTORequest.getEmail(), loginDTORequest.getSenha())
            );
            boolean lembrarMe = loginDTORequest.getLembrarMe() != null && loginDTORequest.getLembrarMe();
            return jwtUtil.generateToken(authentication.getName(), lembrarMe);
        }catch (BadCredentialsException | UsernameNotFoundException | AuthorizationDeniedException e){
            throw new UnauthorizedException("Credenciais invalidas", e.getCause());
        }
    }

    public void emailExiste(String email) {
        if (verificarEmailExistente(email)) {
            throw new ConflictException("Email já cadastrado: " + email);
        }
    }

    public boolean verificarEmailExistente(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public UsuarioDTOResponse buscarUsuarioPorEmail(String email) {
        return usuarioConverter.paraUsuarioDTO(
                usuarioRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException(EMAIL_NAO_ENCONTRADO + email)));
    }

    public Usuario buscarEntityPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(EMAIL_NAO_ENCONTRADO + email));
    }

    public UsuarioDTOResponse buscarUsuarioAutenticado() {
        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        return buscarUsuarioPorEmail(emailAutenticado);
    }

    public void deletarUsuarioPorEmail(String email) {
        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!emailAutenticado.equals(email)) {
            throw new UnauthorizedException("Sem permissão para deletar este usuário");
        }
        usuarioRepository.deleteByEmail(email);
    }

    public void desativarUsuario(String email) {
        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!emailAutenticado.equals(email)) {
            throw new UnauthorizedException("Sem permissão para desativar este usuário");
        }
        Usuario usuario = buscarEntityPorEmail(email);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    public UsuarioDTOResponse atualizaDadosUsuario(String token, UsuarioModDTOrequest usuarioModDTOrequest) {
        String emailToken = jwtUtil.extractUsername(token.substring(7));
        usuarioModDTOrequest.setSenha(usuarioModDTOrequest.getSenha() != null ? passwordEncoder.encode(usuarioModDTOrequest.getSenha()) : null);
        Usuario usuarioEntity = usuarioRepository.findByEmail(emailToken).orElseThrow(() ->
                new ResourceNotFoundException(EMAIL_NAO_ENCONTRADO + emailToken));
        
        boolean emailAlterado = usuarioModDTOrequest.getEmail() != null && !usuarioModDTOrequest.getEmail().equals(usuarioEntity.getEmail());
        
        if (emailAlterado) {
            emailExiste(usuarioModDTOrequest.getEmail()); // Verifica se o novo e-mail já está em uso
            usuarioEntity.setVerificado(false);
        }

        Usuario usuario = usuarioConverter.updateUsuario(usuarioModDTOrequest, usuarioEntity);
        Usuario usuarioAtualizado = java.util.Objects.requireNonNull(usuarioRepository.save(usuario));

        if (emailAlterado) {
            verificationService.criarCodigoVerificacao(usuarioAtualizado);
        }

        return usuarioConverter.paraUsuarioDTO(usuarioAtualizado);
    }

    public EnderecoDTOResponse atualizarEndereco(Long idEndereco, EnderecoDTOrequest enderecoDTOrequest) {
        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException(EMAIL_NAO_ENCONTRADO + emailAutenticado));
        Endereco enderecoEntity = java.util.Objects.requireNonNull(enderecoRepository.findById(idEndereco).orElseThrow(() ->
                new ResourceNotFoundException("Endereco nao encontrado: " + idEndereco)));
        if (!usuario.getId().equals(enderecoEntity.getUsuarioId())) {
            throw new UnauthorizedException("Sem permissão para alterar este endereço");
        }
        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTOrequest, enderecoEntity);
        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTOResponse atualizarTelefone(Long idTelefone, TelefoneDTOrequest telefoneDTOrequest) {
        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException(EMAIL_NAO_ENCONTRADO + emailAutenticado));
        Telefone telefoneEntity = java.util.Objects.requireNonNull(telefoneRepository.findById(idTelefone).orElseThrow(() ->
                new ResourceNotFoundException("Telefone nao encontrado: " + idTelefone)));
        if (!usuario.getId().equals(telefoneEntity.getUsuarioId())) {
            throw new UnauthorizedException("Sem permissão para alterar este telefone");
        }
        Telefone telefone = usuarioConverter.updateTelefone(telefoneDTOrequest, telefoneEntity);
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTOResponse cadastrarEndereco(String token, EnderecoDTOrequest enderecoDTOrequest) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException(EMAIL_NAO_ENCONTRADO + email));
        Endereco endereco = usuarioConverter.paraEnderecoEntity(enderecoDTOrequest, usuario.getId());
        Endereco enderecoSalvo = java.util.Objects.requireNonNull(enderecoRepository.save(endereco));
        return usuarioConverter.paraEnderecoDTO(enderecoSalvo);
    }

    public TelefoneDTOResponse cadastrarTelefone(String token, TelefoneDTOrequest telefoneDTOrequest) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException(EMAIL_NAO_ENCONTRADO + email));
        Telefone endereco = usuarioConverter.paraTelefoneEntity(telefoneDTOrequest, usuario.getId());
        Telefone telefoneSalvo = java.util.Objects.requireNonNull(telefoneRepository.save(endereco));
        return usuarioConverter.paraTelefoneDTO(telefoneSalvo);
    }
}
