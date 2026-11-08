package br.com.oficina.dto;

/** Linha do ranking "serviços mais pedidos" do painel. */
public class ServicoRanking {

    private final String nome;
    private final Long quantidade;

    public ServicoRanking(String nome, Long quantidade) {
        this.nome = nome;
        this.quantidade = quantidade;
    }

    public String getNome() {
        return nome;
    }

    public Long getQuantidade() {
        return quantidade;
    }
}
