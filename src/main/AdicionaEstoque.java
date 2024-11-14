package main;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdicionaEstoque extends Application {

    private Stage menuAnterior;

    // Construtor que recebe o Stage do menu anterior
    public AdicionaEstoque(Stage menuAnterior) {
        this.menuAnterior = menuAnterior;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Adicionar Mercadoria");
        primaryStage.setResizable(false);

        BorderPane painelFundo = new BorderPane();
        painelFundo.setStyle("-fx-background-color: ECEBD7;");
        
        // Fonte e Estilos
        Font fontePadrao = Font.font("Consolas", 18);
        String estiloBotao = "-fx-background-color: #3A5A40;";
        String estiloBotaoHover = "-fx-background-color: #587A58;";
      
        
        // Painel do botão "Voltar" no topo
        HBox painelTopo = new HBox();
        painelTopo.setAlignment(Pos.CENTER_LEFT);
        painelTopo.setPadding(new Insets(10));
        Button botaoVoltar = new Button("Voltar");
        botaoVoltar.setStyle(estiloBotao);
        botaoVoltar.setFont(fontePadrao);
        botaoVoltar.setTextFill(Color.WHITE);
        botaoVoltar.setOnMouseEntered(e -> botaoVoltar.setStyle(estiloBotaoHover));
        botaoVoltar.setOnMouseExited(e -> botaoVoltar.setStyle(estiloBotao));
        botaoVoltar.setOnAction(e -> {
            primaryStage.close(); // Fecha a janela atual
            menuAnterior.show(); // Mostra o MenuErvas (menu principal)
        });
        painelTopo.getChildren().add(botaoVoltar);
        painelFundo.setTop(painelTopo);

        // Painel dos campos de entrada
        GridPane painelCampos = new GridPane();
        painelCampos.setPadding(new Insets(10));
        painelCampos.setHgap(10);
        painelCampos.setVgap(10);
        painelCampos.setAlignment(Pos.CENTER);

        Label labelIdProduto = new Label("ID do Produto:");
        labelIdProduto.setFont(fontePadrao);
        labelIdProduto.setTextFill(Color.DARKGREEN);
        TextField campoIdProduto = new TextField();
        campoIdProduto.setStyle("-fx-background-color: darkgray; -fx-text-fill: white; -fx-font-size: 16px;");

        Label labelQuantidadeAdicionar = new Label("Quantidade a Adicionar:");
        labelQuantidadeAdicionar.setTextFill(Color.DARKGREEN);
        labelQuantidadeAdicionar.setFont(fontePadrao);
        TextField campoQuantidadeAdicionar = new TextField();
        campoQuantidadeAdicionar.setStyle("-fx-background-color: darkgray; -fx-text-fill: white; -fx-font-size: 16px;");

        painelCampos.add(labelIdProduto, 0, 0);
        painelCampos.add(campoIdProduto, 1, 0);
        painelCampos.add(labelQuantidadeAdicionar, 0, 1);
        painelCampos.add(campoQuantidadeAdicionar, 1, 1);
        painelFundo.setCenter(painelCampos);

        // Botão "Adicionar Quantidade" para atualizar o estoque
        Button botaoAdicionarQuantidade = new Button("Adicionar Quantidade");
        botaoAdicionarQuantidade.setStyle(estiloBotao);
        botaoAdicionarQuantidade.setFont(fontePadrao);
        botaoAdicionarQuantidade.setTextFill(Color.WHITE);
        botaoAdicionarQuantidade.setOnMouseEntered(e -> botaoAdicionarQuantidade.setStyle(estiloBotaoHover));
        botaoAdicionarQuantidade.setOnMouseExited(e -> botaoAdicionarQuantidade.setStyle(estiloBotao));
        botaoAdicionarQuantidade.setOnAction(e -> {
            String idProduto = campoIdProduto.getText();
            String quantidadeAdicionar = campoQuantidadeAdicionar.getText();

            if (idProduto.isEmpty() || quantidadeAdicionar.isEmpty()) {
                exibirAlerta(AlertType.WARNING, "Campos Incompletos", "Por favor, preencha todos os campos.");
                return;
            }

            try {
                int quantidade = Integer.parseInt(quantidadeAdicionar.trim());

                if (verificarProdutoExiste(idProduto)) {
                    atualizarEstoque(idProduto, quantidade);
                    exibirAlerta(AlertType.INFORMATION, "Sucesso", "Quantidade adicionada com sucesso!");
                    campoIdProduto.clear();
                    campoQuantidadeAdicionar.clear();
                    HistoricoEntrada.adicionarEntrada(idProduto, quantidade);
                } else {
                    exibirAlerta(AlertType.ERROR, "Erro", "Produto não encontrado no banco de dados.");
                }

            } catch (NumberFormatException ex) {
                exibirAlerta(AlertType.ERROR, "Erro", "Erro ao adicionar a quantidade. Verifique o valor inserido.");
            }
        });

        HBox painelBotoes = new HBox(botaoAdicionarQuantidade);
        painelBotoes.setAlignment(Pos.CENTER);
        painelBotoes.setPadding(new Insets(10));
        painelFundo.setBottom(painelBotoes);

        Scene scene = new Scene(painelFundo, 500, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean verificarProdutoExiste(String idProduto) {
        boolean existe = false;
        Connection conexao = ConexaoBancoDados.conectar();
        String sql = "SELECT ID FROM produtos WHERE ID = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, idProduto);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                existe = true;
            }
        } catch (SQLException e) {
            exibirAlerta(AlertType.ERROR, "Erro", "Erro ao verificar produto: " + e.getMessage());
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
        return existe;
    }

    private void atualizarEstoque(String idProduto, int quantidadeAdicionar) {
        Connection conexao = ConexaoBancoDados.conectar();
        String sql = "UPDATE produtos SET qtd = qtd + ? WHERE ID = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, quantidadeAdicionar);
            stmt.setString(2, idProduto);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated <= 0) {
                exibirAlerta(AlertType.ERROR, "Erro", "Erro ao atualizar o estoque do produto.");
            }
        } catch (SQLException e) {
            exibirAlerta(AlertType.ERROR, "Erro", "Erro ao atualizar estoque: " + e.getMessage());
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
    }

    private void exibirAlerta(AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
    
    
    
    
    
}
