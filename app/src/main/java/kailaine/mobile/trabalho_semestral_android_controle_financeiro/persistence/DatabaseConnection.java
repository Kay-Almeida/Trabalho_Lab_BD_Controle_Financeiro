package kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence;
/*
 *@author:<Kailaine Almeida de Souza RA: 1110482313026>
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gerencia a conexão JDBC com o banco de dados MySQL.
 * Substitui o antigo GenericDAO (SQLiteOpenHelper).
 *
 * ATENÇÃO: Em produção, utilize variáveis de ambiente ou um arquivo
 * de configuração seguro para as credenciais — nunca em texto puro no código.
 */
public class DatabaseConnection {

    // ---------------------------------------------------------------
    // Ajuste as constantes abaixo de acordo com o seu ambiente MySQL
    // ---------------------------------------------------------------
    private static final String HOST     = "acela.proxy.rlwy.net";   // 10.0.2.2 aponta para o localhost do PC no emulador Android
    private static final String PORT     = "10924";
    private static final String DATABASE = "railway";
    private static final String USER     = "root";
    private static final String PASSWORD = "lCcUqSeNOBVEGXZfLHhlgrZbhEfYkjuA";

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                    + "?useSSL=false&serverTimezone=America/Sao_Paulo";
    private static Connection connection;

    private DatabaseConnection() { }

    /**
     * Retorna uma conexão singleton com o MySQL.
     * A conexão é reaberta automaticamente se estiver fechada.
     */
    public static synchronized Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL não encontrado. Verifique o build.gradle.", e);
        }

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    /** Fecha a conexão singleton, se aberta. */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }
}