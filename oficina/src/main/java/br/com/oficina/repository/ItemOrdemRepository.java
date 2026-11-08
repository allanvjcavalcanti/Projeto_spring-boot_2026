package br.com.oficina.repository;

import br.com.oficina.dto.ServicoRanking;
import br.com.oficina.model.ItemOrdem;
import br.com.oficina.model.StatusOrdem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ItemOrdemRepository extends JpaRepository<ItemOrdem, Long> {

    boolean existsByServicoId(Long servicoId);

    /** Faturamento: soma (preço x quantidade) dos itens das ordens com o status informado. */
    @Query("select sum(i.precoUnitario * i.quantidade) from ItemOrdem i where i.ordem.status = :status")
    BigDecimal somarPorStatusDaOrdem(@Param("status") StatusOrdem status);

    /** Serviços mais pedidos em ordens que não foram canceladas. */
    @Query("select new br.com.oficina.dto.ServicoRanking(i.servico.nome, sum(i.quantidade)) "
            + "from ItemOrdem i where i.ordem.status <> br.com.oficina.model.StatusOrdem.CANCELADA "
            + "group by i.servico.nome order by sum(i.quantidade) desc")
    List<ServicoRanking> maisPedidos(Pageable limite);
}
