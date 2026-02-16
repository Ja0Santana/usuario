package com.joaopaulo.usuario.infrastructure.business;

import com.joaopaulo.usuario.infrastructure.business.converter.UsuarioConverter;
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
import com.joaopaulo.usuario.infrastructure.exceptions.ConflictException;
import com.joaopaulo.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.joaopaulo.usuario.infrastructure.repository.EnderecoRepository;
import com.joaopaulo.usuario.infrastructure.repository.TelefoneRepository;
import com.joaopaulo.usuario.infrastructure.repository.UsuarioRepository;
import com.joaopaulo.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;

    public UsuarioDTOResponse salvarUsuario(UsuarioDTOrequest usuarioDTOrequest) {
        emailExiste(usuarioDTOrequest.getEmail());
        usuarioDTOrequest.setSenha(passwordEncoder.encode(usuarioDTOrequest.getSenha()));

        Usuario usuario = usuarioConverter.paraUsuarioEntity(usuarioDTOrequest);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public void emailExiste (String email) {
        try {
            boolean existe = verificarEmailExistente(email);
            if (existe) {
                throw new ConflictException("Email já cadastrado: " + email);
            }
        } catch (ConflictException e) {
            throw new ConflictException("Email já cadastrado: " + e.getCause());
        }
    }

    public boolean verificarEmailExistente(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public UsuarioDTOResponse buscarUsuarioPorEmail(String email) {
        try {
            return usuarioConverter.paraUsuarioDTO(usuarioRepository.findByEmail(email).orElseThrow(() ->
                    new ResourceNotFoundException("Email nao encontrado " + email)));
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Email nao encontrado " + email);
        }
    }

    public void deletarUsuarioPorEmail(String email) {
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTOResponse atualizaDadosUsuario(String token, UsuarioModDTOrequest usuarioModDTOrequest) {
        String email = jwtUtil.extractUsername(token.substring(7));
        usuarioModDTOrequest.setSenha(usuarioModDTOrequest.getSenha() != null ? passwordEncoder.encode(usuarioModDTOrequest.getSenha()) : null);
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email nao encontrado " + email));
        Usuario usuario = usuarioConverter.updateUsuario(usuarioModDTOrequest, usuarioEntity);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public EnderecoDTOResponse atualizarEndereco(Long idEndereco, EnderecoDTOrequest enderecoDTOrequest) {
        Endereco enderecoEntity = enderecoRepository.findById(idEndereco).orElseThrow(() ->
                new ResourceNotFoundException("Endereco nao encontrado " + idEndereco));
        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTOrequest, enderecoEntity);
        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTOResponse atualizarTelefone(Long idTelefone, TelefoneDTOrequest telefoneDTOrequest) {
        Telefone telefoneEntity = telefoneRepository.findById(idTelefone).orElseThrow(() ->
                new ResourceNotFoundException("Telefone nao encontrado " + idTelefone));
        Telefone telefone = usuarioConverter.updateTelefone(telefoneDTOrequest, telefoneEntity);
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTOResponse cadastrarEndereco(String token, EnderecoDTOrequest enderecoDTOrequest) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email nao encontrado " + email));
        Endereco endereco = usuarioConverter.paraEnderecoEntity(enderecoDTOrequest, usuario.getId());
        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTOResponse cadastrarTelefone(String token, TelefoneDTOrequest telefoneDTOrequest) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email nao encontrado " + email));
        Telefone endereco = usuarioConverter.paraTelefoneEntity(telefoneDTOrequest, usuario.getId());
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(endereco));
    }
}
