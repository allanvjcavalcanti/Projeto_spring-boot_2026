package br.com.oficina.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados do formulário de veículo. O dono chega só como id (clienteId). */
public class VeiculoForm {

    private Long id;

    @NotBlank(message = "Informe a placa")
    @Pattern(regexp = "[A-Za-z]{3}-?\\d[A-Za-z0-9]\\d{2}",
            message = "Placa inválida. Exemplos: ABC1234 ou ABC1D23")
    private String placa;

    @NotBlank(message = "Informe a marca")
    @Size(max = 50, message = "A marca pode ter no máximo 50 caracteres")
    private String marca;

    @NotBlank(message = "Informe o modelo")
    @Size(max = 50, message = "O modelo pode ter no máximo 50 caracteres")
    private String modelo;

    @NotNull(message = "Informe o ano")
    @Min(value = 1950, message = "O ano deve ser 1950 ou posterior")
    @Max(value = 2100, message = "Ano inválido")
    private Integer ano;

    @Size(max = 30, message = "A cor pode ter no máximo 30 caracteres")
    private String cor;

    @NotNull(message = "Escolha o dono do veículo")
    private Long clienteId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
