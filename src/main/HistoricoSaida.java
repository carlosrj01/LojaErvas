package main;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.table.DefaultTableModel;

public class HistoricoSaida {

    private TableView<Compra> tabelaHistorico = new TableView<>();
    private Stage menuAnterior;

    public HistoricoSaida(Stage menuAnterior) {
        this.menuAnterior = menuAnterior;
    }
    
    
    private static DefaultTableModel modeloHistorico = new DefaultTableModel(
            new Object[]{"ID Produto", "Nome Produto", "Quantidade", "Data e Hora"}, 0
    );

    // Método start para iniciar a janela de Histórico de Compras
    public void start(Stage janelaHistorico) {
        janelaHistorico.setTitle("Histórico de Compras");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Configurar a tabela de histórico de compras
        configurarTabelaHistorico();
        root.setCenter(tabelaHistorico);

        // Painel de botões
        HBox painelTopo = new HBox(10);
        painelTopo.setPadding(new Insets(10, 0, 10, 0));

        // Botão Voltar
        Button botaoVoltar = new Button("Voltar");
        botaoVoltar.setOnAction(e -> {
            janelaHistorico.close();
            menuAnterior.show();
        });
        
       
        // Botão Gerar CSV
        Button botaoGerarCSV = new Button();
        ImageView iconeCSV = new ImageView(new Image("file:C:/Users/carlo/eclipse-workspace/LojaErvas/src/main/csv.png"));
        iconeCSV.setFitWidth(20);
        iconeCSV.setFitHeight(20);
        botaoGerarCSV.setGraphic(iconeCSV);
        botaoGerarCSV.setOnAction(e -> gerarCSV());

        painelTopo.getChildren().addAll(botaoVoltar, botaoGerarCSV);
        root.setTop(painelTopo);

        carregarHistoricoDoBanco();

        // Configurar e exibir a janela
        Scene scene = new Scene(root, 800, 500);
        janelaHistorico.setScene(scene);
        janelaHistorico.show();
    }

    // Configurar colunas da tabela
    private void configurarTabelaHistorico() {
        TableColumn<Compra, String> colunaId = new TableColumn<>("ID Produto");
        colunaId.setCellValueFactory(new PropertyValueFactory<>("idProduto"));

        TableColumn<Compra, String> colunaNome = new TableColumn<>("Nome Produto");
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));

        TableColumn<Compra, Integer> colunaQuantidade = new TableColumn<>("Quantidade");
        colunaQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));

        TableColumn<Compra, String> colunaDataHora = new TableColumn<>("Data e Hora");
        colunaDataHora.setCellValueFactory(new PropertyValueFactory<>("dataHora"));

        tabelaHistorico.getColumns().addAll(colunaId, colunaNome, colunaQuantidade, colunaDataHora);
    }

    // Método para carregar o histórico de vendas do banco de dados e adicionar à tabela
    private void carregarHistoricoDoBanco() {
        tabelaHistorico.getItems().clear();

        Connection conexao = ConexaoBancoDados.conectar();
        String sql = """
            SELECT vendas.idProduto, produtos.nome, vendas.qtdRetirado, vendas.dataRetirada
            FROM vendas
            INNER JOIN produtos ON vendas.idProduto = produtos.ID
        """;

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Compra compra = new Compra(
                        rs.getString("idProduto"),
                        rs.getString("nome"),
                        rs.getInt("qtdRetirado"),
                        rs.getString("dataRetirada")
                );
                tabelaHistorico.getItems().add(compra);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar histórico de vendas: " + e.getMessage());
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
    }

    // Método para gerar o CSV com o histórico de compras
    private void gerarCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo CSV", "*.csv"));
        File arquivo = fileChooser.showSaveDialog(null);

        if (arquivo != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo))) {
                writer.write("ID Produto,Nome Produto,Quantidade,Data e Hora\n");
                for (Compra compra : tabelaHistorico.getItems()) {
                    writer.write(compra.toCSV());
                    writer.newLine();
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "CSV gerado com sucesso!", ButtonType.OK);
                alert.showAndWait();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao salvar CSV: " + e.getMessage(), ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    // Classe interna para representar uma compra
    public static class Compra {
        private String idProduto;
        private String nomeProduto;
        private int quantidade;
        private String dataHora;

        public Compra(String idProduto, String nomeProduto, int quantidade, String dataHora) {
            this.idProduto = idProduto;
            this.nomeProduto = nomeProduto;
            this.quantidade = quantidade;
            this.dataHora = dataHora;
        }

        public String getIdProduto() { return idProduto; }
        public String getNomeProduto() { return nomeProduto; }
        public int getQuantidade() { return quantidade; }
        public String getDataHora() { return dataHora; }

        public String toCSV() {
            return String.format("%s,%s,%d,%s", idProduto, nomeProduto, quantidade, dataHora);
        }
    }
    
 // Método para adicionar uma compra ao histórico e salvar no banco de dados
    public static void adicionarCompra(String idProduto, int quantidade) {
        String dataHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        Connection conexao = ConexaoBancoDados.conectar();
        String sql = "INSERT INTO vendas (idProduto, qtdRetirado, dataRetirada) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, idProduto);
            stmt.setInt(2, quantidade);
            stmt.setString(3, dataHora);
            stmt.executeUpdate();
            System.out.println("Venda registrada no banco de dados.");
            modeloHistorico.addRow(new Object[]{idProduto, "Produto Não Carregado", quantidade, dataHora});
        } catch (SQLException e) {
            System.err.println("Erro ao registrar venda no banco de dados: " + e.getMessage());
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
    }

}