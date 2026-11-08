package br.com.oficina.service;

import br.com.oficina.exception.RecursoNaoEncontradoException;
import br.com.oficina.exception.RegraNegocioException;
import br.com.oficina.model.Cliente;
import br.com.oficina.repository.ClienteRepository;
import br.com.oficina.repository.VeiculoRepository;
import br.com.oficina.util.Documentos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;

    public ClienteService(ClienteRepository clienteRepository, VeiculoRepository veiculoRepository) {
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar(String busca) {
        if (busca == null || busca.isBlank()) {
            return clienteRepository.findAllByOrderByNome();
        }
        return clienteRepository.findByNomeContainingIgnoreCaseOrderByNome(busca.trim());
    }

    @Transactional(readOnly = true)
    public Cliente buscar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    /** Cria (id nulo) ou atualiza (id preenchido) um cliente. */
    public Cliente salvar(Cliente dados) {
        if (dados.getNome() == null || dados.getNome().isBlank()) {
            throw new RegraNegocioException("nome", "Informe o nome do cliente");
        }
        String cpf = Documentos.somenteDigitos(dados.getCpf());

        if (!Documentos.cpfValido(cpf)) {
            throw new RegraNegocioException("cpf", "CPF inválido, confira os dígitos");
        }
        clienteRepository.findByCpf(cpf)
                .filter(outro -> !outro.getId().equals(dados.getId()))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("cpf", "Já existe um cliente com este CPF");
                });

        Cliente cliente = dados.getId() == null ? new Cliente() : buscar(dados.getId());
        cliente.setNome(dados.getNome().trim());
        cliente.setCpf(cpf);
        cliente.setTelefone(vazioParaNulo(dados.getTelefone()));
        cliente.setEmail(vazioParaNulo(dados.getEmail()));
        return clienteRepository.save(cliente);
    }

    public void excluir(Long id) {
        Cliente cliente = buscar(id);
        if (veiculoRepository.existsByClienteId(id)) {
            throw new RegraNegocioException(
                    "Não dá para excluir " + cliente.getNome() + ": ele ainda tem veículos cadastrados");
        }
        clienteRepository.delete(cliente);
    }

    private String vazioParaNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
