package main;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.sql.*;

public class ModificarPrecoProduto {

    private Stage menuAnterior;
    private Stage janelaAlteracaoPreco;

    public ModificarPrecoProduto(Stage menuAnterior) {
        this.menuAnterior = menuAnterior;
        this.janelaAlteracaoPreco = new Stage();
    }

    public void start(Stage stage) {
        janelaAlteracaoPreco.setTitle("Modificar Preço do Produto");

        // Layout do botão "Voltar" no topo
        HBox painelTopo = new HBox(10);
        painelTopo.setAlignment(Pos.CENTER_LEFT);
        Button botaoVoltar = new Button("Voltar");
        botaoVoltar.setFont(Font.font("Helvetica", 18));
        botaoVoltar.setStyle("-fx-background-color: #3296FF; -fx-text-fill: white;");
        botaoVoltar.setOnAction(e -> {
            janelaAlteracaoPreco.close();  // Fecha a janela de alteração de preço
            menuAnterior.show();  // Reabre o menu principal
        });
        painelTopo.getChildren().add(botaoVoltar);

        // Layout dos campos de entrada
        GridPane painelCampos = new GridPane();
        painelCampos.setAlignment(Pos.CENTER);
        painelCampos.setHgap(10);
        painelCampos.setVgap(10);
        
        Label labelIdProduto = new Label("ID do Produto:");
        labelIdProduto.setFont(Font.font("Helvetica", 18));
        labelIdProduto.setStyle("-fx-text-fill: white;");
        TextField campoIdProduto = new TextField();
        campoIdProduto.setFont(Font.font("Helvetica", 18));

        Label labelNomeProduto = new Label("Nome do Produto:");
        labelNomeProduto.setFont(Font.font("Helvetica", 18));
        labelNomeProduto.setStyle("-fx-text-fill: white;");
        TextField campoNomeProduto = new TextField();
        campoNomeProduto.setFont(Font.font("Helvetica", 18));
        campoNomeProduto.setEditable(false);

        Label labelValorProduto = new Label("Valor Atual:");
        labelValorProduto.setFont(Font.font("Helvetica", 18));
        labelValorProduto.setStyle("-fx-text-fill: white;");
        TextField campoValorProduto = new TextField();
        campoValorProduto.setFont(Font.font("Helvetica", 18));
        campoValorProduto.setEditable(false);

        Label labelNovoValor = new Label("Novo Valor:");
        labelNovoValor.setFont(Font.font("Helvetica", 18));
        labelNovoValor.setStyle("-fx-text-fill: white;");
        TextField campoNovoValor = new TextField();
        campoNovoValor.setFont(Font.font("Helvetica", 18));

        painelCampos.add(labelIdProduto, 0, 0);
        painelCampos.add(campoIdProduto, 1, 0);
        painelCampos.add(labelNomeProduto, 0, 1);
        painelCampos.add(campoNomeProduto, 1, 1);
        painelCampos.add(labelValorProduto, 0, 2);
        painelCampos.add(campoValorProduto, 1, 2);
        painelCampos.add(labelNovoValor, 0, 3);
        painelCampos.add(campoNovoValor, 1, 3);

        campoIdProduto.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {  // Quando o campo perde o foco
                String idProduto = campoIdProduto.getText();
                if (!idProduto.isEmpty()) {
                    preencherProdutoPorId(idProduto, campoNomeProduto, campoValorProduto);
                }
            }
        });

        // Botão "Alterar Preço" para modificar o preço
        Button botaoAlterarPreco = new Button("Alterar Preço");
        botaoAlterarPreco.setFont(Font.font("Helvetica", 18));
        botaoAlterarPreco.setStyle("-fx-background-color: #3296FF; -fx-text-fill: white;");
        botaoAlterarPreco.setOnAction(e -> {
            String idProduto = campoIdProduto.getText();
            String novoValorTexto = campoNovoValor.getText();

            if (idProduto.isEmpty() || novoValorTexto.isEmpty()) {
                Alert alerta = new Alert(Alert.AlertType.WARNING, "Por favor, preencha todos os campos.");
                alerta.show();
                return;
            }

            try {
                double novoValor = Double.parseDouble(novoValorTexto.trim());
                atualizarValorProduto(idProduto, novoValor);

                campoIdProduto.clear();
                campoNomeProduto.clear();
                campoValorProduto.clear();
                campoNovoValor.clear();

                Alert alertaSucesso = new Alert(Alert.AlertType.INFORMATION, "Preço do produto alterado com sucesso!");
                alertaSucesso.show();
            } catch (NumberFormatException ex) {
                Alert alertaErro = new Alert(Alert.AlertType.ERROR, "Por favor, insira um valor numérico válido.");
                alertaErro.show();
            }
        });

        // Layout principal da interface
        VBox painelPrincipal = new VBox(20);
        painelPrincipal.setPadding(new Insets(20));
        painelPrincipal.setStyle("-fx-background-color: black;");
        painelPrincipal.getChildren().addAll(painelTopo, painelCampos, botaoAlterarPreco);

        Scene cena = new Scene(painelPrincipal, 500, 400);
        janelaAlteracaoPreco.setScene(cena);
        janelaAlteracaoPreco.show();
    }

    private void preencherProdutoPorId(String idProduto, TextField campoNomeProduto, TextField campoValorProduto) {
        Connection conexao = ConexaoBancoDados.conectar();
        String sql = "SELECT nome, valor FROM produtos WHERE ID = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, idProduto);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                campoNomeProduto.setText(rs.getString("nome"));
                campoValorProduto.setText(String.format("%.2f", rs.getDouble("valor")));
            } else {
                Alert alerta = new Alert(Alert.AlertType.ERROR, "Produto não encontrado.");
                alerta.show();
                campoNomeProduto.clear();
                campoValorProduto.clear();
            }
        } catch (SQLException e) {
            Alert alertaErro = new Alert(Alert.AlertType.ERROR, "Erro ao buscar produto: " + e.getMessage());
            alertaErro.show();
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
    }

    private void atualizarValorProduto(String idProduto, double novoValor) {
        Connection conexao = ConexaoBancoDados.conectar();
        String sql = "UPDATE produtos SET valor = ? WHERE ID = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setDouble(1, novoValor);
            stmt.setString(2, idProduto);
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                Alert alerta = new Alert(Alert.AlertType.ERROR, "Produto não encontrado para atualização.");
                alerta.show();
            }
        } catch (SQLException e) {
            Alert alertaErro = new Alert(Alert.AlertType.ERROR, "Erro ao atualizar valor: " + e.getMessage());
            alertaErro.show();
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
    }
}
