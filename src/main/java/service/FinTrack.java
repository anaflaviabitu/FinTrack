package service;

import exceptions.EntradaInvalidaException;
import model.Transacao;
import repository.TransacaoDAO;
import utils.Formatador;

import java.util.List;

public class FinTrack {

    private final TransacaoDAO dao;

    public FinTrack(TransacaoDAO dao) {
        this.dao = dao;
    }

    public void adicionarTransacao(Transacao transacao) {
        dao.salvar(transacao);
        System.out.println("Transação adicionada com sucesso! ID=" + transacao.getId());
    }

    public void listarTransacoes() {
        List<Transacao> lista = dao.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("Nenhuma transação cadastrada.");
            return;
        }
        lista.forEach(System.out::println);
    }

    public void removerTransacao(int id) throws EntradaInvalidaException {
        boolean existe = dao.listarTodos().stream()
                .anyMatch(t -> t.getId() == id);
        if (!existe)
            throw new EntradaInvalidaException("ID não encontrado: " + id);
        dao.remover(id);
        System.out.println("Transação removida com sucesso!");
    }

    public double calcularSaldoTotal() {
        return dao.calcularSaldo();
    }

    public void mostrarResumoFinanceiro() {
        List<Transacao> lista = dao.listarTodos();

        double totalReceitas = lista.stream()
                .filter(t -> t.getTipo().equalsIgnoreCase("Receita"))
                .mapToDouble(Transacao::getValor).sum();

        double totalDespesas = lista.stream()
                .filter(t -> t.getTipo().equalsIgnoreCase("Despesa"))
                .mapToDouble(Transacao::getValor).sum();

        System.out.println("====== RESUMO FINANCEIRO ======");
        System.out.println("Total Receitas : " + Formatador.formatarValor(totalReceitas));
        System.out.println("Total Despesas : " + Formatador.formatarValor(totalDespesas));
        System.out.println("Saldo Final    : " +
                Formatador.formatarValor(totalReceitas - totalDespesas));
    }
}