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

import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.MetaFinanceira;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.ReservaFinanceira;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.ValorInvalidoException;

public class ReservaFinanceiraDao implements ICRUDDao<ReservaFinanceira>, IReservaFinanceiraDAO {

    private Connection connection;

    // ------------------------------------------------------------------
    // Ciclo de vida da conexão
    // ------------------------------------------------------------------

    @Override
    public IReservaFinanceiraDAO open() throws SQLException {
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
    public void insert(ReservaFinanceira reserva) throws SQLException {
        String sql = "INSERT INTO reserva (valor, data, metaFinanceira) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, reserva.getValor());
            stmt.setString(2, reserva.getData().toString());
            stmt.setInt(3, reserva.getMeta().getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public int update(ReservaFinanceira reserva) throws SQLException {
        String sql = "UPDATE reserva SET valor = ?, data = ?, metaFinanceira = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, reserva.getValor());
            stmt.setString(2, reserva.getData().toString());
            stmt.setInt(3, reserva.getMeta().getId());
            stmt.setInt(4, reserva.getId());
            return stmt.executeUpdate();
        }
    }

    @Override
    public void delete(ReservaFinanceira reserva) throws SQLException {
        String sql = "DELETE FROM reserva WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reserva.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public ReservaFinanceira buscarPorId(int id) throws SQLException {
        String sql =
                "SELECT r.*, m.nome AS nome_meta " +
                        "FROM reserva r " +
                        "LEFT JOIN metaFinanceira m ON r.metaFinanceira = m.id " +
                        "WHERE r.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getReservaFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<ReservaFinanceira> findAll() throws SQLException {
        List<ReservaFinanceira> reservas = new ArrayList<>();
        String sql =
                "SELECT r.id, r.valor, r.data, r.metaFinanceira, m.nome AS nome_meta " +
                        "FROM reserva r " +
                        "LEFT JOIN metaFinanceira m ON r.metaFinanceira = m.id";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reservas.add(getReservaFromResultSet(rs));
            }
        }
        return reservas;
    }

    /**
     * Busca todas as reservas vinculadas a uma meta específica.
     */
    public List<ReservaFinanceira> buscarReservasPorMeta(int metaId) throws SQLException {
        List<ReservaFinanceira> reservas = new ArrayList<>();
        String sql = "SELECT * FROM reserva WHERE metaFinanceira = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, metaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReservaFinanceira reserva = new ReservaFinanceira();
                    reserva.setId(rs.getInt("id"));
                    reserva.setValor(rs.getDouble("valor"));
                    MetaFinanceira meta = new MetaFinanceira();
                    meta.setId(metaId);
                    reserva.setMeta(meta);
                    reservas.add(reserva);
                }
            }
        }
        return reservas;
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private static ReservaFinanceira getReservaFromResultSet(ResultSet rs) throws SQLException {

            ReservaFinanceira reserva = new ReservaFinanceira();
            reserva.setId(rs.getInt("id"));
            reserva.setValor(rs.getDouble("valor"));
            int metaId = rs.getInt("metaFinanceira");
            if (metaId != 0) {
                MetaFinanceira meta = new MetaFinanceira();
                meta.setId(metaId);
                meta.setNome(rs.getString("nome_meta"));
                reserva.setMeta(meta);
            }
            return reserva;
    }
}