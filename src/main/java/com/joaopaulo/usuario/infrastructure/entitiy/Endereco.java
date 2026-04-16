package com.joaopaulo.usuario.infrastructure.entitiy;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "endereco")
@Builder
public class Endereco implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "rua")
    private String rua;
    @Column(name = "numero")
    private Long numero;
    @Column(name = "bairro", length = 100)
    private String bairro;
    @Column(name = "complemento", length = 50)
    private String complemento;
    @Column(name = "cidade", length = 100)
    private String cidade;
    @Column(name = "estado", length = 2)
    private String estado;
    @Column(name = "cep", length = 9)
    private String cep;
    @Column(name = "usuario_id")
    private Long usuarioId;
}
