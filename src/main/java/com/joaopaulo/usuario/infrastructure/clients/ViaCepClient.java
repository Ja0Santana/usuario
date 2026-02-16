package com.joaopaulo.usuario.infrastructure.clients;

import com.joaopaulo.usuario.infrastructure.clients.dtos.out.CepDTOResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ViaCepClient", url = "${viacep.url}")
public interface ViaCepClient {

    @GetMapping("/ws/{cep}/json/")
    CepDTOResponse buscarDadosDeEnderecoPorCep(@PathVariable("cep") String cep);
}
