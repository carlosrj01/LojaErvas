package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBancoDados {
    private static final String URL = "jdbc:mysql://localhost:3306/lojaervas"; // URL do banco
    private static final String USUARIO = "root"; // Usuário padrão do WampServer
    private static final String SENHA = ""; // Senha padrão (em branco para WampServer, a menos que tenha sido modificada)

    public static Connection conectar() {
        Connection conexao = null;
        try {
            // Conecta ao banco de dados
            conexao = DriverManager.getConnection(URL, USUARIO, SENHA);
            System.out.println("Conexão com o banco de dados bem-sucedida!");
        } catch (SQLException e) {
            System.err.println("Erro ao conectar com o banco de dados: " + e.getMessage());
        }
        return conexao;
    }

    public static void desconectar(Connection conexao) {
        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
                System.out.println("Conexão com o banco de dados fechada.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar a conexão: " + e.getMessage());
        }
    }
}