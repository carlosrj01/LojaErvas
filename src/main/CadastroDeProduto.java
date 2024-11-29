package main;

import javafx.application.Application;
import javafx.application.Platform; // Import necessário
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CadastroDeProduto extends Application {

    private TextField campoID;
    private TextField campoNome;
    private TextField campoPreco;
    private TextField campoQuantidade;
    private Stage menuAnterior;

    public CadastroDeProduto(Stage menuAnterior) {
        this.menuAnterior = menuAnterior;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Cadastro de Produto");

        // Fonte e Estilos
        Font fontePadrao = Font.font("Consolas", 18);
        String estiloBotao = "-fx-background-color: #3A5A40;";
        String estiloBotaoHover = "-fx-background-color: #587A58;";

        // Botão "Voltar"
        Button botaoVoltar = new Button("Voltar");
        botaoVoltar.setFont(fontePadrao);
        botaoVoltar.setStyle(estiloBotao);
        botaoVoltar.setTextFill(Color.WHITE);
        botaoVoltar.setOnMouseEntered(e -> botaoVoltar.setStyle(estiloBotaoHover));
        botaoVoltar.setOnMouseExited(e -> botaoVoltar.setStyle(estiloBotao));
        botaoVoltar.setOnAction(e -> {
            primaryStage.close();
            menuAnterior.show();
        });

        HBox painelSuperior = new HBox(botaoVoltar);
        painelSuperior.setAlignment(Pos.CENTER_LEFT);
        painelSuperior.setPadding(new Insets(10));

        // Campos de cadastro
        Label labelID = new Label("ID do Produto:");
        labelID.setFont(fontePadrao);
        labelID.setTextFill(Color.DARKGREEN);

        campoID = new TextField();
        campoID.setFont(fontePadrao);
        campoID.setStyle("-fx-background-color: darkgray; -fx-text-fill: white;");

        Label labelNome = new Label("Nome do Produto:");
        labelNome.setFont(fontePadrao);
        labelNome.setTextFill(Color.DARKGREEN);

        campoNome = new TextField();
        campoNome.setFont(fontePadrao);
        campoNome.setStyle("-fx-background-color: darkgray; -fx-text-fill: white;");

        campoPreco = new TextField();
        campoPreco.setFont(fontePadrao);
        campoPreco.setStyle("-fx-background-color: darkgray; -fx-text-fill: white;");

        Label labelQuantidade = new Label("Quantidade:");
        labelQuantidade.setFont(fontePadrao);
        labelQuantidade.setTextFill(Color.DARKGREEN);

        campoQuantidade = new TextField();
        campoQuantidade.setFont(fontePadrao);
        campoQuantidade.setStyle("-fx-background-color: darkgray; -fx-text-fill: white;");

        Button botaoCadastrar = new Button("Cadastrar");
        botaoCadastrar.setFont(fontePadrao);
        botaoCadastrar.setStyle(estiloBotao);
        botaoCadastrar.setTextFill(Color.WHITE);
        botaoCadastrar.setOnMouseEntered(e -> botaoCadastrar.setStyle(estiloBotaoHover));
        botaoCadastrar.setOnMouseExited(e -> botaoCadastrar.setStyle(estiloBotao));
        botaoCadastrar.setOnAction(e -> cadastrarProduto());

        // Painel com GridPane
        GridPane painelCampos = new GridPane();
        painelCampos.setHgap(10);
        painelCampos.setVgap(10);
        painelCampos.setPadding(new Insets(10));
        painelCampos.add(labelID, 0, 0);
        painelCampos.add(campoID, 1, 0);
        painelCampos.add(labelNome, 0, 1);
        painelCampos.add(campoNome, 1, 1);
        painelCampos.add(labelQuantidade, 0, 2);
        painelCampos.add(campoQuantidade, 1, 2);
        painelCampos.add(botaoCadastrar, 1, 3);

        VBox root = new VBox(10, painelSuperior, painelCampos);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #ECEBD7;");

        Scene scene = new Scene(root, 360, 320);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Garantir que o campo ID receba o foco após a interface ser carregada
        Platform.runLater(() -> campoID.requestFocus());

        // Listener para detectar a leitura do código de barras no campo ID
        campoID.setOnAction(e -> {
            // Após o código ser lido, move o foco para o próximo campo (Nome)
            if (!campoID.getText().isEmpty()) {
                campoNome.requestFocus(); // Move o foco para o campo Nome
            }
        });

        // Adiciona um listener que ao perder o foco faz validação
        campoID.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                // Ação quando o campo perde o foco (após leitura)
                if (campoID.getText().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Código do Produto está vazio!");
                }
            }
        });
    }

    private void cadastrarProduto() {
        String id = campoID.getText();
        String nome = campoNome.getText();
        int quantidade;

        try {
            quantidade = Integer.parseInt(campoQuantidade.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "ID, preço ou quantidade inválidos.");
            return;
        }

        Connection conexao = ConexaoBancoDados.conectar();
        String sql = "INSERT INTO produtos (id, nome, qtd) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, nome);
            stmt.setInt(3, quantidade);
            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Produto cadastrado com sucesso!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao cadastrar produto: " + e.getMessage());
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
