package br.com.oficina.service;

import br.com.oficina.dto.ItemForm;
import br.com.oficina.dto.OrdemForm;
import br.com.oficina.dto.VeiculoForm;
import br.com.oficina.exception.RegraNegocioException;
import br.com.oficina.model.Cliente;
import br.com.oficina.model.OrdemServico;
import br.com.oficina.model.Servico;
import br.com.oficina.model.StatusOrdem;
import br.com.oficina.model.Veiculo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testes de integração: usam o banco em memória já com os dados iniciais. Cada teste desfaz o que gravou. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RegrasDeNegocioTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private VeiculoService veiculoService;

    @Autowired
    private ServicoService servicoService;

    @Autowired
    private OrdemServicoService ordemService;

    // ---------- clientes ----------

    @Test
    void naoPermiteDoisClientesComOMesmoCpf() {
        Cliente repetido = novoCliente("Outra Pessoa", "529.982.247-25"); // já existe nos dados iniciais

        assertThrows(RegraNegocioException.class, () -> clienteService.salvar(repetido));
    }

    @Test
    void naoPermiteCpfComDigitoVerificadorInvalido() {
        assertThrows(RegraNegocioException.class,
                () -> clienteService.salvar(novoCliente("Fulano de Tal", "529.982.247-00")));
    }

    @Test
    void gravaOCpfSomenteComDigitos() {
        Cliente salvo = clienteService.salvar(novoCliente("Ana Paula Silva", "168.995.350-09"));

        assertEquals("16899535009", salvo.getCpf());
    }

    @Test
    void naoExcluiClienteQueTemVeiculos() {
        Cliente comVeiculos = veiculoService.listar().get(0).getCliente();

        assertThrows(RegraNegocioException.class, () -> clienteService.excluir(comVeiculos.getId()));
    }

    // ---------- veículos ----------

    @Test
    void naoPermiteDuasPlacasIguais() {
        Veiculo existente = veiculoService.listar().get(0);
        VeiculoForm form = novoVeiculo(existente.getPlaca().toLowerCase(), existente.getCliente().getId());

        assertThrows(RegraNegocioException.class, () -> veiculoService.salvar(form));
    }

    @Test
    void recusaPlacaForaDoPadraoMesmoSemPassarPeloFormulario() {
        Long donoId = clienteService.listar(null).get(0).getId();

        assertThrows(RegraNegocioException.class, () -> veiculoService.salvar(novoVeiculo("1234ABC", donoId)));
    }

    @Test
    void recusaAnoNoFuturo() {
        Long donoId = clienteService.listar(null).get(0).getId();
        VeiculoForm form = novoVeiculo("ZZZ9Z99", donoId);
        form.setAno(2100);

        assertThrows(RegraNegocioException.class, () -> veiculoService.salvar(form));
    }

    // ---------- serviços ----------

    @Test
    void naoPermitePrecoZeroOuNegativo() {
        Servico s = new Servico();
        s.setNome("Serviço de teste");
        s.setPreco(new BigDecimal("-5"));

        assertThrows(RegraNegocioException.class, () -> servicoService.salvar(s));
    }

    // ---------- ordens de serviço ----------

    @Test
    void naoConcluiOrdemSemServicos() {
        OrdemServico ordem = abrirOrdemEmVeiculoLivre();
        ordemService.mudarStatus(ordem.getId(), StatusOrdem.EM_ANDAMENTO);

        assertThrows(RegraNegocioException.class,
                () -> ordemService.mudarStatus(ordem.getId(), StatusOrdem.CONCLUIDA));
    }

    @Test
    void naoPulaEtapaDoFluxo() {
        OrdemServico ordem = abrirOrdemEmVeiculoLivre();

        assertThrows(RegraNegocioException.class,
                () -> ordemService.mudarStatus(ordem.getId(), StatusOrdem.CONCLUIDA));
    }

    @Test
    void ordemCanceladaNaoAceitaMaisNada() {
        OrdemServico ordem = abrirOrdemEmVeiculoLivre();
        ordemService.mudarStatus(ordem.getId(), StatusOrdem.CANCELADA);

        assertThrows(RegraNegocioException.class,
                () -> ordemService.adicionarItem(ordem.getId(), item(servicoAtivo().getId(), 1)));
        assertThrows(RegraNegocioException.class,
                () -> ordemService.mudarStatus(ordem.getId(), StatusOrdem.EM_ANDAMENTO));
    }

    @Test
    void veiculoNaoTemDuasOrdensAtivasAoMesmoTempo() {
        OrdemServico primeira = abrirOrdemEmVeiculoLivre();

        OrdemForm segunda = new OrdemForm();
        segunda.setVeiculoId(primeira.getVeiculo().getId());
        segunda.setDescricaoProblema("Segunda ordem para o mesmo carro");

        assertThrows(RegraNegocioException.class, () -> ordemService.abrir(segunda));
    }

    @Test
    void usaOPrecoDoCatalogoEOTotalBate() {
        OrdemServico ordem = abrirOrdemEmVeiculoLivre();
        Servico servico = servicoAtivo();

        ordemService.adicionarItem(ordem.getId(), item(servico.getId(), 2));

        OrdemServico recarregada = ordemService.buscar(ordem.getId());
        BigDecimal esperado = servico.getPreco().multiply(BigDecimal.valueOf(2));
        assertEquals(0, esperado.compareTo(recarregada.getTotal()));
    }

    @Test
    void somarOMesmoServicoNaoPassaDoLimiteDeUnidades() {
        OrdemServico ordem = abrirOrdemEmVeiculoLivre();
        Long servicoId = servicoAtivo().getId();

        ordemService.adicionarItem(ordem.getId(), item(servicoId, 99));

        assertThrows(RegraNegocioException.class,
                () -> ordemService.adicionarItem(ordem.getId(), item(servicoId, 1)));
    }

    @Test
    void naoAceitaQuantidadeInvalidaMesmoSemPassarPeloFormulario() {
        OrdemServico ordem = abrirOrdemEmVeiculoLivre();
        Long servicoId = servicoAtivo().getId();

        assertThrows(RegraNegocioException.class,
                () -> ordemService.adicionarItem(ordem.getId(), item(servicoId, 0)));
        assertThrows(RegraNegocioException.class,
                () -> ordemService.adicionarItem(ordem.getId(), item(servicoId, -3)));
        assertThrows(RegraNegocioException.class,
                () -> ordemService.adicionarItem(ordem.getId(), item(servicoId, 100)));
    }

    @Test
    void concluirGravaADataDeConclusao() {
        OrdemServico ordem = abrirOrdemEmVeiculoLivre();
        ordemService.adicionarItem(ordem.getId(), item(servicoAtivo().getId(), 1));
        ordemService.mudarStatus(ordem.getId(), StatusOrdem.EM_ANDAMENTO);
        ordemService.mudarStatus(ordem.getId(), StatusOrdem.CONCLUIDA);

        OrdemServico concluida = ordemService.buscar(ordem.getId());
        assertEquals(StatusOrdem.CONCLUIDA, concluida.getStatus());
        assertTrue(concluida.getDataConclusao() != null);
    }

    // ---------- apoio ----------

    private Cliente novoCliente(String nome, String cpf) {
        Cliente c = new Cliente();
        c.setNome(nome);
        c.setCpf(cpf);
        return c;
    }

    private VeiculoForm novoVeiculo(String placa, Long clienteId) {
        VeiculoForm f = new VeiculoForm();
        f.setPlaca(placa);
        f.setMarca("Marca");
        f.setModelo("Modelo");
        f.setAno(2020);
        f.setClienteId(clienteId);
        return f;
    }

    private ItemForm item(Long servicoId, int quantidade) {
        ItemForm f = new ItemForm();
        f.setServicoId(servicoId);
        f.setQuantidade(quantidade);
        return f;
    }

    private Servico servicoAtivo() {
        return servicoService.listarAtivos().get(0);
    }

    /** Cadastra um carro novo (sem ordens) e abre uma ordem nele. */
    private OrdemServico abrirOrdemEmVeiculoLivre() {
        Long donoId = clienteService.listar(null).get(0).getId();
        Veiculo veiculo = veiculoService.salvar(novoVeiculo("TST1A23", donoId));

        OrdemForm form = new OrdemForm();
        form.setVeiculoId(veiculo.getId());
        form.setDescricaoProblema("Problema de teste");
        return ordemService.abrir(form);
    }
}
