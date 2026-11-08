package br.com.oficina.service;

import br.com.oficina.dto.ItemForm;
import br.com.oficina.dto.OrdemForm;
import br.com.oficina.exception.RecursoNaoEncontradoException;
import br.com.oficina.exception.RegraNegocioException;
import br.com.oficina.model.ItemOrdem;
import br.com.oficina.model.OrdemServico;
import br.com.oficina.model.Servico;
import br.com.oficina.model.StatusOrdem;
import br.com.oficina.model.Veiculo;
import br.com.oficina.repository.OrdemServicoRepository;
import br.com.oficina.repository.ServicoRepository;
import br.com.oficina.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrdemServicoService {

    static final int QUANTIDADE_MAXIMA_POR_ITEM = 99;
    static final int ITENS_MAXIMOS_POR_ORDEM = 50;

    private final OrdemServicoRepository ordemRepository;
    private final VeiculoRepository veiculoRepository;
    private final ServicoRepository servicoRepository;

    public OrdemServicoService(OrdemServicoRepository ordemRepository,
                               VeiculoRepository veiculoRepository,
                               ServicoRepository servicoRepository) {
        this.ordemRepository = ordemRepository;
        this.veiculoRepository = veiculoRepository;
        this.servicoRepository = servicoRepository;
    }

    /** Lista todas as ordens ou só as que estão no status informado. */
    @Transactional(readOnly = true)
    public List<OrdemServico> listar(StatusOrdem status) {
        if (status == null) {
            return ordemRepository.findAllByOrderByDataAberturaDesc();
        }
        return ordemRepository.findByStatusOrderByDataAberturaDesc(status);
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listarDoVeiculo(Long veiculoId) {
        return ordemRepository.findByVeiculoIdOrderByDataAberturaDesc(veiculoId);
    }

    @Transactional(readOnly = true)
    public OrdemServico buscar(Long id) {
        return ordemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));
    }

    public OrdemServico abrir(OrdemForm form) {
        if (form.getVeiculoId() == null) {
            throw new RegraNegocioException("veiculoId", "Escolha o veículo");
        }
        if (form.getDescricaoProblema() == null || form.getDescricaoProblema().trim().length() < 5) {
            throw new RegraNegocioException("descricaoProblema", "Descreva o problema com pelo menos 5 caracteres");
        }

        Veiculo veiculo = veiculoRepository.findById(form.getVeiculoId())
                .orElseThrow(() -> new RegraNegocioException("veiculoId", "Veículo não encontrado"));

        // um carro só pode ter uma ordem "viva" por vez (também barra clique duplo no botão)
        boolean jaTemOrdemAtiva = ordemRepository.existsByVeiculoIdAndStatusIn(
                veiculo.getId(), List.of(StatusOrdem.ABERTA, StatusOrdem.EM_ANDAMENTO));
        if (jaTemOrdemAtiva) {
            throw new RegraNegocioException("veiculoId",
                    "Este veículo já tem uma ordem aberta ou em andamento. Conclua ou cancele a anterior antes.");
        }

        OrdemServico ordem = new OrdemServico();
        ordem.setVeiculo(veiculo);
        ordem.setDescricaoProblema(form.getDescricaoProblema().trim());
        ordem.setStatus(StatusOrdem.ABERTA);
        ordem.setDataAbertura(LocalDateTime.now());
        return ordemRepository.save(ordem);
    }

    public void adicionarItem(Long ordemId, ItemForm form) {
        if (form.getServicoId() == null) {
            throw new RegraNegocioException("servicoId", "Escolha um serviço");
        }
        if (form.getQuantidade() == null || form.getQuantidade() < 1
                || form.getQuantidade() > QUANTIDADE_MAXIMA_POR_ITEM) {
            throw new RegraNegocioException("quantidade",
                    "A quantidade deve estar entre 1 e " + QUANTIDADE_MAXIMA_POR_ITEM);
        }

        OrdemServico ordem = carregarParaAlterar(ordemId);
        exigirEditavel(ordem);

        Servico servico = servicoRepository.findById(form.getServicoId())
                .orElseThrow(() -> new RegraNegocioException("servicoId", "Serviço não encontrado"));
        if (!servico.isAtivo()) {
            throw new RegraNegocioException("servicoId", "Este serviço está inativo");
        }

        // se o serviço já está na ordem, só soma a quantidade
        Optional<ItemOrdem> existente = ordem.getItens().stream()
                .filter(item -> item.getServico().getId().equals(servico.getId()))
                .findFirst();

        if (existente.isPresent()) {
            ItemOrdem item = existente.get();
            int novaQuantidade = item.getQuantidade() + form.getQuantidade();
            if (novaQuantidade > QUANTIDADE_MAXIMA_POR_ITEM) {
                throw new RegraNegocioException("quantidade", "Uma ordem aceita no máximo "
                        + QUANTIDADE_MAXIMA_POR_ITEM + " unidades do mesmo serviço (já tem " + item.getQuantidade() + ")");
            }
            item.setQuantidade(novaQuantidade);
        } else {
            if (ordem.getItens().size() >= ITENS_MAXIMOS_POR_ORDEM) {
                throw new RegraNegocioException(
                        "Uma ordem aceita no máximo " + ITENS_MAXIMOS_POR_ORDEM + " serviços diferentes");
            }
            // o preço vem do catálogo (banco), nunca do formulário
            ordem.adicionarItem(new ItemOrdem(servico, form.getQuantidade(), servico.getPreco()));
        }
        // sem save(): a ordem já é gerenciada pelo JPA e o commit grava as mudanças (cascade inclui o item novo)
    }

    public void removerItem(Long ordemId, Long itemId) {
        OrdemServico ordem = carregarParaAlterar(ordemId);
        exigirEditavel(ordem);

        // orphanRemoval = true: tirar da lista apaga a linha no banco
        boolean removeu = ordem.getItens().removeIf(item -> item.getId().equals(itemId));
        if (!removeu) {
            throw new RecursoNaoEncontradoException("Item não encontrado nesta ordem");
        }
    }

    public void mudarStatus(Long ordemId, StatusOrdem novo) {
        OrdemServico ordem = carregarParaAlterar(ordemId);

        if (!ordem.getStatus().podeIrPara(novo)) {
            throw new RegraNegocioException("Não é possível passar de \"" + ordem.getStatus().getRotulo()
                    + "\" para \"" + novo.getRotulo() + "\"");
        }
        if (novo == StatusOrdem.CONCLUIDA && ordem.getItens().isEmpty()) {
            throw new RegraNegocioException("Adicione pelo menos um serviço antes de concluir a ordem");
        }

        ordem.setStatus(novo);
        if (novo == StatusOrdem.CONCLUIDA) {
            ordem.setDataConclusao(LocalDateTime.now());
        }
    }

    private OrdemServico carregarParaAlterar(Long id) {
        return ordemRepository.buscarParaAlterar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));
    }

    private void exigirEditavel(OrdemServico ordem) {
        if (!ordem.getStatus().isEditavel()) {
            throw new RegraNegocioException("Esta ordem está " + ordem.getStatus().getRotulo().toLowerCase()
                    + " e não aceita mais alterações");
        }
    }
}
