package main;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VerificaEstoque extends Application {

    private ListView<String> listaProdutosView; // Lista de exibição dos produtos em estoque
    private Stage menuAnterior; // Janela do menu principal

    public VerificaEstoque(Stage menuAnterior) {
        this.menuAnterior = menuAnterior;
    }
    
    // Fonte e Estilos
    Font fontePadrao = Font.font("Consolas", 18);
    String estiloBotao = "-fx-background-color: #3A5A40;";
    String estiloBotaoHover = "-fx-background-color: #587A58;";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Produtos em Estoque");
        primaryStage.setOnCloseRequest(e -> menuAnterior.show());

        // Layout principal
        VBox layoutPrincipal = new VBox(10);
        layoutPrincipal.setPadding(new Insets(10));
        layoutPrincipal.setAlignment(Pos.TOP_CENTER);
        layoutPrincipal.setStyle("-fx-background-color: #ECEBD7;");

        // Botão "Voltar"
        Button botaoVoltar = criarBotao("Voltar");
        botaoVoltar.setOnAction(e -> {
            primaryStage.close();
            menuAnterior.show();
        });

        // Lista de produtos
        listaProdutosView = new ListView<>();
        listaProdutosView.setPrefHeight(300);

        // Preencher a lista com produtos do estoque
        carregarProdutosEstoque();

        // Layout final
        layoutPrincipal.getChildren().addAll(botaoVoltar, listaProdutosView);

        Scene cena = new Scene(layoutPrincipal, 600, 400);
        primaryStage.setScene(cena);
        primaryStage.show();
    }

    // Método para carregar produtos do estoque e exibir na lista
    private void carregarProdutosEstoque() {
        Connection conexao = ConexaoBancoDados.conectar();
        String sql = "SELECT ID, nome, qtd FROM produtos";

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String idProduto = rs.getString("ID");
                String nomeProduto = rs.getString("nome");
                int quantidade = rs.getInt("qtd");

                // Formatação da linha de exibição
                String item = String.format("ID: %s | Nome: %s | Quantidade: %d", idProduto, nomeProduto, quantidade);
                listaProdutosView.getItems().add(item);
            }
        } catch (SQLException e) {
            exibirMensagem("Erro ao carregar produtos do estoque: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
    }

    // Criação de botões com estilo padrão
    private Button criarBotao(String texto) {
        Button botao = new Button(texto);
        botao.setFont(fontePadrao);
        botao.setTextFill(Color.WHITE);
        botao.setStyle(estiloBotao);
        botao.setOnMouseEntered(e -> botao.setStyle(estiloBotaoHover));
        botao.setOnMouseExited(e -> botao.setStyle(estiloBotao));
        return botao;
    }

    // Exibe uma mensagem em um diálogo
    private void exibirMensagem(String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
