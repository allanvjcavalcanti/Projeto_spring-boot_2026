package br.com.oficina.service;

import br.com.oficina.dto.VeiculoForm;
import br.com.oficina.exception.RecursoNaoEncontradoException;
import br.com.oficina.exception.RegraNegocioException;
import br.com.oficina.model.Cliente;
import br.com.oficina.model.Veiculo;
import br.com.oficina.repository.ClienteRepository;
import br.com.oficina.repository.OrdemServicoRepository;
import br.com.oficina.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class VeiculoService {

    // ABC1234 (antiga) ou ABC1D23 (Mercosul), já sem hífen e em maiúsculas
    private static final Pattern PLACA = Pattern.compile("[A-Z]{3}\\d[A-Z0-9]\\d{2}");

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final OrdemServicoRepository ordemRepository;

    public VeiculoService(VeiculoRepository veiculoRepository,
                          ClienteRepository clienteRepository,
                          OrdemServicoRepository ordemRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
        this.ordemRepository = ordemRepository;
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listar() {
        return veiculoRepository.findAllByOrderByPlaca();
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarDoCliente(Long clienteId) {
        return veiculoRepository.findByClienteIdOrderByPlaca(clienteId);
    }

    @Transactional(readOnly = true)
    public Veiculo buscar(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado"));
    }

    /** Converte a entidade para o formulário (usado na tela de edição). */
    @Transactional(readOnly = true)
    public VeiculoForm paraForm(Long id) {
        Veiculo v = buscar(id);
        VeiculoForm form = new VeiculoForm();
        form.setId(v.getId());
        form.setPlaca(v.getPlaca());
        form.setMarca(v.getMarca());
        form.setModelo(v.getModelo());
        form.setAno(v.getAno());
        form.setCor(v.getCor());
        form.setClienteId(v.getCliente().getId());
        return form;
    }

    public Veiculo salvar(VeiculoForm form) {
        // as regras abaixo valem mesmo que alguém chame o service sem passar pelo formulário
        if (form.getPlaca() == null) {
            throw new RegraNegocioException("placa", "Informe a placa");
        }
        String placa = form.getPlaca().replace("-", "").trim().toUpperCase();
        if (!PLACA.matcher(placa).matches()) {
            throw new RegraNegocioException("placa", "Placa inválida. Exemplos: ABC1234 ou ABC1D23");
        }
        if (form.getAno() == null || form.getAno() < 1950 || form.getAno() > Year.now().getValue() + 1) {
            throw new RegraNegocioException("ano",
                    "O ano deve estar entre 1950 e " + (Year.now().getValue() + 1));
        }
        if (form.getMarca() == null || form.getMarca().isBlank()) {
            throw new RegraNegocioException("marca", "Informe a marca");
        }
        if (form.getModelo() == null || form.getModelo().isBlank()) {
            throw new RegraNegocioException("modelo", "Informe o modelo");
        }
        if (form.getClienteId() == null) {
            throw new RegraNegocioException("clienteId", "Escolha o dono do veículo");
        }

        veiculoRepository.findByPlaca(placa)
                .filter(outro -> !outro.getId().equals(form.getId()))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("placa", "Já existe um veículo com esta placa");
                });

        Cliente dono = clienteRepository.findById(form.getClienteId())
                .orElseThrow(() -> new RegraNegocioException("clienteId", "Cliente não encontrado"));

        Veiculo veiculo = form.getId() == null ? new Veiculo() : buscar(form.getId());
        veiculo.setPlaca(placa);
        veiculo.setMarca(form.getMarca().trim());
        veiculo.setModelo(form.getModelo().trim());
        veiculo.setAno(form.getAno());
        veiculo.setCor(form.getCor() == null || form.getCor().isBlank() ? null : form.getCor().trim());
        veiculo.setCliente(dono);
        return veiculoRepository.save(veiculo);
    }

    public void excluir(Long id) {
        Veiculo veiculo = buscar(id);
        if (ordemRepository.existsByVeiculoId(id)) {
            throw new RegraNegocioException(
                    "O veículo " + veiculo.getPlaca() + " tem ordens de serviço e não pode ser excluído");
        }
        veiculoRepository.delete(veiculo);
    }
}
