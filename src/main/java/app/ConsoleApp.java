package app;

import exceptions.EntradaInvalidaException;
import model.Transacao;
import repository.TransacaoDAO;
import service.FinTracker;
import utils.Conexao;
import utils.Formatador;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class ConsoleApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static FinTracker tracker;

    public static void main(String[] args) {
        TransacaoDAO dao = new TransacaoDAO();
        dao.inicializarBanco();
        tracker = new FinTracker(dao);

        int opcao = -1;
        do {
            exibirMenu();
            opcao = lerInt("Opção");
            switch (opcao) {
                case 1 -> cadastrarTransacao("Receita");
                case 2 -> cadastrarTransacao("Despesa");
                case 3 -> tracker.listarTransacoes();
                case 4 -> exibirSaldo();
                case 5 -> removerTransacao();
                case 0 -> System.out.println("Encerrando FinTrack. Até logo!");
                default -> System.out.println("Opção inválida.");
            }
        } while (opcao != 0);

        Conexao.fechar();
    }

    private static void exibirMenu() {
        System.out.println("""

                ╔══════════════════════════════╗
                ║       FinTrack Console        ║
                ╠══════════════════════════════╣
                ║  1. Cadastrar Receita         ║
                ║  2. Cadastrar Despesa         ║
                ║  3. Listar Transações         ║
                ║  4. Exibir Saldo              ║
                ║  5. Remover Transação         ║
                ║  0. Sair                      ║
                ╚══════════════════════════════╝""");
    }

    private static void cadastrarTransacao(String tipo) {
        try {
            System.out.print("Descrição: ");
            String descricao = scanner.nextLine().trim();

            double valor = lerDouble("Valor (ex: 150.00)");

            System.out.print("Data (AAAA-MM-DD) [Enter = hoje]: ");
            String dataStr = scanner.nextLine().trim();
            LocalDate data = dataStr.isEmpty()
                    ? LocalDate.now()
                    : LocalDate.parse(dataStr);

            Transacao t = new Transacao(descricao, valor, tipo, data);
            tracker.adicionarTransacao(t);

        } catch (IllegalArgumentException e) {
            System.out.println("✘ " + e.getMessage());
        } catch (DateTimeParseException e) {
            System.out.println("✘ Data inválida. Use o formato AAAA-MM-DD.");
        }
    }

    private static void exibirSaldo() {
        double saldo = tracker.calcularSaldoTotal();
        System.out.printf("%nSaldo atual: %s%n", Formatador.formatarValor(saldo));
    }

    private static void removerTransacao() {
        tracker.listarTransacoes();
        int id = lerInt("ID da transação a remover");
        try {
            tracker.removerTransacao(id);
        } catch (EntradaInvalidaException e) {
            System.out.println("✘ " + e.getMessage());
        }
    }

    private static int lerInt(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro.");
            }
        }
    }

    private static double lerDouble(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            try {
                double v = Double.parseDouble(
                        scanner.nextLine().trim().replace(",", "."));
                if (v > 0) return v;
                System.out.println("O valor deve ser positivo.");
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido.");
            }
        }
    }
}