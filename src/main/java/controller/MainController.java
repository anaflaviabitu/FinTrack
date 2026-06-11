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

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    private static final Logger LOGGER =
            Logger.getLogger(MainController.class.getName());

    @FXML private Label lblSaldoTopo;
    @FXML private Label lblTotalReceitas;
    @FXML private Label lblTotalDespesas;
    @FXML private ComboBox<String> comboMes;
    @FXML private TableView<Transacao> tabelaTransacoes;
    @FXML private TableColumn<Transacao, LocalDate> colData;
    @FXML private TableColumn<Transacao, String> colDescricao;
    @FXML private TableColumn<Transacao, Double> colValor;
    @FXML private TableColumn<Transacao, String> colTipo;

    private final TransacaoDAO dao = new TransacaoDAO();

    @FXML
    public void initialize() {

        dao.inicializarBanco();

        configurarColunas();

        configurarFiltroMes();

        tabelaTransacoes.setPlaceholder(
                new Label("Nenhuma transação encontrada")
        );


        atualizarTabela();
    }

    private void configurarColunas() {

        colDescricao.setCellValueFactory(
                new PropertyValueFactory<>("descricao")
        );

        colValor.setCellValueFactory(
                new PropertyValueFactory<>("valor")
        );

        colTipo.setCellValueFactory(
                new PropertyValueFactory<>("tipo")
        );


        colData.setCellValueFactory(
                new PropertyValueFactory<>("data")
        );

        colData.setCellFactory(column -> new TableCell<>() {

            @Override
            protected void updateItem(LocalDate data, boolean empty) {

                super.updateItem(data, empty);

                getStyleClass().removeAll("coluna-data");

                if (empty || data == null) {
                    setText(null);
                } else {
                    setText(data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    getStyleClass().add("coluna-data");
                }
            }
        });


        colTipo.setCellFactory(column -> new TableCell<>() {

            @Override
            protected void updateItem(String item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(item);

                badge.getStyleClass().add(
                        "Receita".equals(item) ? "badge-receita" : "badge-despesa"
                );

                setGraphic(badge);
                setText(null);
            }
        });


        colValor.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(Double item, boolean empty) {

                super.updateItem(item, empty);

                getStyleClass().removeAll(
                        "coluna-valor-positivo",
                        "coluna-valor-negativo"
                );

                if (empty || item == null) {
                    setText(null);
                    return;
                }

                if (getTableView() == null || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    return;
                }

                Transacao t = getTableView().getItems().get(getIndex());

                if (t == null || t.getTipo() == null) {
                    setText(null);
                    return;
                }

                boolean receita = "Receita".equalsIgnoreCase(t.getTipo());
                String sinal = receita ? "+ " : "− ";

                setText(sinal + Formatador.formatarValor(item));

                getStyleClass().add(
                        receita ? "coluna-valor-positivo" : "coluna-valor-negativo"
                );
            }
        });
    }

    private void configurarFiltroMes() {

        comboMes.getItems().addAll(
                "Todos",
                "Janeiro", "Fevereiro", "Março",
                "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro",
                "Outubro", "Novembro", "Dezembro"
        );

        comboMes.setValue("Todos");

        comboMes.setOnAction(e -> {

            String mesSelecionado = comboMes.getValue();

            if ("Todos".equals(mesSelecionado)) {

                atualizarTabela();

            } else {

                int mes = comboMes.getSelectionModel().getSelectedIndex();

                List<Transacao> lista = dao.listarPorMes(mes);

                tabelaTransacoes.getItems().setAll(lista);

                atualizarResumoFinanceiro(lista);
            }
        });
    }

    @FXML
    public void abrirNovaTransacao() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/nova_transacao.fxml")
            );

            Parent root = loader.load();

            Stage stage = new Stage();

            stage.setTitle("Nova Transação — FinTrack");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.showAndWait();

            atualizarTabela();

        } catch (Exception e) {

            mostrarErro("Erro ao abrir a tela de nova transação.");

            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    @FXML
    public void abrirRelatorio() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/relatorio.fxml")
            );

            Parent root = loader.load();

            Stage stage = new Stage();

            stage.setTitle("Relatório — FinTrack");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setMinWidth(900);
            stage.setMinHeight(700);

            stage.setResizable(true);

            stage.showAndWait();

        } catch (Exception e) {

            mostrarErro("Erro ao abrir a tela de relatório.");

            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    @FXML
    public void removerTransacao() {

        Transacao selecionada = tabelaTransacoes
                .getSelectionModel()
                .getSelectedItem();

        if (selecionada == null) {
            mostrarAviso("Selecione uma transação para remover.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);

        confirmacao.setTitle("Confirmar remoção");
        confirmacao.setHeaderText("Deseja realmente remover esta transação?");
        confirmacao.setContentText(
                "Descrição: " + selecionada.getDescricao()
                        + "\nValor: " + Formatador.formatarValor(selecionada.getValor())
                        + "\nTipo: " + selecionada.getTipo()
        );

        adicionarCss(confirmacao);

        ButtonType resultado = confirmacao.showAndWait().orElse(ButtonType.CANCEL);

        if (resultado == ButtonType.OK) {

            try {

                dao.remover(selecionada.getId());
                atualizarTabela();

            } catch (Exception e) {

                mostrarErro("Não foi possível remover a transação.");

                LOGGER.log(Level.SEVERE, null, e);
            }
        }
    }

    public void atualizarTabela() {

        List<Transacao> lista = dao.listarTodos();

        tabelaTransacoes.getItems().setAll(lista);

        atualizarResumoFinanceiro(lista);
    }

    private void atualizarResumoFinanceiro(List<Transacao> lista) {

        double receitas = lista.stream()
                .filter(t -> "Receita".equalsIgnoreCase(t.getTipo()))
                .mapToDouble(Transacao::getValor)
                .sum();

        double despesas = lista.stream()
                .filter(t -> "Despesa".equalsIgnoreCase(t.getTipo()))
                .mapToDouble(Transacao::getValor)
                .sum();

        double saldo = receitas - despesas;

        lblTotalReceitas.setText(Formatador.formatarValor(receitas));
        lblTotalDespesas.setText(Formatador.formatarValor(despesas));
        lblSaldoTopo.setText(Formatador.formatarValor(saldo));


        lblSaldoTopo.getStyleClass().removeAll(
                "topo-valor",
                "topo-valor-positivo",
                "topo-valor-negativo"
        );

        lblSaldoTopo.getStyleClass().add(
                saldo >= 0 ? "topo-valor-positivo" : "topo-valor-negativo"
        );
    }

    private void mostrarAviso(String mensagem) {

        Alert alert = new Alert(Alert.AlertType.WARNING);

        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        adicionarCss(alert);

        alert.showAndWait();
    }

    private void mostrarErro(String mensagem) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        adicionarCss(alert);

        alert.showAndWait();
    }

    private void adicionarCss(Alert alert) {

        URL css = getClass().getResource("/css/style.css");

        if (css != null) {
            alert.getDialogPane()
                    .getStylesheets()
                    .add(css.toExternalForm());
        }
    }
}