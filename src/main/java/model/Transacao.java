package model;

import java.time.LocalDate;

public class Transacao {

    private String descricao;

    private double valor;

    private String tipo;

    private LocalDate data;

    private int id;

    public Transacao(
            String descricao,
            double valor,
            String tipo,
            LocalDate data
    ) {

        if (valor <= 0) {

            throw new IllegalArgumentException(
                    "O valor deve ser maior que zero."
            );
        }

        if (
                !tipo.equalsIgnoreCase("Receita")
                        &&
                        !tipo.equalsIgnoreCase("Despesa")
        ) {

            throw new IllegalArgumentException(
                    "Tipo inválido."
            );
        }

        this.descricao = descricao;

        this.valor = valor;

        this.tipo = tipo;

        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getValor() {
        return valor;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDate getData() {
        return data;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {

        return descricao +
                " - R$ " +
                valor;
    }
}