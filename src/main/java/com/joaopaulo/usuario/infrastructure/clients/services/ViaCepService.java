package com.joaopaulo.usuario.infrastructure.clients.services;

import com.joaopaulo.usuario.infrastructure.clients.ViaCepClient;
import com.joaopaulo.usuario.infrastructure.clients.dtos.out.CepDTOResponse;
import com.joaopaulo.usuario.infrastructure.exceptions.IllegalArgumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViaCepService {
    private final ViaCepClient viaCepClient;

    public CepDTOResponse buscarDadosDeEnderecoPorCep(String cep) {
        return viaCepClient.buscarDadosDeEnderecoPorCep(processarCep(cep));
    }

    public String processarCep(String cep) {
        if (cep == null || !cep.matches("\\d+")) {
            throw new IllegalArgumentException("CEP invalido, verifique as informações e tente novamente");
        }

        String cepFormatado = cep.replaceAll("\\D", "");

        if (cepFormatado.length() != 8) {
            throw new IllegalArgumentException("CEP invalido, verifique as informações e tente novamente");
        }

        return cepFormatado;
    }
}
