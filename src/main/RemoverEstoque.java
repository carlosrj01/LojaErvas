package main;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class RemoverEstoque extends Application {

    private ArrayList<String[]> produtos; // Lista para armazenar os produtos adicionados
    private ListView<String> listaProdutosView; // Lista de exibição dos produtos
    private Stage menuAnterior; // Janela do menu principal

    public RemoverEstoque(Stage menuAnterior) {
        this.menuAnterior = menuAnterior;
        produtos = new ArrayList<>();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Carrinho de Compras");
        primaryStage.setOnCloseRequest(e -> menuAnterior.show());

        // Layout principal
        VBox layoutPrincipal = new VBox(10);
        layoutPrincipal.setPadding(new Insets(10));
        layoutPrincipal.setAlignment(Pos.TOP_CENTER);
        layoutPrincipal.setStyle("-fx-background-color: black;");

        // Botão "Voltar"
        Button botaoVoltar = criarBotao("Voltar");
        botaoVoltar.setOnAction(e -> {
            primaryStage.close();
            menuAnterior.show();
        });

        // Campos de entrada
        TextField campoIdProduto = new TextField();
        TextField campoNomeProduto = new TextField();
        TextField campoValor = new TextField();
        TextField campoQuantidade = new TextField();

        campoNomeProduto.setEditable(false);
        campoValor.setEditable(false);

        // Campo de ID para preencher automaticamente nome e valor ao perder o foco
        campoIdProduto.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                preencherProdutoPorId(campoIdProduto.getText(), campoNomeProduto, campoValor);
            }
        });

        // Layout dos campos de entrada
        GridPane gridCampos = new GridPane();
        gridCampos.setHgap(10);
        gridCampos.setVgap(10);
        gridCampos.setAlignment(Pos.CENTER);
        gridCampos.setPadding(new Insets(10));

        gridCampos.add(criarLabel("ID do Produto:"), 0, 0);
        gridCampos.add(campoIdProduto, 1, 0);
        gridCampos.add(criarLabel("Nome do Produto:"), 0, 1);
        gridCampos.add(campoNomeProduto, 1, 1);
        gridCampos.add(criarLabel("Valor:"), 0, 2);
        gridCampos.add(campoValor, 1, 2);
        gridCampos.add(criarLabel("Quantidade:"), 0, 3);
        gridCampos.add(campoQuantidade, 1, 3);

        // Lista de produtos
        listaProdutosView = new ListView<>();
        listaProdutosView.setPrefHeight(150);

        // Botão "Adicionar"
        Button botaoAdicionar = criarBotao("Adicionar");
        botaoAdicionar.setOnAction(e -> {
            String idProduto = campoIdProduto.getText();
            String nomeProduto = campoNomeProduto.getText();
            String valorTexto = campoValor.getText();
            String quantidade = campoQuantidade.getText();

            if (idProduto.isEmpty() || nomeProduto.isEmpty() || valorTexto.isEmpty() || quantidade.isEmpty()) {
                exibirMensagem("Por favor, preencha todos os campos.", Alert.AlertType.WARNING);
                return;
            }

            try {
                String valorTextoComPonto = valorTexto.replace(",", ".");
                double valorProduto = Double.parseDouble(valorTextoComPonto.trim());
                int qtdProduto = Integer.parseInt(quantidade.trim());

                int qtdEmEstoque = verificarQuantidadeEmEstoque(idProduto);
                if (qtdProduto > qtdEmEstoque) {
                    exibirMensagem("Quantidade solicitada maior que a disponível em estoque. Estoque disponível: " + qtdEmEstoque, Alert.AlertType.ERROR);
                    return;
                }

                atualizarEstoque(idProduto, qtdProduto);

                double subtotal = valorProduto * qtdProduto;
                DecimalFormat df = new DecimalFormat("#,##0.00");
                String item = "ID: " + idProduto + " | Nome: " + nomeProduto + " | Quantidade: " + qtdProduto + " | Valor: R$ " + df.format(valorProduto) + " | Subtotal: R$ " + df.format(subtotal);

                produtos.add(new String[]{idProduto, nomeProduto, String.valueOf(valorProduto), String.valueOf(qtdProduto)});
                listaProdutosView.getItems().add(item);

                campoIdProduto.clear();
                campoNomeProduto.clear();
                campoValor.clear();
                campoQuantidade.clear();

            } catch (NumberFormatException ex) {
                exibirMensagem("Erro ao calcular o subtotal. Verifique os valores inseridos.", Alert.AlertType.ERROR);
                ex.printStackTrace();
            }
        });

        // Botão "Finalizar Compra"
        Button botaoFinalizar = criarBotao("Finalizar Compra");
        botaoFinalizar.setOnAction(e -> {
            double total = 0;
            for (String[] produto : produtos) {
            	String idProduto = produto[0];
                double valorProduto = Double.parseDouble(produto[2]);
                int qtdProduto = Integer.parseInt(produto[3]);
                total += valorProduto * qtdProduto;
                
             // Adiciona o produto no histórico de compras
                HistoricoSaida.adicionarCompra(idProduto, valorProduto, qtdProduto);
                
            }
            exibirMensagem("Total de Estoque retirado em valor: R$  " + total, Alert.AlertType.INFORMATION);
        });

        // Layout dos botões
        HBox botoesBox = new HBox(10, botaoAdicionar, botaoFinalizar);
        botoesBox.setAlignment(Pos.CENTER);

        layoutPrincipal.getChildren().addAll(botaoVoltar, gridCampos, listaProdutosView, botoesBox);

        Scene cena = new Scene(layoutPrincipal, 600, 400);
        primaryStage.setScene(cena);
        primaryStage.show();
    }

    // Criação de rótulos com estilo padrão
    private Label criarLabel(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Helvetica", 18));
        label.setTextFill(Color.WHITE);
        return label;
    }

    // Criação de botões com estilo padrão
    private Button criarBotao(String texto) {
        Button botao = new Button(texto);
        botao.setFont(Font.font("Helvetica", 18));
        botao.setTextFill(Color.WHITE);
        botao.setStyle("-fx-background-color: #3296FF;");
        botao.setOnMouseEntered(e -> botao.setStyle("-fx-background-color: #46b4ff;"));
        botao.setOnMouseExited(e -> botao.setStyle("-fx-background-color: #3296FF;"));
        return botao;
    }

    // Método para preencher o nome e o valor do produto com base no ID
    private void preencherProdutoPorId(String idProduto, TextField campoNomeProduto, TextField campoValor) {
        Connection conexao = ConexaoBancoDados.conectar();
        String sql = "SELECT nome, valor FROM produtos WHERE ID = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, idProduto);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                campoNomeProduto.setText(rs.getString("nome"));
                campoValor.setText(String.format("%.2f", rs.getDouble("valor")));
            } else {
                exibirMensagem("Produto não encontrado.", Alert.AlertType.ERROR);
                campoNomeProduto.clear();
                campoValor.clear();
            }
        } catch (SQLException e) {
            exibirMensagem("Erro ao buscar produto: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
    }

    // Verifica a quantidade do produto no estoque
    private int verificarQuantidadeEmEstoque(String idProduto) {
        int quantidadeEstoque = 0;
        Connection conexao = ConexaoBancoDados.conectar();
        String sql = "SELECT qtd FROM produtos WHERE ID = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, idProduto);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                quantidadeEstoque = rs.getInt("qtd");
            }
        } catch (SQLException e) {
            exibirMensagem("Erro ao verificar quantidade em estoque: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
        return quantidadeEstoque;
    }

    // Atualiza o estoque após a compra
    private void atualizarEstoque(String idProduto, int qtdProduto) {
        Connection conexao = ConexaoBancoDados.conectar();
        String sql = "UPDATE produtos SET qtd = qtd - ? WHERE ID = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, qtdProduto);
            stmt.setString(2, idProduto);
            stmt.executeUpdate();
        } catch (SQLException e) {
            exibirMensagem("Erro ao atualizar estoque: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
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