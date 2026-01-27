package com.joaopaulo.usuario.infrastructure.repository;


import com.joaopaulo.usuario.infrastructure.entitiy.Telefone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelefoneRepository extends JpaRepository<Telefone, Long> {
}
