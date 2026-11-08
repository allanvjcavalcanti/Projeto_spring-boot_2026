package br.com.oficina.model;

/** Perfis de acesso. GERENTE pode tudo; ATENDENTE não mexe no catálogo, não exclui e não cancela ordens. */
public enum Perfil {
    GERENTE,
    ATENDENTE
}
