package br.com.oficina.repository;

import br.com.oficina.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    List<Veiculo> findAllByOrderByPlaca();

    List<Veiculo> findByClienteIdOrderByPlaca(Long clienteId);

    Optional<Veiculo> findByPlaca(String placa);

    boolean existsByClienteId(Long clienteId);
}
