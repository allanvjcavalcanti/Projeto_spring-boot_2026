package br.com.oficina.repository;

import br.com.oficina.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    List<Servico> findAllByOrderByNome();

    List<Servico> findByAtivoTrueOrderByNome();

    Optional<Servico> findByNomeIgnoreCase(String nome);
}
