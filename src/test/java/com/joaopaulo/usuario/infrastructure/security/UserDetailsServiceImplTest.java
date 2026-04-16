package com.joaopaulo.usuario.infrastructure.security;

import com.joaopaulo.usuario.infrastructure.entitiy.Usuario;
import com.joaopaulo.usuario.infrastructure.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Deve carregar detalhes do usuário ativo com sucesso")
    void deveCarregarUsuarioAtivoComSucesso() {
        String email = "joao@email.com";
        Usuario usuario = Usuario.builder()
                .nome("João")
                .email(email)
                .senha("senha123")
                .ativo(true)
                .build();

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("senha123");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Deve carregar usuário com senha correta e estado inativo")
    void deveCarregarUsuarioInativo() {
        String email = "inativo@email.com";
        Usuario usuario = Usuario.builder()
                .email(email)
                .senha("hash123")
                .ativo(false)
                .build();

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails.isEnabled()).isFalse();
        assertThat(userDetails.getUsername()).isEqualTo(email);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não existir")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        String email = "inexistente@email.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado: " + email);
    }

    @Test
    @DisplayName("Deve considerar usuário ativo quando campo ativo for nulo (default)")
    void deveConsiderarAtivoQuandoNulo() {
        String email = "nulo@email.com";
        Usuario usuario = Usuario.builder()
                .email(email)
                .senha("senha123")
                .ativo(null)
                .build();

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails.isEnabled()).isTrue();
    }
}
