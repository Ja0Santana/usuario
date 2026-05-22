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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.joaopaulo.usuario.infrastructure.utils.PasswordValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UsuarioService {
    @org.springframework.beans.factory.annotation.Value("${google.client-id}")
    private String googleClientId;
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
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
        PasswordValidator.validate(usuarioDTOrequest.getSenha());
        usuarioDTOrequest.setSenha(passwordEncoder.encode(usuarioDTOrequest.getSenha()));


        Usuario usuario = usuarioConverter.paraUsuarioEntity(usuarioDTOrequest);
        usuario.setAtivo(true);
        Usuario usuarioSalvo = java.util.Objects.requireNonNull(usuarioRepository.save(usuario));
        
        // Gera e envia código de verificação
        verificationService.criarCodigoVerificacao(usuarioSalvo);
        
        return usuarioConverter.paraUsuarioDTO(usuarioSalvo);
    }

    public String autenticarUsuario(LoginDTORequest loginDTORequest) {
        System.out.println("DEBUG - UsuarioService.autenticarUsuario - Email: " + loginDTORequest.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTORequest.getEmail(), loginDTORequest.getSenha())
            );
            System.out.println("DEBUG - Autenticação bem sucedida para: " + loginDTORequest.getEmail());
            boolean lembrarMe = loginDTORequest.getLembrarMe() != null && loginDTORequest.getLembrarMe();
            return jwtUtil.generateToken(authentication.getName(), lembrarMe);
        } catch (BadCredentialsException e) {
            System.err.println("DEBUG - Erro de senha para: " + loginDTORequest.getEmail());
            logger.error("Falha no login para o email {}: Senha incorreta", loginDTORequest.getEmail());
            throw new UnauthorizedException("Credenciais invalidas", e);
        } catch (UsernameNotFoundException e) {
            System.err.println("DEBUG - Usuário não encontrado: " + loginDTORequest.getEmail());
            logger.error("Falha no login: Email {} nao encontrado", loginDTORequest.getEmail());
            throw new UnauthorizedException("Credenciais invalidas", e);
        } catch (AuthenticationException e) {
            System.err.println("DEBUG - Erro geral de autenticação para: " + loginDTORequest.getEmail() + " - " + e.getMessage());
            logger.error("Falha na autenticacao para {}: {}", loginDTORequest.getEmail(), e.getMessage());
            throw new UnauthorizedException("Erro na autenticacao: " + e.getMessage(), e);
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
        if (usuarioModDTOrequest.getSenha() != null) {
            PasswordValidator.validate(usuarioModDTOrequest.getSenha());
            usuarioModDTOrequest.setSenha(passwordEncoder.encode(usuarioModDTOrequest.getSenha()));
        }

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

    public void solicitarRecuperacaoSenha(String email) {
        Usuario usuario = buscarEntityPorEmail(email);
        verificationService.criarCodigoRecuperacao(usuario);
    }

    @Transactional
    public void resetarSenha(ResetSenhaDTORequest resetSenhaDTORequest) {
        Usuario usuario = buscarEntityPorEmail(resetSenhaDTORequest.getEmail());
        verificationService.validarCodigoRecuperacao(resetSenhaDTORequest.getEmail(), resetSenhaDTORequest.getCodigo());
        
        PasswordValidator.validate(resetSenhaDTORequest.getNovaSenha());
        usuario.setSenha(passwordEncoder.encode(resetSenhaDTORequest.getNovaSenha()));
        usuarioRepository.save(usuario);
    }

    public String loginComGoogle(GoogleLoginDTORequest request) {
        try {
            NetHttpTransport transport = new NetHttpTransport();
            GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                throw new UnauthorizedException("Token do Google invalido");
            }
            Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            java.util.Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            Usuario usuario;
            if (usuarioOpt.isPresent()) {
                usuario = usuarioOpt.get();
                if (!usuario.getAtivo()) {
                    throw new UnauthorizedException("Esta conta foi desativada");
                }
            } else {
                usuario = new Usuario();
                usuario.setEmail(email);
                usuario.setNome(name);
                usuario.setFotoUrl(pictureUrl);
                usuario.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
                usuario.setAtivo(true);
                usuario.setVerificado(true);
                usuario = usuarioRepository.save(usuario);
            }
            boolean lembrarMe = request.getLembrarMe() != null && request.getLembrarMe();
            return jwtUtil.generateToken(usuario.getEmail(), lembrarMe);
        } catch (Exception e) {
            throw new UnauthorizedException("Falha na autenticacao com Google: " + e.getMessage(), e);
        }
    }
}
