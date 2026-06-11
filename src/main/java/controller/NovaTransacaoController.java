package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import model.Transacao;
import repository.TransacaoDAO;

import java.util.function.UnaryOperator;

public class NovaTransacaoController {

    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtValor;
    @FXML private DatePicker datePicker;
    @FXML private TextArea txtObservacao;
    @FXML private CheckBox chkConfirmar;
    @FXML private Label lblFeedback;
    @FXML private Button btnSalvar;

    private final TransacaoDAO dao = new TransacaoDAO();

    @FXML
    public void initialize() {

        cbTipo.getItems().addAll("Receita", "Despesa");

        UnaryOperator<TextFormatter.Change> filtroValor = change -> {
            String texto = change.getControlNewText();

            if (texto.matches("\\d*([.,]\\d{0,2})?")) {
                return change;
            }

            return null;
        };

        txtValor.setTextFormatter(new TextFormatter<>(filtroValor));

        datePicker.setEditable(false);
    }

    @FXML
    public void salvarTransacao() {

        if (cbTipo.getValue() == null) {
            mostrarErro("Selecione o tipo da transação.");
            return;
        }

        String descricao = txtDescricao.getText().trim();

        if (descricao.isEmpty()) {
            mostrarErro("Informe a descrição.");
            return;
        }

        if (!descricao.matches(".*[a-zA-ZÀ-ÿ].*")) {
            mostrarErro("A descrição deve conter letras.");
            return;
        }

        if (txtValor.getText().isBlank()) {
            mostrarErro("Informe o valor.");
            return;
        }

        double valor;

        try {

            valor = Double.parseDouble(
                    txtValor.getText()
                            .replace(",", ".")
                            .trim());

            if (valor <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            mostrarErro("Valor inválido. Informe um número maior que zero.");
            return;
        }

        if (datePicker.getValue() == null) {
            mostrarErro("Selecione a data.");
            return;
        }

        if (!chkConfirmar.isSelected()) {
            mostrarErro("Confirme que os dados estão corretos.");
            return;
        }

        lblFeedback.setVisible(false);
        lblFeedback.setManaged(false);

        Transacao nova = new Transacao(
                descricao,
                valor,
                cbTipo.getValue(),
                datePicker.getValue()
        );

        dao.salvar(nova);

        fechar();
    }

    @FXML
    public void fechar() {
        ((Stage) btnSalvar.getScene().getWindow()).close();
    }

    private void mostrarErro(String msg) {

        lblFeedback.setText(msg);

        if (!lblFeedback.getStyleClass().contains("label-erro")) {
            lblFeedback.getStyleClass().add("label-erro");
        }

        lblFeedback.setVisible(true);
        lblFeedback.setManaged(true);
    }
}