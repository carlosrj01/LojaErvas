package main;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.HistoricoSaida.Compra;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.table.DefaultTableModel;

public class HistoricoEntrada {

    private TableView<Entrada> tabelaHistorico = new TableView<>();
    private Stage menuAnterior;

    public HistoricoEntrada(Stage menuAnterior) {
        this.menuAnterior = menuAnterior;
    }
    
    private static DefaultTableModel modeloHistorico = new DefaultTableModel(
            new Object[]{"ID Produto", "Nome Produto", "Quantidade", "Data e Hora"}, 0
    );

    
    public void start(Stage janelaHistorico) throws ParseException {
        janelaHistorico.setTitle("Histórico de Entradas");

     // Fonte e Estilos
        Font fontePadrao = Font.font("Consolas", 18);
        String estiloBotao = "-fx-background-color: #3A5A40;";
        String estiloBotaoHover = "-fx-background-color: #587A58;";
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: ECEBD7;");
        root.setPadding(new Insets(10));

        // Configurar a tabela de histórico de entradas
        configurarTabelaHistorico();
        root.setCenter(tabelaHistorico);

        // Painel de botões
        HBox painelTopo = new HBox(10);
        painelTopo.setPadding(new Insets(10, 0, 10, 0));

        // Botão Voltar
        Button botaoVoltar = new Button("Voltar");
        botaoVoltar.setStyle(estiloBotao);
        botaoVoltar.setFont(fontePadrao);
        botaoVoltar.setTextFill(Color.WHITE);
        botaoVoltar.setOnMouseEntered(e -> botaoVoltar.setStyle(estiloBotaoHover));
        botaoVoltar.setOnMouseExited(e -> botaoVoltar.setStyle(estiloBotao));
        botaoVoltar.setOnAction(e -> {
            janelaHistorico.close();
            menuAnterior.show();
        });
        
        Button botaoGerarCSV = new Button();
        ImageView iconeCSV = new ImageView(new Image("file:C:/Users/carlo/eclipse-workspace/LojaErvas/src/main/csv.png"));
        iconeCSV.setFitWidth(20);
        iconeCSV.setFitHeight(27);
        botaoGerarCSV.setGraphic(iconeCSV);
        botaoGerarCSV.setStyle(estiloBotao);
        botaoGerarCSV.setOnMouseEntered(e -> botaoGerarCSV.setStyle(estiloBotaoHover));
        botaoGerarCSV.setOnMouseExited(e -> botaoGerarCSV.setStyle(estiloBotao));
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
        TableColumn<Entrada, String> colunaId = new TableColumn<>("ID Produto");
        colunaId.setCellValueFactory(new PropertyValueFactory<>("idProduto"));
        colunaId.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 16px");

        TableColumn<Entrada, String> colunaNome = new TableColumn<>("Nome Produto");
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colunaNome.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 16px");

        TableColumn<Entrada, Integer> colunaQuantidade = new TableColumn<>("Quantidade");
        colunaQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colunaQuantidade.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 16px");

        TableColumn<Entrada, String> colunaDataHora = new TableColumn<>("Data e Hora");
        colunaDataHora.setCellValueFactory(new PropertyValueFactory<>("dataHora"));
        colunaDataHora.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 16px");

        tabelaHistorico.getColumns().addAll(colunaId, colunaNome, colunaQuantidade, colunaDataHora);
    }

    // Método para carregar o histórico de entradas do banco de dados e adicionar à tabela
    private void carregarHistoricoDoBanco() throws ParseException {
        tabelaHistorico.getItems().clear();

        Connection conexao = ConexaoBancoDados.conectar();
        String sql = """
            SELECT entradas.idProduto, produtos.nome, entradas.qtdAdicionada, entradas.dataEntrada
            FROM entradas
            INNER JOIN produtos ON entradas.idProduto = produtos.ID
        """;

        SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
            	 String dataEntrada = rs.getString("dataEntrada");
                 // Convertendo a String para um objeto Date
                 Date data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dataEntrada);
                 String dataFormatada = formatoData.format(data);
                Entrada entrada = new Entrada(
                        rs.getString("idProduto"),
                        rs.getString("nome"),
                        rs.getInt("qtdAdicionada"),
                        dataFormatada
                        
                );
                tabelaHistorico.getItems().add(entrada);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar histórico de entradas: " + e.getMessage());
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
    }
    
    private void gerarCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo CSV", "*.csv"));
        File arquivo = fileChooser.showSaveDialog(null);

        if (arquivo != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo))) {
                writer.write("ID Produto,Nome Produto,Quantidade,Data e Hora\n");
                for (Entrada compra : tabelaHistorico.getItems()) {
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
    
    
    

    // Classe interna para representar uma entrada
    public static class Entrada {
        private String idProduto;
        private String nomeProduto;
        private int quantidade;
        private String dataHora;

        public Entrada(String idProduto, String nomeProduto, int quantidade, String dataHora) {
            this.idProduto = idProduto;
            this.nomeProduto = nomeProduto;
            this.quantidade = quantidade;
            this.dataHora = dataHora;
        }

        public String toCSV() {
        	return String.format("%s,%s,%d,%s", idProduto, nomeProduto, quantidade, dataHora);
		}

		public String getIdProduto() { return idProduto; }
        public String getNomeProduto() { return nomeProduto; }
        public int getQuantidade() { return quantidade; }
        public String getDataHora() { return dataHora; }
    }
   
    public static void adicionarEntrada(String idProduto, int qtdAdicionada) {
        String dataHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        Connection conexao = ConexaoBancoDados.conectar();
        String sql = "INSERT INTO entradas (idProduto, qtdAdicionada, dataentrada) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, idProduto);
            stmt.setInt(2, qtdAdicionada);
            stmt.setString(3, dataHora);
            stmt.executeUpdate();
            System.out.println("Venda registrada no banco de dados.");
            modeloHistorico.addRow(new Object[]{idProduto, "Produto Não Carregado", qtdAdicionada, dataHora});
        } catch (SQLException e) {
            System.err.println("Erro ao registrar venda no banco de dados: " + e.getMessage());
        } finally {
            ConexaoBancoDados.desconectar(conexao);
        }
    } 
    
}
