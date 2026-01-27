package com.joaopaulo.usuario.infrastructure.business;

import com.joaopaulo.usuario.infrastructure.business.converter.UsuarioConverter;
import com.joaopaulo.usuario.infrastructure.business.dto.EnderecoDTO;
import com.joaopaulo.usuario.infrastructure.business.dto.TelefoneDTO;
import com.joaopaulo.usuario.infrastructure.business.dto.UsuarioDTO;
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

    public UsuarioDTO salvarUsuario(UsuarioDTO usuarioDTO) {
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));

        Usuario usuario = usuarioConverter.paraUsuarioEntity(usuarioDTO);
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

    public UsuarioDTO buscarUsuarioPorEmail(String email) {
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

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO usuarioDTO) {
        String email = jwtUtil.extractUsername(token.substring(7));
        usuarioDTO.setSenha(usuarioDTO.getSenha() != null ? passwordEncoder.encode(usuarioDTO.getSenha()) : null);
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email nao encontrado " + email));
        Usuario usuario = usuarioConverter.updateUsuario(usuarioDTO, usuarioEntity);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public EnderecoDTO atualizarEndereco(Long idEndereco, EnderecoDTO enderecoDTO) {
        Endereco enderecoEntity = enderecoRepository.findById(idEndereco).orElseThrow(() ->
                new ResourceNotFoundException("Endereco nao encontrado " + idEndereco));
        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, enderecoEntity);
        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO atualizarTelefone(Long idTelefone, TelefoneDTO telefoneDTO) {
        Telefone telefoneEntity = telefoneRepository.findById(idTelefone).orElseThrow(() ->
                new ResourceNotFoundException("Telefone nao encontrado " + idTelefone));
        Telefone telefone = usuarioConverter.updateTelefone(telefoneDTO, telefoneEntity);
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTO cadastrarEndereco(String token, EnderecoDTO enderecoDTO) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email nao encontrado " + email));
        Endereco endereco = usuarioConverter.paraEnderecoEntity(enderecoDTO, usuario.getId());
        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO cadastrarTelefone(String token, TelefoneDTO telefoneDTO) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email nao encontrado " + email));
        Telefone endereco = usuarioConverter.paraTelefoneEntity(telefoneDTO, usuario.getId());
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(endereco));
    }
}
