package br.com.oficina.config;

import br.com.oficina.dto.ItemForm;
import br.com.oficina.dto.OrdemForm;
import br.com.oficina.dto.VeiculoForm;
import br.com.oficina.model.Cliente;
import br.com.oficina.model.OrdemServico;
import br.com.oficina.model.Perfil;
import br.com.oficina.model.Servico;
import br.com.oficina.model.StatusOrdem;
import br.com.oficina.model.Usuario;
import br.com.oficina.model.Veiculo;
import br.com.oficina.repository.ClienteRepository;
import br.com.oficina.repository.UsuarioRepository;
import br.com.oficina.service.ClienteService;
import br.com.oficina.service.OrdemServicoService;
import br.com.oficina.service.ServicoService;
import br.com.oficina.service.VeiculoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Na primeira execução cria os usuários de acesso e popula o banco com dados de exemplo,
 * para a aplicação não abrir vazia na apresentação. Se os dados já existem, não faz nada.
 * Passa pelos services de propósito: assim as regras de negócio também rodam.
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String senhaGerente;
    private final String senhaAtendente;
    private final ClienteService clienteService;
    private final VeiculoService veiculoService;
    private final ServicoService servicoService;
    private final OrdemServicoService ordemService;

    public DadosIniciais(ClienteRepository clienteRepository,
                         UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder,
                         @Value("${oficina.seguranca.senha-gerente}") String senhaGerente,
                         @Value("${oficina.seguranca.senha-atendente}") String senhaAtendente,
                         ClienteService clienteService,
                         VeiculoService veiculoService,
                         ServicoService servicoService,
                         OrdemServicoService ordemService) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.senhaGerente = senhaGerente;
        this.senhaAtendente = senhaAtendente;
        this.clienteService = clienteService;
        this.veiculoService = veiculoService;
        this.servicoService = servicoService;
        this.ordemService = ordemService;
    }

    @Override
    public void run(String... args) {
        criarUsuarios();

        if (clienteRepository.count() > 0) {
            return;
        }

        Cliente carlos = cliente("Carlos Eduardo Lima", "529.982.247-25", "(81) 99123-4567", "carlos.lima@email.com");
        Cliente mariana = cliente("Mariana Souza", "111.444.777-35", "(81) 98765-4321", "mariana.souza@email.com");
        Cliente joao = cliente("João Pedro Albuquerque", "390.533.447-05", "(81) 99988-7766", null);

        Veiculo onix = veiculo("PGT2B45", "Chevrolet", "Onix", 2019, "Prata", carlos);
        Veiculo civic = veiculo("KLM8H21", "Honda", "Civic", 2016, "Preto", carlos);
        Veiculo hb20 = veiculo("RTY4C67", "Hyundai", "HB20", 2021, "Branco", mariana);
        Veiculo fiesta = veiculo("NEX9087", "Ford", "Fiesta", 2014, "Vermelho", joao);

        Servico oleo = servico("Troca de óleo e filtro", "Óleo sintético 5W30 e filtro de óleo", "129.90");
        Servico alinhamento = servico("Alinhamento e balanceamento", "Das quatro rodas", "120.00");
        Servico freios = servico("Revisão de freios", "Inspeção de discos, tambores e fluido", "189.90");
        Servico pastilhas = servico("Troca de pastilhas de freio", "Par dianteiro, mão de obra inclusa", "160.00");
        Servico scanner = servico("Diagnóstico com scanner", "Leitura de falhas da injeção eletrônica", "90.00");
        Servico arCondicionado = servico("Higienização do ar-condicionado", "Limpeza do sistema e troca do filtro de cabine", "110.00");
        servico("Troca de correia dentada", "Correia, tensor e mão de obra", "420.00");

        // ordem concluída (faturamento do painel)
        OrdemServico os1 = abrir(onix, "Barulho ao frear e pedal baixo");
        item(os1, freios, 1);
        item(os1, pastilhas, 1);
        ordemService.mudarStatus(os1.getId(), StatusOrdem.EM_ANDAMENTO);
        ordemService.mudarStatus(os1.getId(), StatusOrdem.CONCLUIDA);

        OrdemServico os2 = abrir(hb20, "Revisão dos 30 mil km");
        item(os2, oleo, 1);
        item(os2, alinhamento, 1);
        ordemService.mudarStatus(os2.getId(), StatusOrdem.EM_ANDAMENTO);
        ordemService.mudarStatus(os2.getId(), StatusOrdem.CONCLUIDA);

        // ordem em andamento
        OrdemServico os3 = abrir(fiesta, "Luz da injeção acesa e motor falhando");
        item(os3, scanner, 1);
        ordemService.mudarStatus(os3.getId(), StatusOrdem.EM_ANDAMENTO);

        // ordem aberta
        OrdemServico os4 = abrir(civic, "Ar-condicionado não está gelando");
        item(os4, arCondicionado, 1);

        // ordem cancelada
        OrdemServico os5 = abrir(hb20, "Orçamento de troca de pneus");
        ordemService.mudarStatus(os5.getId(), StatusOrdem.CANCELADA);
    }

    private void criarUsuarios() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        usuarioRepository.save(new Usuario("gerente", "Gerente da oficina",
                passwordEncoder.encode(senhaGerente), Perfil.GERENTE));
        usuarioRepository.save(new Usuario("atendente", "Atendente",
                passwordEncoder.encode(senhaAtendente), Perfil.ATENDENTE));
    }

    private Cliente cliente(String nome, String cpf, String telefone, String email) {
        Cliente c = new Cliente();
        c.setNome(nome);
        c.setCpf(cpf);
        c.setTelefone(telefone);
        c.setEmail(email);
        return clienteService.salvar(c);
    }

    private Veiculo veiculo(String placa, String marca, String modelo, int ano, String cor, Cliente dono) {
        VeiculoForm f = new VeiculoForm();
        f.setPlaca(placa);
        f.setMarca(marca);
        f.setModelo(modelo);
        f.setAno(ano);
        f.setCor(cor);
        f.setClienteId(dono.getId());
        return veiculoService.salvar(f);
    }

    private Servico servico(String nome, String descricao, String preco) {
        Servico s = new Servico();
        s.setNome(nome);
        s.setDescricao(descricao);
        s.setPreco(new BigDecimal(preco));
        return servicoService.salvar(s);
    }

    private OrdemServico abrir(Veiculo veiculo, String problema) {
        OrdemForm f = new OrdemForm();
        f.setVeiculoId(veiculo.getId());
        f.setDescricaoProblema(problema);
        return ordemService.abrir(f);
    }

    private void item(OrdemServico ordem, Servico servico, int quantidade) {
        ItemForm f = new ItemForm();
        f.setServicoId(servico.getId());
        f.setQuantidade(quantidade);
        ordemService.adicionarItem(ordem.getId(), f);
    }
}
