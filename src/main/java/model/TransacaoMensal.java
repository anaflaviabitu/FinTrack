package model;

import java.time.LocalDate;

public class TransacaoMensal extends Transacao {

    public TransacaoMensal(String descricao,
                           double valor,
                           String tipo,
                           LocalDate data) {

        super(descricao, valor, tipo, data);
    }

    @Override
    public String toString() {

        return super.toString() + " | Mensal";
    }
}