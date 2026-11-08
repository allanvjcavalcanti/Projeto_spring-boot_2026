package br.com.oficina.repository;

import br.com.oficina.model.OrdemServico;
import br.com.oficina.model.StatusOrdem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    List<OrdemServico> findAllByOrderByDataAberturaDesc();

    List<OrdemServico> findByStatusOrderByDataAberturaDesc(StatusOrdem status);

    List<OrdemServico> findByVeiculoIdOrderByDataAberturaDesc(Long veiculoId);

    List<OrdemServico> findTop5ByOrderByDataAberturaDesc();

    long countByStatus(StatusOrdem status);

    boolean existsByVeiculoId(Long veiculoId);

    boolean existsByVeiculoIdAndStatusIn(Long veiculoId, Collection<StatusOrdem> status);

    /**
     * Busca a ordem para alterá-la. O lock otimista faz o Hibernate incrementar a versão
     * ao final da transação: se outra pessoa mexeu na mesma ordem ao mesmo tempo, a segunda
     * gravação falha em vez de sobrescrever a primeira em silêncio.
     */
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select o from OrdemServico o where o.id = :id")
    Optional<OrdemServico> buscarParaAlterar(@Param("id") Long id);
}
