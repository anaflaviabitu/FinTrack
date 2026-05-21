package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Transacao;
import repository.TransacaoDAO;
import utils.Formatador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainController {

    @FXML
    private Label lblSaldoTopo;

    @FXML
    private Label lblTotalReceitas;

    @FXML
    private Label lblTotalDespesas;

    @FXML
    private ComboBox<String> comboMes;

    @FXML
    private TableView<Transacao> tabelaTransacoes;

    @FXML
    private TableColumn<Transacao, LocalDate> colData;

    @FXML
    private TableColumn<Transacao, String> colDescricao;

    @FXML
    private TableColumn<Transacao, Double> colValor;

    @FXML
    private TableColumn<Transacao, String> colTipo;

    private final TransacaoDAO dao =
            new TransacaoDAO();

    @FXML
    public void initialize() {

        dao.inicializarBanco();

        configurarColunas();

        configurarFiltroMes();

        atualizarTabela();
    }

    private void configurarColunas() {

        colData.setCellValueFactory(
                new PropertyValueFactory<>("data")
        );

        colData.setCellFactory(column ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(
                            LocalDate data,
                            boolean empty
                    ) {

                        super.updateItem(data, empty);

                        if (empty || data == null) {

                            setText(null);

                        } else {

                            setText(
                                    data.format(
                                            DateTimeFormatter.ofPattern(
                                                    "dd/MM/yyyy"
                                            )
                                    )
                            );
                        }
                    }
                });

        colDescricao.setCellValueFactory(
                new PropertyValueFactory<>("descricao")
        );

        colValor.setCellValueFactory(
                new PropertyValueFactory<>("valor")
        );

        colTipo.setCellValueFactory(
                new PropertyValueFactory<>("tipo")
        );

        colTipo.setCellFactory(col ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(
                            String item,
                            boolean empty
                    ) {

                        super.updateItem(item, empty);

                        getStyleClass().removeAll(
                                "coluna-tipo-receita",
                                "coluna-tipo-despesa"
                        );

                        if (empty || item == null) {

                            setText(null);

                        } else {

                            setText(item);

                            getStyleClass().add(
                                    item.equalsIgnoreCase(
                                            "Receita"
                                    )
                                            ? "coluna-tipo-receita"
                                            : "coluna-tipo-despesa"
                            );
                        }
                    }
                });

        colValor.setCellFactory(col ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(
                            Double item,
                            boolean empty
                    ) {

                        super.updateItem(item, empty);

                        getStyleClass().removeAll(
                                "coluna-valor-positivo",
                                "coluna-valor-negativo"
                        );

                        if (empty || item == null) {

                            setText(null);

                        } else {

                            setText(
                                    Formatador.formatarValor(item)
                            );

                            Transacao t =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            getStyleClass().add(
                                    t.getTipo().equalsIgnoreCase(
                                            "Receita"
                                    )
                                            ? "coluna-valor-positivo"
                                            : "coluna-valor-negativo"
                            );
                        }
                    }
                });
    }

    private void configurarFiltroMes() {

        comboMes.getItems().addAll(
                "Todos",
                "Janeiro",
                "Fevereiro",
                "Março",
                "Abril",
                "Maio",
                "Junho",
                "Julho",
                "Agosto",
                "Setembro",
                "Outubro",
                "Novembro",
                "Dezembro"
        );

        comboMes.setValue("Todos");

        comboMes.setOnAction(e -> {

            String mesSelecionado =
                    comboMes.getValue();

            if (mesSelecionado.equals("Todos")) {

                atualizarTabela();

            } else {

                int mes =
                        comboMes.getSelectionModel()
                                .getSelectedIndex();

                List<Transacao> lista =
                        dao.listarPorMes(mes);

                tabelaTransacoes
                        .getItems()
                        .setAll(lista);

                atualizarTopo(lista);
            }
        });
    }

    @FXML
    public void abrirNovaTransacao() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/view/nova_transacao.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    new Stage();

            stage.setTitle(
                    "Nova Transação — FinTrack"
            );

            stage.setScene(
                    new Scene(root)
            );

            stage.initModality(
                    Modality.APPLICATION_MODAL
            );

            stage.setResizable(false);

            stage.showAndWait();

            atualizarTabela();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @FXML
    public void abrirRelatorio() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/view/relatorio.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    new Stage();

            stage.setTitle(
                    "Relatório — FinTrack"
            );

            stage.setScene(
                    new Scene(root)
            );

            stage.initModality(
                    Modality.APPLICATION_MODAL
            );

            stage.setResizable(false);

            stage.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @FXML
    public void removerTransacao() {

        Transacao selecionada =
                tabelaTransacoes
                        .getSelectionModel()
                        .getSelectedItem();

        if (selecionada == null) {

            mostrarAlerta(
                    "Selecione uma transação para remover."
            );

            return;
        }

        Alert confirmacao =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacao.setTitle(
                "Confirmar remoção"
        );

        confirmacao.setHeaderText(
                "Deseja realmente remover esta transação?"
        );

        confirmacao.setContentText(
                selecionada.getDescricao()
        );

        ButtonType resultado =
                confirmacao.showAndWait().orElse(
                        ButtonType.CANCEL
                );

        if (resultado == ButtonType.OK) {

            dao.remover(
                    selecionada.getId()
            );

            atualizarTabela();
        }
    }

    public void atualizarTabela() {

        List<Transacao> lista =
                dao.listarTodos();

        tabelaTransacoes
                .getItems()
                .setAll(lista);

        atualizarTopo(lista);
    }

    private void atualizarTopo(
            List<Transacao> lista
    ) {

        double receitas =
                lista.stream()
                        .filter(t ->
                                t.getTipo()
                                        .equalsIgnoreCase(
                                                "Receita"
                                        )
                        )
                        .mapToDouble(
                                Transacao::getValor
                        )
                        .sum();

        double despesas =
                lista.stream()
                        .filter(t ->
                                t.getTipo()
                                        .equalsIgnoreCase(
                                                "Despesa"
                                        )
                        )
                        .mapToDouble(
                                Transacao::getValor
                        )
                        .sum();

        double saldo =
                receitas - despesas;

        lblTotalReceitas.setText(
                Formatador.formatarValor(
                        receitas
                )
        );

        lblTotalDespesas.setText(
                Formatador.formatarValor(
                        despesas
                )
        );

        lblSaldoTopo.setText(
                Formatador.formatarValor(
                        saldo
                )
        );

        lblSaldoTopo
                .getStyleClass()
                .removeAll(
                        "topo-valor",
                        "topo-valor-positivo",
                        "topo-valor-negativo"
                );

        lblSaldoTopo
                .getStyleClass()
                .add(
                        saldo >= 0
                                ? "topo-valor-positivo"
                                : "topo-valor-negativo"
                );
    }

    private void mostrarAlerta(
            String mensagem
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle("Aviso");

        alert.setHeaderText(null);

        alert.setContentText(mensagem);

        alert.showAndWait();
    }
}