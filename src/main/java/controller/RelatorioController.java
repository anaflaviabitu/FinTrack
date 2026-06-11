package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Transacao;
import repository.TransacaoDAO;
import utils.Formatador;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class RelatorioController {

    @FXML private Label lblReceitas;
    @FXML private Label lblDespesas;
    @FXML private Label lblSaldo;
    @FXML private ComboBox<String> comboMes;
    @FXML private TableView<Transacao> tabelaRelatorio;
    @FXML private TableColumn<Transacao, LocalDate> colData;
    @FXML private TableColumn<Transacao, String> colDescricao;
    @FXML private TableColumn<Transacao, Double> colValor;
    @FXML private TableColumn<Transacao, String> colTipo;
    @FXML private PieChart graficoFinanceiro;
    @FXML private Button btnFechar;
    @FXML private Label lblResumoReceita;
    @FXML private Label lblResumoDespesa;
    @FXML private Label lblResumoSaldo;
    @FXML private Hyperlink linkUltimosLancamentos;  // declarado UMA única vez

    private final TransacaoDAO dao = new TransacaoDAO();
    private PieChart.Data receitaData;
    private PieChart.Data despesaData;
    private boolean mostrandoUltimos = false;

    @FXML
    public void initialize() {
        configurarTabela();
        tabelaRelatorio.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        configurarFiltroMes();

        tabelaRelatorio.setFixedCellSize(44);

        VBox emptyBox = new VBox(10);
        emptyBox.setAlignment(Pos.CENTER);
        Label icon = new Label("\uD83D\uDCCA");
        icon.setStyle("-fx-font-size: 40px;");
        Label texto = new Label("Nenhuma movimentação encontrada");
        texto.getStyleClass().add("label-muted");
        emptyBox.getChildren().addAll(icon, texto);
        tabelaRelatorio.setPlaceholder(emptyBox);

        graficoFinanceiro.setLegendVisible(true);
        graficoFinanceiro.setLabelsVisible(true);
        graficoFinanceiro.setClockwise(true);
        graficoFinanceiro.setPrefSize(400, 260);
        graficoFinanceiro.setMinSize(400, 260);

        Platform.runLater(() -> {
            carregarRelatorioCompleto();

            FadeTransition fade = new FadeTransition(Duration.seconds(0.7), tabelaRelatorio);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }

    private void configurarTabela() {

        // DATA
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colData.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate data, boolean empty) {
                super.updateItem(data, empty);
                if (empty || data == null) {
                    setText(null);
                } else {
                    setText(data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });


        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));


        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colValor.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeIf(c ->
                        c.equals("coluna-valor-positivo") || c.equals("coluna-valor-negativo")
                );
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(Formatador.formatarValor(item));
                    if (getTableView() != null && getIndex() < getTableView().getItems().size()) {
                        Transacao t = getTableView().getItems().get(getIndex());
                        if (t != null && t.getTipo() != null) {
                            getStyleClass().add(
                                    t.getTipo().equalsIgnoreCase("Receita")
                                            ? "coluna-valor-positivo"
                                            : "coluna-valor-negativo"
                            );
                        }
                    }
                }
            }
        });


        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colTipo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("coluna-tipo-receita", "coluna-tipo-despesa");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add(
                            item.equalsIgnoreCase("Receita")
                                    ? "coluna-tipo-receita"
                                    : "coluna-tipo-despesa"
                    );
                }
            }
        });
    }

    private void configurarFiltroMes() {
        comboMes.getItems().addAll(
                "Todos", "Janeiro", "Fevereiro", "Março", "Abril",
                "Maio", "Junho", "Julho", "Agosto", "Setembro",
                "Outubro", "Novembro", "Dezembro"
        );
        comboMes.setValue("Todos");
        comboMes.setOnAction(event -> {
            String mesSelecionado = comboMes.getValue();
            if (mesSelecionado == null || mesSelecionado.equals("Todos")) {
                carregarRelatorioCompleto();
                return;
            }
            int numeroMes = comboMes.getSelectionModel().getSelectedIndex();
            List<Transacao> lista = dao.listarPorMes(numeroMes);
            tabelaRelatorio.setItems(FXCollections.observableArrayList(lista));
            atualizarResumo(lista);
        });
    }

    private void carregarRelatorioCompleto() {
        List<Transacao> lista = dao.listarTodos();
        tabelaRelatorio.setItems(FXCollections.observableArrayList(lista));
        tabelaRelatorio.refresh();
        tabelaRelatorio.setOpacity(1.0);
        atualizarResumo(lista);
    }

    private void atualizarResumo(List<Transacao> lista) {
        double receitas = lista.stream()
                .filter(t -> t.getTipo().equalsIgnoreCase("Receita"))
                .mapToDouble(Transacao::getValor)
                .sum();

        double despesas = lista.stream()
                .filter(t -> t.getTipo().equalsIgnoreCase("Despesa"))
                .mapToDouble(Transacao::getValor)
                .sum();

        double saldo = receitas - despesas;

        lblReceitas.setText(Formatador.formatarValor(receitas));
        lblDespesas.setText(Formatador.formatarValor(despesas));
        lblSaldo.setText(Formatador.formatarValor(saldo));

        lblSaldo.getStyleClass().removeAll("topo-valor", "topo-valor-positivo", "topo-valor-negativo");
        lblSaldo.getStyleClass().add(saldo >= 0 ? "topo-valor-positivo" : "topo-valor-negativo");

        lblResumoReceita.setText(Formatador.formatarValor(receitas));
        lblResumoDespesa.setText(Formatador.formatarValor(despesas));
        lblResumoSaldo.setText(Formatador.formatarValor(saldo));

        atualizarGrafico(receitas, despesas);
    }

    private void atualizarGrafico(double receitas, double despesas) {
        if (receitas == 0 && despesas == 0) {
            graficoFinanceiro.setData(FXCollections.observableArrayList());
            receitaData = null;
            despesaData = null;
            return;
        }

        if (receitaData == null || despesaData == null) {
            receitaData = new PieChart.Data("Receitas", receitas);
            despesaData = new PieChart.Data("Despesas", despesas);

            graficoFinanceiro.setData(
                    FXCollections.observableArrayList(receitaData, despesaData)
            );
            graficoFinanceiro.setStartAngle(90);
            graficoFinanceiro.setLegendVisible(true);
            graficoFinanceiro.setLabelsVisible(true);

            Platform.runLater(() -> {
                receitaData.getNode().setStyle("-fx-pie-color: #34E89E;");
                despesaData.getNode().setStyle("-fx-pie-color: #FF6B8A;");

                int i = 0;
                for (Node node : graficoFinanceiro.lookupAll(".chart-legend-item-symbol")) {
                    if (i == 0) node.setStyle("-fx-background-color: #34E89E;");
                    else if (i == 1) node.setStyle("-fx-background-color: #FF6B8A;");
                    i++;
                }
            });
        } else {
            receitaData.setPieValue(receitas);
            despesaData.setPieValue(despesas);
        }
    }

    @FXML
    public void filtrarUltimosLancamentos() {
        if (mostrandoUltimos) {
            mostrandoUltimos = false;
            linkUltimosLancamentos.setText("Últimos Lançamentos");
            carregarRelatorioCompleto();
        } else {
            mostrandoUltimos = true;
            linkUltimosLancamentos.setText("Ver todos");

            List<Transacao> ultimos = dao.listarTodos().stream()
                    .sorted(Comparator.comparing(Transacao::getData).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

            tabelaRelatorio.setItems(FXCollections.observableArrayList(ultimos));
            tabelaRelatorio.refresh();
            tabelaRelatorio.setOpacity(1.0);
            atualizarResumo(ultimos);
        }
    }

    @FXML
    public void fecharRelatorio() {
        Stage stage = (Stage) btnFechar.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void confirmarRemocao() {
        Transacao selecionada = tabelaRelatorio.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Aviso");
            alerta.setHeaderText(null);
            alerta.setContentText("Selecione uma transação.");
            alerta.showAndWait();
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Remoção");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja realmente remover esta transação?");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    dao.remover(selecionada.getId());
                    carregarRelatorioCompleto();
                } catch (Exception e) {
                    Alert erroAlerta = new Alert(Alert.AlertType.ERROR);
                    erroAlerta.setTitle("Erro no Sistema");
                    erroAlerta.setHeaderText("Não foi possível remover a transação.");
                    erroAlerta.setContentText("Ocorreu um erro ao acessar o banco de dados.");
                    erroAlerta.showAndWait();
                    e.printStackTrace();
                }
            }
        });
    }
}