package main;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

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

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Configurar a tabela de histórico de entradas
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
        
        painelTopo.getChildren().add(botaoVoltar);
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

        TableColumn<Entrada, String> colunaNome = new TableColumn<>("Nome Produto");
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));

        TableColumn<Entrada, Integer> colunaQuantidade = new TableColumn<>("Quantidade");
        colunaQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));

        TableColumn<Entrada, String> colunaDataHora = new TableColumn<>("Data e Hora");
        colunaDataHora.setCellValueFactory(new PropertyValueFactory<>("dataHora"));

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
