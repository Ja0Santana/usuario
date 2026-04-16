package com.joaopaulo.usuario.infrastructure.business.converter;

import com.joaopaulo.usuario.infrastructure.business.dto.in.EnderecoDTOrequest;
import com.joaopaulo.usuario.infrastructure.business.dto.in.TelefoneDTOrequest;
import com.joaopaulo.usuario.infrastructure.business.dto.in.UsuarioDTOrequest;
import com.joaopaulo.usuario.infrastructure.business.dto.in.UsuarioModDTOrequest;
import com.joaopaulo.usuario.infrastructure.business.dto.out.UsuarioDTOResponse;
import com.joaopaulo.usuario.infrastructure.entitiy.Endereco;
import com.joaopaulo.usuario.infrastructure.entitiy.Telefone;
import com.joaopaulo.usuario.infrastructure.entitiy.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioConverterTest {

    private UsuarioConverter usuarioConverter;

    @BeforeEach
    void setUp() {
        usuarioConverter = new UsuarioConverter();
    }

    @Test
    @DisplayName("Deve converter UsuarioDTOrequest para Usuario entity com todos os campos")
    void deveConverterParaUsuarioEntity() {
        EnderecoDTOrequest enderecoDTO = EnderecoDTOrequest.builder()
                .rua("Rua A").numero(123L).bairro("Bairro B")
                .cidade("Cidade C").estado("ST").cep("12345-678")
                .build();

        TelefoneDTOrequest telefoneDTO = TelefoneDTOrequest.builder()
                .ddd("11").numero("98888-7777")
                .build();

        UsuarioDTOrequest dto = UsuarioDTOrequest.builder()
                .nome("João")
                .email("joao@email.com")
                .senha("senha123")
                .fotoUrl("http://foto.com")
                .enderecos(List.of(enderecoDTO))
                .telefones(List.of(telefoneDTO))
                .build();

        Usuario entity = usuarioConverter.paraUsuarioEntity(dto);

        assertThat(entity.getNome()).isEqualTo(dto.getNome());
        assertThat(entity.getEmail()).isEqualTo(dto.getEmail());
        assertThat(entity.getSenha()).isEqualTo(dto.getSenha());
        assertThat(entity.getFotoUrl()).isEqualTo(dto.getFotoUrl());
        assertThat(entity.getEnderecos()).hasSize(1);
        assertThat(entity.getTelefones()).hasSize(1);
        assertThat(entity.getEnderecos().get(0).getRua()).isEqualTo("Rua A");
        assertThat(entity.getTelefones().get(0).getNumero()).isEqualTo("98888-7777");
    }

    @Test
    @DisplayName("Deve converter UsuarioDTOrequest com listas nulas para entity com listas vazias")
    void deveConverterParaUsuarioEntityComListasNulas() {
        UsuarioDTOrequest dto = UsuarioDTOrequest.builder()
                .nome("João")
                .enderecos(null)
                .telefones(null)
                .build();

        Usuario entity = usuarioConverter.paraUsuarioEntity(dto);

        assertThat(entity.getEnderecos()).isEmpty();
        assertThat(entity.getTelefones()).isEmpty();
    }

    @Test
    @DisplayName("Deve converter Usuario entity para UsuarioDTOResponse")
    void deveConverterParaUsuarioDTO() {
        Usuario entity = Usuario.builder()
                .nome("João")
                .email("joao@email.com")
                .verificado(true)
                .enderecos(List.of(Endereco.builder().rua("Rua A").build()))
                .telefones(List.of(Telefone.builder().numero("98888-7777").build()))
                .build();

        UsuarioDTOResponse response = usuarioConverter.paraUsuarioDTO(entity);

        assertThat(response.getNome()).isEqualTo(entity.getNome());
        assertThat(response.getEmail()).isEqualTo(entity.getEmail());
        assertThat(response.isVerificado()).isTrue();
        assertThat(response.getEnderecos()).hasSize(1);
        assertThat(response.getTelefones()).hasSize(1);
        assertThat(response.getEnderecos().get(0).getRua()).isEqualTo("Rua A");
    }

    @Test
    @DisplayName("Deve atualizar usuario preservando campos nulos do DTO")
    void deveAtualizarUsuarioPreservandoCamposNulos() {
        Usuario entityOriginal = Usuario.builder()
                .id(1L)
                .nome("Nome Original")
                .email("original@email.com")
                .senha("senhaAntiga")
                .build();

        UsuarioModDTOrequest modDTO = UsuarioModDTOrequest.builder()
                .nome("Nome Novo")
                .email(null) // Não deve alterar
                .build();

        Usuario entityAtualizada = usuarioConverter.updateUsuario(modDTO, entityOriginal);

        assertThat(entityAtualizada.getId()).isEqualTo(1L);
        assertThat(entityAtualizada.getNome()).isEqualTo("Nome Novo");
        assertThat(entityAtualizada.getEmail()).isEqualTo("original@email.com");
        assertThat(entityAtualizada.getSenha()).isEqualTo("senhaAntiga");
    }

    @Test
    @DisplayName("Deve atualizar usuario preservando status verificado e ativo")
    void deveAtualizarUsuarioPreservandoStatus() {
        Usuario entityOriginal = Usuario.builder()
                .verificado(true)
                .ativo(false)
                .build();

        UsuarioModDTOrequest modDTO = UsuarioModDTOrequest.builder()
                .nome("Novo Nome")
                .build();

        Usuario entityAtualizada = usuarioConverter.updateUsuario(modDTO, entityOriginal);

        assertThat(entityAtualizada.isVerificado()).isTrue();
        assertThat(entityAtualizada.getAtivo()).isFalse();
    }

    @Test
    @DisplayName("Deve atualizar endereco preservando ID e usuarioId")
    void deveAtualizarEnderecoComSucesso() {
        Endereco entityOriginal = Endereco.builder()
                .id(10L)
                .usuarioId(1L)
                .rua("Rua Antiga")
                .cidade("Cidade Antiga")
                .build();

        EnderecoDTOrequest dto = EnderecoDTOrequest.builder()
                .rua("Rua Nova")
                .build();

        Endereco entityAtualizada = usuarioConverter.updateEndereco(dto, entityOriginal);

        assertThat(entityAtualizada.getId()).isEqualTo(10L);
        assertThat(entityAtualizada.getUsuarioId()).isEqualTo(1L);
        assertThat(entityAtualizada.getRua()).isEqualTo("Rua Nova");
        assertThat(entityAtualizada.getCidade()).isEqualTo("Cidade Antiga");
    }

    @Test
    @DisplayName("Deve atualizar telefone preservando ID e usuarioId")
    void deveAtualizarTelefoneComSucesso() {
        Telefone entityOriginal = Telefone.builder()
                .id(5L)
                .usuarioId(1L)
                .ddd("11")
                .numero("91111-1111")
                .build();

        TelefoneDTOrequest dto = TelefoneDTOrequest.builder()
                .numero("92222-2222")
                .build();

        Telefone entityAtualizada = usuarioConverter.updateTelefone(dto, entityOriginal);

        assertThat(entityAtualizada.getId()).isEqualTo(5L);
        assertThat(entityAtualizada.getUsuarioId()).isEqualTo(1L);
        assertThat(entityAtualizada.getNumero()).isEqualTo("92222-2222");
        assertThat(entityAtualizada.getDdd()).isEqualTo("11");
    }

    @Test
    @DisplayName("Deve converter EnderecoDTOrequest para nova entity com usuarioId")
    void deveConverterParaEnderecoEntity() {
        EnderecoDTOrequest dto = EnderecoDTOrequest.builder()
                .rua("Rua A")
                .numero(100L)
                .build();

        Endereco entity = usuarioConverter.paraEnderecoEntity(dto, 1L);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getUsuarioId()).isEqualTo(1L);
        assertThat(entity.getRua()).isEqualTo("Rua A");
        assertThat(entity.getNumero()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Deve converter TelefoneDTOrequest para nova entity com usuarioId")
    void deveConverterParaTelefoneEntity() {
        TelefoneDTOrequest dto = TelefoneDTOrequest.builder()
                .ddd("21")
                .numero("99999-9999")
                .build();

        Telefone entity = usuarioConverter.paraTelefoneEntity(dto, 2L);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getUsuarioId()).isEqualTo(2L);
        assertThat(entity.getDdd()).isEqualTo("21");
        assertThat(entity.getNumero()).isEqualTo("99999-9999");
    }
}
