package kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence;
/*
 *@author:<Kailaine Almeida de Souza RA: 1110482313026>
 */
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.Despesa;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.ValorInvalidoException;

public class DespesaDao implements ICRUDDao<Despesa>, IDespesaDAO {

    private Connection connection;

    // ------------------------------------------------------------------
    // Ciclo de vida da conexão
    // ------------------------------------------------------------------

    @Override
    public IDespesaDAO open() throws SQLException {
        connection = DatabaseConnection.getConnection();
        return this;
    }

    @Override
    public void close() {
        DatabaseConnection.closeConnection();
    }

    // ------------------------------------------------------------------
    // CRUD
    // ------------------------------------------------------------------

    @Override
    public void insert(Despesa despesa) throws SQLException {
        String sql = "INSERT INTO despesa (valor, data) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, despesa.getValor());
            stmt.setString(2, despesa.getData().toString());
            stmt.executeUpdate();
        }
    }

    @Override
    public int update(Despesa despesa) throws SQLException {
        String sql = "UPDATE despesa SET valor = ?, data = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, despesa.getValor());
            stmt.setString(2, despesa.getData().toString());
            stmt.setInt(3, despesa.getId());
            return stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Despesa despesa) throws SQLException {
        String sql = "DELETE FROM despesa WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, despesa.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public Despesa buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM despesa WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getDespesaFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Despesa> findAll() throws SQLException {
        List<Despesa> despesas = new ArrayList<>();
        String sql = "SELECT * FROM despesa";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                despesas.add(getDespesaFromResultSet(rs));
            }
        }
        return despesas;
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private static Despesa getDespesaFromResultSet(ResultSet rs) throws SQLException {
            Despesa despesa = new Despesa();
            despesa.setId(rs.getInt("id"));
            despesa.setValor(rs.getDouble("valor"));
            // Se o modelo tiver setData(), descomente:
            // despesa.setData(rs.getString("data"));
            return despesa;

    }
}
