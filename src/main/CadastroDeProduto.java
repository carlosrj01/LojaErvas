package main;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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

        // Botão "Voltar"
        Button botaoVoltar = new Button("Voltar");
        botaoVoltar.setOnAction(e -> {
            primaryStage.close();
            menuAnterior.show();
        });

        HBox painelSuperior = new HBox(botaoVoltar);
        painelSuperior.setAlignment(Pos.CENTER_LEFT);
        painelSuperior.setPadding(new Insets(10));

        // Campos de cadastro
        Label labelID = new Label("ID do Produto:");
        campoID = new TextField();
        Label labelNome = new Label("Nome do Produto:");
        campoNome = new TextField();
        Label labelPreco = new Label("Preço:");
        campoPreco = new TextField();
        Label labelQuantidade = new Label("Quantidade:");
        campoQuantidade = new TextField();
        Button botaoCadastrar = new Button("Cadastrar");

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
        painelCampos.add(labelPreco, 0, 2);
        painelCampos.add(campoPreco, 1, 2);
        painelCampos.add(labelQuantidade, 0, 3);
        painelCampos.add(campoQuantidade, 1, 3);
        painelCampos.add(botaoCadastrar, 1, 4);

        VBox root = new VBox(10, painelSuperior, painelCampos);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void cadastrarProduto() {
        int id;
        String nome = campoNome.getText();
        double preco;
        int quantidade;

        try {
            id = Integer.parseInt(campoID.getText());
            preco = Double.parseDouble(campoPreco.getText());
            quantidade = Integer.parseInt(campoQuantidade.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "ID, preço ou quantidade inválidos.");
            return;
        }

        Connection conexao = ConexaoBancoDados.conectar();
        String sql = "INSERT INTO produtos (id, nome, valor, qtd) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, nome);
            stmt.setDouble(3, preco);
            stmt.setInt(4, quantidade);
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
