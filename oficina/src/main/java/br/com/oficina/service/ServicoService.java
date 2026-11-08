package br.com.oficina.service;

import br.com.oficina.exception.RecursoNaoEncontradoException;
import br.com.oficina.exception.RegraNegocioException;
import br.com.oficina.model.Servico;
import br.com.oficina.repository.ItemOrdemRepository;
import br.com.oficina.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ItemOrdemRepository itemRepository;

    public ServicoService(ServicoRepository servicoRepository, ItemOrdemRepository itemRepository) {
        this.servicoRepository = servicoRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<Servico> listar() {
        return servicoRepository.findAllByOrderByNome();
    }

    @Transactional(readOnly = true)
    public List<Servico> listarAtivos() {
        return servicoRepository.findByAtivoTrueOrderByNome();
    }

    @Transactional(readOnly = true)
    public Servico buscar(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado"));
    }

    public Servico salvar(Servico dados) {
        if (dados.getNome() == null || dados.getNome().isBlank()) {
            throw new RegraNegocioException("nome", "Informe o nome do serviço");
        }
        if (dados.getPreco() == null || dados.getPreco().signum() <= 0) {
            throw new RegraNegocioException("preco", "O preço deve ser maior que zero");
        }
        String nome = dados.getNome().trim();

        servicoRepository.findByNomeIgnoreCase(nome)
                .filter(outro -> !outro.getId().equals(dados.getId()))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("nome", "Já existe um serviço com este nome");
                });

        Servico servico = dados.getId() == null ? new Servico() : buscar(dados.getId());
        servico.setNome(nome);
        servico.setDescricao(dados.getDescricao() == null || dados.getDescricao().isBlank()
                ? null : dados.getDescricao().trim());
        servico.setPreco(dados.getPreco().setScale(2, RoundingMode.HALF_UP));
        servico.setAtivo(dados.isAtivo());
        return servicoRepository.save(servico);
    }

    public void excluir(Long id) {
        Servico servico = buscar(id);
        if (itemRepository.existsByServicoId(id)) {
            throw new RegraNegocioException("\"" + servico.getNome()
                    + "\" já foi usado em ordens de serviço. Em vez de excluir, desmarque \"ativo\" na edição");
        }
        servicoRepository.delete(servico);
    }
}
