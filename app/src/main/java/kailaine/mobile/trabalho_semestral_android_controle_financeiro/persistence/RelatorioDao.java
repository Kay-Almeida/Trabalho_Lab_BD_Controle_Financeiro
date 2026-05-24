package kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence;
/*
 *@author:<Kailaine Almeida de Souza RA: 1110482313026>
 */

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável por:
 *  - Relatório consolidado de receitas, despesas e reservas (item 6)
 *  - Chamada à Stored Procedure sp_resumo_financeiro (item 7)
 */
public class RelatorioDao {

    private Connection connection;

    public RelatorioDao open() throws SQLException {
        connection = DatabaseConnection.getConnection();
        return this;
    }

    public void close() {
        DatabaseConnection.closeConnection();
    }

    // ------------------------------------------------------------------
    // 6. Relatório de dados: consolidado financeiro por período
    // ------------------------------------------------------------------

    /**
     * Retorna o relatório consolidado: cada linha contém
     * tipo (RECEITA/DESPESA/RESERVA), data e valor.
     *
     * @param dataInicio formato 'YYYY-MM-DD'
     * @param dataFim    formato 'YYYY-MM-DD'
     */
    public List<String[]> gerarRelatorioConsolidado(String dataInicio, String dataFim)
            throws SQLException {
        List<String[]> linhas = new ArrayList<>();
        String sql =
                "SELECT 'RECEITA'  AS tipo, data, valor FROM receita  WHERE data BETWEEN ? AND ? " +
                        "UNION ALL " +
                        "SELECT 'DESPESA'  AS tipo, data, valor FROM despesa  WHERE data BETWEEN ? AND ? " +
                        "UNION ALL " +
                        "SELECT 'RESERVA'  AS tipo, r.data, r.valor " +
                        "  FROM reserva r WHERE r.data BETWEEN ? AND ? " +
                        "ORDER BY data";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Cada par de parâmetros corresponde a uma das três consultas da UNION
            stmt.setString(1, dataInicio); stmt.setString(2, dataFim);
            stmt.setString(3, dataInicio); stmt.setString(4, dataFim);
            stmt.setString(5, dataInicio); stmt.setString(6, dataFim);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    linhas.add(new String[]{
                            rs.getString("tipo"),
                            rs.getString("data"),
                            String.valueOf(rs.getDouble("valor"))
                    });
                }
            }
        }
        return linhas;
    }

    // ------------------------------------------------------------------
    // 7. Chamada à Stored Procedure sp_resumo_financeiro
    //    A procedure calcula totais de receita, despesa e saldo líquido
    //    para um determinado mês/ano e os retorna via parâmetros OUT.
    //
    //    Script MySQL para criar a procedure (execute no servidor):
    // ------------------------------------------------------------------
    /*
        DELIMITER $$
        CREATE PROCEDURE sp_resumo_financeiro(
            IN  p_mes   INT,
            IN  p_ano   INT,
            OUT p_total_receita  DECIMAL(10,2),
            OUT p_total_despesa  DECIMAL(10,2),
            OUT p_saldo          DECIMAL(10,2)
        )
        BEGIN
            SELECT COALESCE(SUM(valor), 0)
              INTO p_total_receita
              FROM receita
             WHERE MONTH(data) = p_mes AND YEAR(data) = p_ano;

            SELECT COALESCE(SUM(valor), 0)
              INTO p_total_despesa
              FROM despesa
             WHERE MONTH(data) = p_mes AND YEAR(data) = p_ano;

            SET p_saldo = p_total_receita - p_total_despesa;
        END$$
        DELIMITER ;
    */

    /**
     * Chama a stored procedure sp_resumo_financeiro e retorna
     * um array com [totalReceita, totalDespesa, saldo].
     *
     * @param mes mês desejado (1-12)
     * @param ano ano desejado (ex.: 2025)
     * @return double[] { totalReceita, totalDespesa, saldo }
     */
    public double[] chamarProcedureResumoFinanceiro(int mes, int ano) throws SQLException {
        String sql = "{CALL sp_resumo_financeiro(?, ?, ?, ?, ?)}";
        try (CallableStatement cs = connection.prepareCall(sql)) {
            // Parâmetros IN
            cs.setInt(1, mes);
            cs.setInt(2, ano);
            // Parâmetros OUT
            cs.registerOutParameter(3, Types.DECIMAL);  // p_total_receita
            cs.registerOutParameter(4, Types.DECIMAL);  // p_total_despesa
            cs.registerOutParameter(5, Types.DECIMAL);  // p_saldo

            cs.execute();

            double totalReceita = cs.getDouble(3);
            double totalDespesa = cs.getDouble(4);
            double saldo        = cs.getDouble(5);

            return new double[]{ totalReceita, totalDespesa, saldo };
        }
    }
}