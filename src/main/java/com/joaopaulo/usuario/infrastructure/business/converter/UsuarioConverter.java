package com.joaopaulo.usuario.infrastructure.business.converter;

import com.joaopaulo.usuario.infrastructure.business.dto.in.EnderecoDTOrequest;
import com.joaopaulo.usuario.infrastructure.business.dto.in.TelefoneDTOrequest;
import com.joaopaulo.usuario.infrastructure.business.dto.in.UsuarioDTOrequest;
import com.joaopaulo.usuario.infrastructure.business.dto.in.UsuarioModDTOrequest;
import com.joaopaulo.usuario.infrastructure.business.dto.out.EnderecoDTOResponse;
import com.joaopaulo.usuario.infrastructure.business.dto.out.TelefoneDTOResponse;
import com.joaopaulo.usuario.infrastructure.business.dto.out.UsuarioDTOResponse;
import com.joaopaulo.usuario.infrastructure.entitiy.Endereco;
import com.joaopaulo.usuario.infrastructure.entitiy.Telefone;
import com.joaopaulo.usuario.infrastructure.entitiy.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class UsuarioConverter {

    public Usuario paraUsuarioEntity(UsuarioDTOrequest usuarioDTOrequest) {
        return Usuario.builder()
                .nome(usuarioDTOrequest.getNome())
                .email(usuarioDTOrequest.getEmail())
                .senha(usuarioDTOrequest.getSenha())
                .enderecos(paraListaEndereco(usuarioDTOrequest.getEnderecos()))
                .telefones(paraListaTelefones(usuarioDTOrequest.getTelefones()))
                .build();
    }

    public List<Endereco> paraListaEndereco(List<EnderecoDTOrequest> enderecoDTOrequests) {
        return enderecoDTOrequests.stream().map(this::paraEndereco).toList();
    }

    public Endereco paraEndereco(EnderecoDTOrequest enderecoDTOrequest) {
        return Endereco.builder()
                .id(enderecoDTOrequest.getId())
                .rua(enderecoDTOrequest.getRua())
                .numero(enderecoDTOrequest.getNumero())
                .complemento(enderecoDTOrequest.getComplemento())
                .cidade(enderecoDTOrequest.getCidade())
                .estado(enderecoDTOrequest.getEstado())
                .cep(enderecoDTOrequest.getCep())
                .build();
    }

    public List<Telefone> paraListaTelefones(List<TelefoneDTOrequest> telefoneDTOrequests) {
        return telefoneDTOrequests.stream().map(this::paraTelefone).toList();
    }

    public Telefone paraTelefone(TelefoneDTOrequest telefoneDTOrequest) {
        return Telefone.builder()
                .id(telefoneDTOrequest.getId())
                .numero(telefoneDTOrequest.getNumero())
                .ddd(telefoneDTOrequest.getDdd())
                .build();
    }

    //----------------------------------------------------------------------------------------

    public UsuarioDTOResponse paraUsuarioDTO(Usuario usuario) {
        return UsuarioDTOResponse.builder()
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .enderecos(paraListaEnderecoDTO(usuario.getEnderecos()))
                .telefones(paraListaTelefonesDTO(usuario.getTelefones()))
                .build();
    }

    public List<EnderecoDTOResponse> paraListaEnderecoDTO(List<Endereco> endereco) {
        return endereco.stream().map(this::paraEnderecoDTO).toList();
    }

    public EnderecoDTOResponse paraEnderecoDTO(Endereco endereco) {
        return EnderecoDTOResponse.builder()
                .id(endereco.getId())
                .rua(endereco.getRua())
                .numero(endereco.getNumero())
                .complemento(endereco.getComplemento())
                .cidade(endereco.getCidade())
                .estado(endereco.getEstado())
                .cep(endereco.getCep())
                .build();
    }

    public List<TelefoneDTOResponse> paraListaTelefonesDTO(List<Telefone> telefone) {
        return telefone.stream().map(this::paraTelefoneDTO).toList();
    }

    public TelefoneDTOResponse paraTelefoneDTO(Telefone telefone) {
        return TelefoneDTOResponse.builder()
                .id(telefone.getId())
                .numero(telefone.getNumero())
                .ddd(telefone.getDdd())
                .build();
    }

    public Usuario updateUsuario(UsuarioModDTOrequest usuarioModDTOrequest, Usuario usuarioEntity) {
        return Usuario.builder()
                .nome(usuarioModDTOrequest.getNome() != null ? usuarioModDTOrequest.getNome() : usuarioEntity.getNome())
                .id(usuarioEntity.getId())
                .email(usuarioModDTOrequest.getEmail() != null ? usuarioModDTOrequest.getEmail() : usuarioEntity.getEmail())
                .senha(usuarioModDTOrequest.getSenha() != null ? usuarioModDTOrequest.getSenha() : usuarioEntity.getSenha())
                .enderecos(usuarioEntity.getEnderecos())
                .telefones(usuarioEntity.getTelefones())
                .build();
    }

    public Endereco updateEndereco(EnderecoDTOrequest enderecoDTOrequest, Endereco enderecoEntity) {
        return Endereco.builder()
                .id(enderecoEntity.getId())
                .rua(enderecoDTOrequest.getRua() != null ? enderecoDTOrequest.getRua() : enderecoEntity.getRua())
                .numero(enderecoDTOrequest.getNumero() != null ? enderecoDTOrequest.getNumero() : enderecoEntity.getNumero())
                .cidade(enderecoDTOrequest.getCidade() != null ? enderecoDTOrequest.getCidade() : enderecoEntity.getCidade())
                .cep(enderecoDTOrequest.getCep() != null ? enderecoDTOrequest.getCep() : enderecoEntity.getCep())
                .complemento(enderecoDTOrequest.getComplemento() != null ? enderecoDTOrequest.getComplemento() : enderecoEntity.getComplemento())
                .estado(enderecoDTOrequest.getEstado() != null ? enderecoDTOrequest.getEstado() : enderecoEntity.getEstado())
                .usuarioId(enderecoEntity.getUsuarioId())
                .build();
    }

    public Telefone updateTelefone(TelefoneDTOrequest telefoneDTOrequest, Telefone telefoneEntity) {
        return Telefone.builder()
                .id(telefoneEntity.getId())
                .numero(telefoneDTOrequest.getNumero() != null ? telefoneDTOrequest.getNumero() : telefoneEntity.getNumero())
                .ddd(telefoneDTOrequest.getDdd() != null ? telefoneDTOrequest.getDdd() : telefoneEntity.getDdd())
                .usuarioId(telefoneEntity.getUsuarioId())
                .build();
    }

    public Endereco paraEnderecoEntity(EnderecoDTOrequest enderecoDTOrequest, Long idUsuario) {
        return Endereco.builder()
                .rua(enderecoDTOrequest.getRua())
                .numero(enderecoDTOrequest.getNumero())
                .cidade(enderecoDTOrequest.getCidade())
                .complemento(enderecoDTOrequest.getComplemento())
                .cep(enderecoDTOrequest.getCep())
                .estado(enderecoDTOrequest.getEstado())
                .usuarioId(idUsuario)
                .build();
    }

    public Telefone paraTelefoneEntity(TelefoneDTOrequest telefoneDTOrequest, Long idUsuario) {
        return Telefone.builder()
                .numero(telefoneDTOrequest.getNumero())
                .ddd(telefoneDTOrequest.getDdd())
                .usuarioId(idUsuario)
                .build();
    }
}
