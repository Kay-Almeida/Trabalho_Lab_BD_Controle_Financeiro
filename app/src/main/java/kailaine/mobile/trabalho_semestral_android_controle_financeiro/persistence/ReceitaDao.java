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

import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.Receita;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.ValorInvalidoException;

public class ReceitaDao implements ICRUDDao<Receita>, IReceitaDAO {

    private Connection connection;

    // ------------------------------------------------------------------
    // Ciclo de vida da conexão
    // ------------------------------------------------------------------

    @Override
    public IReceitaDAO open() throws SQLException {
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
    public void insert(Receita receita) throws SQLException {
        String sql = "INSERT INTO receita (valor, data) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, receita.getValor());
            stmt.setString(2, receita.getData().toString());
            stmt.executeUpdate();
        }
    }

    @Override
    public int update(Receita receita) throws SQLException {
        String sql = "UPDATE receita SET valor = ?, data = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, receita.getValor());
            stmt.setString(2, receita.getData().toString());
            stmt.setInt(3, receita.getId());
            return stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Receita receita) throws SQLException {
        String sql = "DELETE FROM receita WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, receita.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public Receita buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM receita WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getReceitaFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Receita> findAll() throws SQLException {
        List<Receita> receitas = new ArrayList<>();
        String sql = "SELECT * FROM receita";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                receitas.add(getReceitaFromResultSet(rs));
            }
        }
        return receitas;
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private static Receita getReceitaFromResultSet(ResultSet rs) throws SQLException {
            Receita receita = new Receita();
            receita.setId(rs.getInt("id"));
            receita.setValor(rs.getDouble("valor"));
            // receita.setData(rs.getString("data"));
            return receita;

    }
}