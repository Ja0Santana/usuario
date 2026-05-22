package com.joaopaulo.usuario.infrastructure.business;

import com.joaopaulo.usuario.infrastructure.business.converter.UsuarioConverter;
import com.joaopaulo.usuario.infrastructure.business.dto.in.*;
import com.joaopaulo.usuario.infrastructure.entitiy.Endereco;
import com.joaopaulo.usuario.infrastructure.entitiy.Telefone;
import com.joaopaulo.usuario.infrastructure.entitiy.Usuario;
import com.joaopaulo.usuario.infrastructure.exceptions.ConflictException;
import com.joaopaulo.usuario.infrastructure.exceptions.UnauthorizedException;
import com.joaopaulo.usuario.infrastructure.repository.EnderecoRepository;
import com.joaopaulo.usuario.infrastructure.repository.TelefoneRepository;
import com.joaopaulo.usuario.infrastructure.repository.UsuarioRepository;
import com.joaopaulo.usuario.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioConverter usuarioConverter;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private EnderecoRepository enderecoRepository;
    @Mock private TelefoneRepository telefoneRepository;
    @Mock private VerificationService verificationService;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setup() {
        // Setup manual do contexto de segurança se necessário
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve salvar usuário com sucesso hashing a senha e criando código")
    void deveSalvarUsuarioComSucesso() {
        UsuarioDTOrequest request = UsuarioDTOrequest.builder()
                .email("novo@email.com")
                .senha("Senha@123")
                .build();
        
        Usuario usuarioEntity = Usuario.builder().email("novo@email.com").build();
        
        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("Senha@123")).thenReturn("hash123");
        when(usuarioConverter.paraUsuarioEntity(request)).thenReturn(usuarioEntity);
        when(usuarioRepository.save(any())).thenReturn(usuarioEntity);

        usuarioService.salvarUsuario(request);

        verify(passwordEncoder).encode("Senha@123");

        verify(usuarioRepository).save(usuarioEntity);
        verify(verificationService).criarCodigoVerificacao(usuarioEntity);
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando email já existe")
    void deveLancarErroEmailExistente() {
        when(usuarioRepository.existsByEmail("existe@email.com")).thenReturn(true);

        UsuarioDTOrequest request = UsuarioDTOrequest.builder().email("existe@email.com").build();
        assertThatThrownBy(() -> usuarioService.salvarUsuario(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Deve autenticar usuário e retornar token JWT")
    void deveAutenticarUsuario() {
        LoginDTORequest request = new LoginDTORequest("user@email.com", "senha", false);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@email.com");
        when(jwtUtil.generateToken("user@email.com", false)).thenReturn("token-jwt");

        String token = usuarioService.autenticarUsuario(request);

        assertThat(token).isEqualTo("token-jwt");
    }

    @Test
    @DisplayName("Deve resetar verificação ao alterar email no perfil")
    void deveResetarVerificacaoAoMudarEmail() {
        String token = "Bearer secret-token";
        String emailAntigo = "velho@email.com";
        String emailNovo = "novo@email.com";
        
        UsuarioModDTOrequest request = new UsuarioModDTOrequest();
        request.setEmail(emailNovo);
        
        Usuario usuarioExistente = Usuario.builder().email(emailAntigo).verificado(true).build();
        
        when(jwtUtil.extractUsername(any())).thenReturn(emailAntigo);
        when(usuarioRepository.findByEmail(emailAntigo)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.existsByEmail(emailNovo)).thenReturn(false);
        when(usuarioConverter.updateUsuario(eq(request), any())).thenReturn(usuarioExistente);
        when(usuarioRepository.save(any())).thenReturn(usuarioExistente);

        usuarioService.atualizaDadosUsuario(token, request);

        assertThat(usuarioExistente.isVerificado()).isFalse();
        verify(verificationService).criarCodigoVerificacao(usuarioExistente);
    }

    @Test
    @DisplayName("Deve permitir alterar endereço se usuário for o dono")
    void deveAlterarEnderecoDono() {
        mockAuthenticatedUser("dono@email.com");
        Usuario usuario = Usuario.builder().id(123L).email("dono@email.com").build();
        Endereco endereco = Endereco.builder().usuarioId(123L).build();
        
        when(usuarioRepository.findByEmail("dono@email.com")).thenReturn(Optional.of(usuario));
        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(endereco));
        
        usuarioService.atualizarEndereco(1L, new EnderecoDTOrequest());

        verify(enderecoRepository).save(any());
    }

    @Test
    @DisplayName("Deve proibir alterar endereço se usuário NÃO for o dono")
    void deveProibirAlterarEnderecoNaoDono() {
        mockAuthenticatedUser("invasor@email.com");
        Usuario usuario = Usuario.builder().id(999L).email("invasor@email.com").build();
        Endereco endereco = Endereco.builder().usuarioId(123L).build(); // Pertence ao ID 123
        
        when(usuarioRepository.findByEmail("invasor@email.com")).thenReturn(Optional.of(usuario));
        when(enderecoRepository.findById(1L)).thenReturn(Optional.of(endereco));

        EnderecoDTOrequest request = new EnderecoDTOrequest();
        assertThatThrownBy(() -> usuarioService.atualizarEndereco(1L, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Sem permissão para alterar este endereço");
    }

    @Test
    @DisplayName("Deve disparar erro se autenticação falhar")
    void deveDispararErroAutenticacao() {
        LoginDTORequest request = new LoginDTORequest("errado@email.com", "senha", false);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad Data"));

        assertThatThrownBy(() -> usuarioService.autenticarUsuario(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Deve deletar usuário se for o próprio")
    void deveDeletarProprioUsuario() {
        mockAuthenticatedUser("eu@email.com");
        usuarioService.deletarUsuarioPorEmail("eu@email.com");
        verify(usuarioRepository).deleteByEmail("eu@email.com");
    }

    @Test
    @DisplayName("Deve disparar erro ao tentar deletar outro usuário")
    void deveDispararErroDeletarOutro() {
        mockAuthenticatedUser("eu@email.com");
        assertThatThrownBy(() -> usuarioService.deletarUsuarioPorEmail("outro@email.com"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Deve desativar o próprio usuário")
    void deveDesativarUsuario() {
        mockAuthenticatedUser("eu@email.com");
        Usuario usuario = Usuario.builder().email("eu@email.com").ativo(true).build();
        when(usuarioRepository.findByEmail("eu@email.com")).thenReturn(Optional.of(usuario));
        
        usuarioService.desativarUsuario("eu@email.com");
        
        assertThat(usuario.getAtivo()).isFalse();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve cadastrar telefone com sucesso")
    void deveCadastrarTelefone() {
        String token = "Bearer secret-token";
        String email = "user@test.com";
        TelefoneDTOrequest request = new TelefoneDTOrequest();
        Usuario usuario = Usuario.builder().id(1L).email(email).build();
        Telefone telefone = Telefone.builder().id(10L).usuarioId(1L).build();
        
        when(jwtUtil.extractUsername(any())).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(usuarioConverter.paraTelefoneEntity(request, 1L)).thenReturn(telefone);
        when(telefoneRepository.save(any())).thenReturn(telefone);
        
        usuarioService.cadastrarTelefone(token, request);
        
        verify(telefoneRepository).save(any());
        verify(usuarioConverter).paraTelefoneDTO(any());
    }
    @Test
    @DisplayName("Deve cadastrar endereço com sucesso")
    void deveCadastrarEndereco() {
        String token = "Bearer secret-token";
        String email = "user@test.com";
        EnderecoDTOrequest request = new EnderecoDTOrequest();
        Usuario usuario = Usuario.builder().id(1L).email(email).build();
        Endereco endereco = Endereco.builder().id(10L).usuarioId(1L).build();
        
        when(jwtUtil.extractUsername(any())).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(usuarioConverter.paraEnderecoEntity(request, 1L)).thenReturn(endereco);
        when(enderecoRepository.save(any())).thenReturn(endereco);
        
        usuarioService.cadastrarEndereco(token, request);
        
        verify(enderecoRepository).save(any());
        verify(usuarioConverter).paraEnderecoDTO(any());
    }

    @Test
    @DisplayName("Deve proibir alterar telefone se usuário NÃO for o dono")
    void deveProibirAlterarTelefoneNaoDono() {
        mockAuthenticatedUser("invasor@email.com");
        Usuario usuario = Usuario.builder().id(999L).email("invasor@email.com").build();
        Telefone telefone = Telefone.builder().usuarioId(123L).build();
        
        when(usuarioRepository.findByEmail("invasor@email.com")).thenReturn(Optional.of(usuario));
        when(telefoneRepository.findById(1L)).thenReturn(Optional.of(telefone));

        TelefoneDTOrequest request = new TelefoneDTOrequest();
        assertThatThrownBy(() -> usuarioService.atualizarTelefone(1L, request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Deve disparar erro se usuário não for encontrado por email")
    void deveDispararErroEmailNaoEncontrado() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarUsuarioPorEmail("nao@existe.com"))
                .isInstanceOf(com.joaopaulo.usuario.infrastructure.exceptions.ResourceNotFoundException.class);
    }

    private void mockAuthenticatedUser(String email) {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
    }
}
