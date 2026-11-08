package br.com.oficina.service;

import br.com.oficina.dto.ServicoRanking;
import br.com.oficina.model.OrdemServico;
import br.com.oficina.model.StatusOrdem;
import br.com.oficina.repository.ClienteRepository;
import br.com.oficina.repository.ItemOrdemRepository;
import br.com.oficina.repository.OrdemServicoRepository;
import br.com.oficina.repository.VeiculoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reúne os números exibidos na página inicial. Só lê, nunca grava. */
@Service
@Transactional(readOnly = true)
public class PainelService {

    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final OrdemServicoRepository ordemRepository;
    private final ItemOrdemRepository itemRepository;

    public PainelService(ClienteRepository clienteRepository,
                         VeiculoRepository veiculoRepository,
                         OrdemServicoRepository ordemRepository,
                         ItemOrdemRepository itemRepository) {
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.ordemRepository = ordemRepository;
        this.itemRepository = itemRepository;
    }

    public long totalClientes() {
        return clienteRepository.count();
    }

    public long totalVeiculos() {
        return veiculoRepository.count();
    }

    /** Quantidade de ordens em cada status, na ordem do enum. */
    public Map<StatusOrdem, Long> ordensPorStatus() {
        Map<StatusOrdem, Long> mapa = new LinkedHashMap<>();
        for (StatusOrdem status : StatusOrdem.values()) {
            mapa.put(status, ordemRepository.countByStatus(status));
        }
        return mapa;
    }

    /** Valor total das ordens já concluídas. */
    public BigDecimal faturamento() {
        BigDecimal soma = itemRepository.somarPorStatusDaOrdem(StatusOrdem.CONCLUIDA);
        return soma == null ? BigDecimal.ZERO : soma;
    }

    public List<OrdemServico> ultimasOrdens() {
        return ordemRepository.findTop5ByOrderByDataAberturaDesc();
    }

    public List<ServicoRanking> servicosMaisPedidos() {
        return itemRepository.maisPedidos(PageRequest.of(0, 5));
    }
}
