package br.com.oficina.model;

import java.util.List;

/**
 * Ciclo de vida de uma ordem de serviço.
 * ABERTA -> EM_ANDAMENTO -> CONCLUIDA, e CANCELADA pode acontecer antes de concluir.
 */
public enum StatusOrdem {

    ABERTA("Aberta", "Reabrir"),
    EM_ANDAMENTO("Em andamento", "Iniciar atendimento"),
    CONCLUIDA("Concluída", "Concluir ordem"),
    CANCELADA("Cancelada", "Cancelar ordem");

    private final String rotulo;
    private final String acao;

    StatusOrdem(String rotulo, String acao) {
        this.rotulo = rotulo;
        this.acao = acao;
    }

    public String getRotulo() {
        return rotulo;
    }

    /** Texto do botão que leva a ordem para este status. */
    public String getAcao() {
        return acao;
    }

    public List<StatusOrdem> proximos() {
        return switch (this) {
            case ABERTA -> List.of(EM_ANDAMENTO, CANCELADA);
            case EM_ANDAMENTO -> List.of(CONCLUIDA, CANCELADA);
            case CONCLUIDA, CANCELADA -> List.of();
        };
    }

    public boolean podeIrPara(StatusOrdem destino) {
        return proximos().contains(destino);
    }

    /** Só dá para mexer nos itens enquanto a ordem não foi encerrada. */
    public boolean isEditavel() {
        return this == ABERTA || this == EM_ANDAMENTO;
    }
}
