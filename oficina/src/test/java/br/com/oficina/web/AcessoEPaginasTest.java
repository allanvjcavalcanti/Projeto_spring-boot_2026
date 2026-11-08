package br.com.oficina.web;

import br.com.oficina.model.OrdemServico;
import br.com.oficina.model.StatusOrdem;
import br.com.oficina.model.Veiculo;
import br.com.oficina.seguranca.ControleTentativas;
import br.com.oficina.service.ClienteService;
import br.com.oficina.service.OrdemServicoService;
import br.com.oficina.service.ServicoService;
import br.com.oficina.service.VeiculoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa a aplicação "por fora", como o navegador: login, permissões, CSRF, validação dos formulários
 * e se todas as páginas abrem sem erro (o que também confere os templates Thymeleaf).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AcessoEPaginasTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private VeiculoService veiculoService;

    @Autowired
    private ServicoService servicoService;

    @Autowired
    private OrdemServicoService ordemService;

    @Autowired
    private ControleTentativas controleTentativas;

    // ---------- login ----------

    @Test
    void visitanteSemLoginVaiParaATelaDeLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/clientes")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/ordens/1")).andExpect(status().is3xxRedirection());
    }

    @Test
    void telaDeLoginAbreSemEstarLogado() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().string(containsString("Entrar")));
    }

    @Test
    void gerenteEAtendenteConseguemEntrarComAsSenhasIniciais() throws Exception {
        mvc.perform(formLogin().user("gerente").password("Gerente-2026"))
                .andExpect(authenticated().withUsername("gerente"));
        mvc.perform(formLogin().user("atendente").password("Atendente-2026"))
                .andExpect(authenticated().withUsername("atendente"));
    }

    @Test
    void senhaErradaNaoEntra() throws Exception {
        mvc.perform(formLogin().user("gerente").password("senha-errada"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?erro"));
    }

    @Test
    void depoisDeCincoErrosAteASenhaCertaFicaBloqueada() throws Exception {
        try {
            for (int i = 0; i < 5; i++) {
                mvc.perform(formLogin().user("atendente").password("errada" + i));
            }
            mvc.perform(formLogin().user("atendente").password("Atendente-2026"))
                    .andExpect(unauthenticated());
        } finally {
            controleTentativas.limpar("atendente");
        }
    }

    // ---------- todas as páginas abrem ----------

    @Test
    @WithMockUser(username = "gerente", roles = "GERENTE")
    void gerenteAbreTodasAsPaginas() throws Exception {
        Veiculo veiculo = veiculoService.listar().get(0);
        Long clienteId = veiculo.getCliente().getId();
        Long servicoId = servicoService.listar().get(0).getId();
        Long ordemId = ordemService.listar(null).get(0).getId();

        String[] urls = {
                "/", "/clientes", "/clientes?busca=maria", "/clientes/novo",
                "/clientes/" + clienteId, "/clientes/" + clienteId + "/editar",
                "/veiculos", "/veiculos/novo", "/veiculos/novo?clienteId=" + clienteId,
                "/veiculos/" + veiculo.getId(), "/veiculos/" + veiculo.getId() + "/editar",
                "/servicos", "/servicos/novo", "/servicos/" + servicoId + "/editar",
                "/ordens", "/ordens?status=ABERTA", "/ordens?status=CONCLUIDA", "/ordens/nova",
                "/ordens/nova?veiculoId=" + veiculo.getId(), "/ordens/" + ordemId
        };
        for (String url : urls) {
            mvc.perform(get(url)).andExpect(status().isOk());
        }
    }

    @Test
    @WithMockUser(username = "atendente", roles = "ATENDENTE")
    void atendenteAbreAsPaginasDoDiaADia() throws Exception {
        Long ordemId = ordemService.listar(null).get(0).getId();

        for (String url : new String[]{"/", "/clientes", "/veiculos", "/ordens", "/ordens/nova", "/ordens/" + ordemId}) {
            mvc.perform(get(url)).andExpect(status().isOk());
        }
    }

    @Test
    @WithMockUser(username = "gerente", roles = "GERENTE")
    void paginaInexistenteMostraOErro404() throws Exception {
        mvc.perform(get("/clientes/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/ordens/999999")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "gerente", roles = "GERENTE")
    void valorInvalidoNaUrlNaoDaErro500() throws Exception {
        mvc.perform(get("/clientes/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/ordens?status=INVENTADO")).andExpect(status().isBadRequest());
    }

    // ---------- permissões ----------

    @Test
    @WithMockUser(username = "atendente", roles = "ATENDENTE")
    void atendenteNaoAcessaOCatalogoDeServicos() throws Exception {
        mvc.perform(get("/servicos")).andExpect(status().isForbidden());
        mvc.perform(get("/servicos/novo")).andExpect(status().isForbidden());
        mvc.perform(post("/servicos").with(csrf()).param("nome", "Serviço pirata").param("preco", "1.00"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "atendente", roles = "ATENDENTE")
    void atendenteNaoExcluiNada() throws Exception {
        Long clienteId = clienteService.listar(null).get(0).getId();
        Long veiculoId = veiculoService.listar().get(0).getId();

        mvc.perform(post("/clientes/" + clienteId + "/excluir").with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/veiculos/" + veiculoId + "/excluir").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "atendente", roles = "ATENDENTE")
    void atendenteNaoCancelaOrdemNemPeloEnderecoDireto() throws Exception {
        Long ordemId = ordemService.listar(StatusOrdem.ABERTA).get(0).getId();

        // rota de cancelar: bloqueada pelo perfil
        mvc.perform(post("/ordens/" + ordemId + "/cancelar").with(csrf())).andExpect(status().isForbidden());

        // tentando cancelar pela rota de status: o controller recusa
        mvc.perform(post("/ordens/" + ordemId + "/status").with(csrf()).param("novo", "CANCELADA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("erro"));
        org.junit.jupiter.api.Assertions.assertEquals(StatusOrdem.ABERTA, ordemService.buscar(ordemId).getStatus());
    }

    @Test
    @WithMockUser(username = "gerente", roles = "GERENTE")
    void gerenteCancelaOrdem() throws Exception {
        OrdemServico aberta = ordemService.listar(StatusOrdem.ABERTA).get(0);

        mvc.perform(post("/ordens/" + aberta.getId() + "/cancelar").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("sucesso"));
        org.junit.jupiter.api.Assertions.assertEquals(StatusOrdem.CANCELADA, ordemService.buscar(aberta.getId()).getStatus());
    }

    // ---------- CSRF ----------

    @Test
    @WithMockUser(username = "gerente", roles = "GERENTE")
    void postSemTokenCsrfEhRecusado() throws Exception {
        mvc.perform(post("/clientes").param("nome", "Fulano de Tal").param("cpf", "529.982.247-25"))
                .andExpect(status().isForbidden());
    }

    // ---------- validação dos formulários ----------

    @Test
    @WithMockUser(username = "gerente", roles = "GERENTE")
    void clienteComCpfInvalidoVoltaParaOFormularioComMensagem() throws Exception {
        mvc.perform(post("/clientes").with(csrf())
                        .param("nome", "Fulano de Tal").param("cpf", "529.982.247-00"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CPF inv")));
    }

    @Test
    @WithMockUser(username = "gerente", roles = "GERENTE")
    void clienteComDadosVaziosMostraOsErros() throws Exception {
        mvc.perform(post("/clientes").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Informe o nome")))
                .andExpect(content().string(containsString("Informe o CPF")));
    }

    @Test
    @WithMockUser(username = "gerente", roles = "GERENTE")
    void clienteValidoEhGravadoEVaiParaADescricao() throws Exception {
        mvc.perform(post("/clientes").with(csrf())
                        .param("nome", "  Beatriz Moreira  ").param("cpf", "168.995.350-09")
                        .param("telefone", "(81) 98888-7777").param("email", "bia@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/clientes/*"));
    }

    @Test
    @WithMockUser(username = "gerente", roles = "GERENTE")
    void campoExtraEnviadoPorForaDoFormularioEhIgnorado() throws Exception {
        // "veiculos[0].placa" não existe no formulário: a lista de campos permitidos descarta
        mvc.perform(post("/clientes").with(csrf())
                        .param("nome", "Carla Dias").param("cpf", "168.995.350-09")
                        .param("veiculos[0].placa", "HAK0000"))
                .andExpect(status().is3xxRedirection());
        org.junit.jupiter.api.Assertions.assertTrue(veiculoService.listar().stream()
                .noneMatch(v -> "HAK0000".equals(v.getPlaca())));
    }

    @Test
    @WithMockUser(username = "gerente", roles = "GERENTE")
    void letrasNoCampoDePrecoViramMensagemEmPortugues() throws Exception {
        mvc.perform(post("/servicos").with(csrf())
                        .param("nome", "Serviço novo").param("preco", "abc"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("valor num")));
    }

    @Test
    @WithMockUser(username = "atendente", roles = "ATENDENTE")
    void quantidadeNegativaNoItemDaOrdemEhRecusada() throws Exception {
        Long ordemId = ordemService.listar(StatusOrdem.ABERTA).get(0).getId();
        Long servicoId = servicoService.listarAtivos().get(0).getId();

        mvc.perform(post("/ordens/" + ordemId + "/itens").with(csrf())
                        .param("servicoId", String.valueOf(servicoId)).param("quantidade", "-5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("quantidade m")));
    }

    @Test
    @WithMockUser(username = "gerente", roles = "GERENTE")
    void naoExcluiClienteComVeiculosPelaTela() throws Exception {
        Long clienteId = veiculoService.listar().get(0).getCliente().getId();

        mvc.perform(post("/clientes/" + clienteId + "/excluir").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("erro"));
    }
}
