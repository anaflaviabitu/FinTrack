package controller;

import javafx.collections.FXCollections;

import javafx.fxml.FXML;

import javafx.scene.chart.PieChart;

import javafx.scene.control.*;

import javafx.scene.control.cell.PropertyValueFactory;

import javafx.stage.Stage;

import model.Transacao;

import repository.TransacaoDAO;

import utils.Formatador;

import java.time.LocalDate;

import java.time.format.DateTimeFormatter;

import java.util.List;

public class RelatorioController {

    @FXML
    private Label lblReceitas;

    @FXML
    private Label lblDespesas;

    @FXML
    private Label lblSaldo;

    @FXML
    private Label lblTituloPeriodo;

    @FXML
    private ComboBox<String> comboMes;

    @FXML
    private TableView<Transacao> tabelaRelatorio;

    @FXML
    private TableColumn<Transacao, LocalDate> colData;

    @FXML
    private TableColumn<Transacao, String> colDescricao;

    @FXML
    private TableColumn<Transacao, Double> colValor;

    @FXML
    private TableColumn<Transacao, String> colTipo;

    @FXML
    private PieChart graficoFinanceiro;

    @FXML
    private Button btnFechar;

    private final TransacaoDAO dao =
            new TransacaoDAO();

    @FXML
    public void initialize() {

        configurarTabela();

        configurarFiltroMes();

        carregarRelatorioCompleto();
    }

    private void configurarTabela() {



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

        // VALOR

        colValor.setCellValueFactory(
                new PropertyValueFactory<>("valor")
        );

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

        comboMes.setOnAction(event -> {

            String mesSelecionado =
                    comboMes.getValue();

            if (mesSelecionado.equals("Todos")) {

                carregarRelatorioCompleto();

                lblTituloPeriodo.setText(
                        "Relatório Financeiro Completo"
                );

                return;
            }

            int numeroMes =
                    comboMes.getSelectionModel()
                            .getSelectedIndex();

            List<Transacao> lista =
                    dao.listarPorMes(numeroMes);

            tabelaRelatorio.setItems(
                    FXCollections.observableArrayList(
                            lista
                    )
            );

            atualizarResumo(lista);

            lblTituloPeriodo.setText(
                    "Relatório — " + mesSelecionado
            );
        });
    }

    private void carregarRelatorioCompleto() {

        List<Transacao> lista =
                dao.listarTodos();

        tabelaRelatorio.getItems().setAll(
                lista
        );

        atualizarResumo(lista);
    }

    private void atualizarResumo(
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

        lblReceitas.setText(
                Formatador.formatarValor(
                        receitas
                )
        );

        lblDespesas.setText(
                Formatador.formatarValor(
                        despesas
                )
        );

        lblSaldo.setText(
                Formatador.formatarValor(
                        saldo
                )
        );

        lblSaldo.getStyleClass()
                .removeAll(

                        "topo-valor",

                        "topo-valor-positivo",

                        "topo-valor-negativo"
                );

        lblSaldo.getStyleClass()
                .add(

                        saldo >= 0

                                ? "topo-valor-positivo"

                                : "topo-valor-negativo"
                );

        atualizarGrafico(
                receitas,
                despesas
        );
    }

    private void atualizarGrafico(
            double receitas,
            double despesas
    ) {

        graficoFinanceiro.setData(

                FXCollections.observableArrayList(

                        new PieChart.Data(
                                "Receitas",
                                receitas
                        ),

                        new PieChart.Data(
                                "Despesas",
                                despesas
                        )
                )
        );

        graficoFinanceiro.setLegendVisible(true);

        graficoFinanceiro.setLabelsVisible(true);
    }

    @FXML
    public void fechar() {

        ((Stage)
                btnFechar
                        .getScene()
                        .getWindow()
        ).close();
    }

    @FXML
    public void confirmarRemocao() {

        Transacao selecionada =
                tabelaRelatorio
                        .getSelectionModel()
                        .getSelectedItem();

        if (selecionada == null) {

            Alert alerta =
                    new Alert(
                            Alert.AlertType.WARNING
                    );

            alerta.setTitle("Aviso");

            alerta.setHeaderText(null);

            alerta.setContentText(
                    "Selecione uma transação."
            );

            alerta.showAndWait();

            return;
        }

        Alert confirmacao =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacao.setTitle(
                "Confirmar Remoção"
        );

        confirmacao.setHeaderText(null);

        confirmacao.setContentText(
                "Deseja realmente remover esta transação?"
        );

        confirmacao.showAndWait()
                .ifPresent(resposta -> {

                    if (resposta == ButtonType.OK) {

                        dao.remover(
                                selecionada.getId()
                        );

                        carregarRelatorioCompleto();
                    }
                });
    }
}