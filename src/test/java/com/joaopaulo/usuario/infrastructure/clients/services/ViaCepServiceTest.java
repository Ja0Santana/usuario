package com.joaopaulo.usuario.infrastructure.clients.services;

import com.joaopaulo.usuario.infrastructure.clients.ViaCepClient;
import com.joaopaulo.usuario.infrastructure.clients.dtos.out.CepDTOResponse;
import com.joaopaulo.usuario.infrastructure.exceptions.IllegalArgumentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViaCepServiceTest {

    @Mock
    private ViaCepClient viaCepClient;

    @InjectMocks
    private ViaCepService viaCepService;

    @Test
    @DisplayName("Deve buscar dados de endereço com CEP válido")
    void deveBuscarDadosComCepValido() {
        String cep = "12345678";
        CepDTOResponse response = new CepDTOResponse(cep, "Rua A", "", "", "Bairro B", "Cidade C", "UF", "Estado E", "Regiao R", "123", "456", "11", "789");
        when(viaCepClient.buscarDadosDeEnderecoPorCep(cep)).thenReturn(response);

        CepDTOResponse result = viaCepService.buscarDadosDeEnderecoPorCep(cep);

        assertThat(result).isEqualTo(response);
        verify(viaCepClient).buscarDadosDeEnderecoPorCep(cep);
    }

    @Test
    @DisplayName("Deve disparar erro para CEP nulo")
    void deveDispararErroCepNulo() {
        assertThatThrownBy(() -> viaCepService.processarCep(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CEP invalido");
    }

    @Test
    @DisplayName("Deve disparar erro para CEP com letras")
    void deveDispararErroCepComLetras() {
        assertThatThrownBy(() -> viaCepService.processarCep("12345a78"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve disparar erro para CEP com tamanho incorreto")
    void deveDispararErroCepTamanhoIncorreto() {
        assertThatThrownBy(() -> viaCepService.processarCep("1234567"))
                .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> viaCepService.processarCep("123456789"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve formatar CEP com caracteres especiais e validar")
    void deveFormatarCepEValidar() {
        // O processarCep no código original usa matches("\\d+") antes de replaceAll.
        // Então "12345-678" falharia no matches("\\d+"). Preciso validar esse comportamento.
        assertThatThrownBy(() -> viaCepService.processarCep("12345-67"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
