package br.com.oficina.repository;

import br.com.oficina.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findAllByOrderByNome();

    // o Spring Data monta o SQL a partir do nome do método
    List<Cliente> findByNomeContainingIgnoreCaseOrderByNome(String nome);

    Optional<Cliente> findByCpf(String cpf);
}
