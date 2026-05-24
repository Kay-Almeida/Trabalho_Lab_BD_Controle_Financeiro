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
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.ValorInvalidoException;

public class MetaFinanceiraDao implements ICRUDDao<MetaFinanceira>, IMetaFinanceiraDAO {

    private Connection connection;

    // ------------------------------------------------------------------
    // Ciclo de vida da conexão
    // ------------------------------------------------------------------

    @Override
    public IMetaFinanceiraDAO open() throws SQLException {
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
    public void insert(MetaFinanceira meta) throws SQLException {
        String sql = "INSERT INTO metaFinanceira (nome, valor) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, meta.getNome());
            stmt.setDouble(2, meta.getValorMeta());
            stmt.executeUpdate();
        }
    }

    @Override
    public int update(MetaFinanceira meta) throws SQLException {
        String sql = "UPDATE metaFinanceira SET nome = ?, valor = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, meta.getNome());
            stmt.setDouble(2, meta.getValorMeta());
            stmt.setInt(3, meta.getId());
            return stmt.executeUpdate();
        }
    }

    @Override
    public void delete(MetaFinanceira meta) throws SQLException {
        String sql = "DELETE FROM metaFinanceira WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, meta.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public MetaFinanceira buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM metaFinanceira WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getMetaFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Busca uma MetaFinanceira pelo seu objeto, incluindo o total já reservado.
     * Equivalente ao antigo buscarPorObjeto() com rawQuery + JOIN.
     */
    public MetaFinanceira buscarPorObjeto(MetaFinanceira metaFinanceira) throws SQLException {
        String sql =
                "SELECT m.id, m.nome, m.valor, COALESCE(SUM(r.valor), 0) AS valor_reserva " +
                        "FROM metaFinanceira m " +
                        "LEFT JOIN reserva r ON r.metaFinanceira = m.id " +
                        "WHERE m.id = ? " +
                        "GROUP BY m.id, m.nome, m.valor";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, metaFinanceira.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    MetaFinanceira meta = new MetaFinanceira();
                    meta.setId(rs.getInt("id"));
                    meta.setNome(rs.getString("nome"));
                    meta.setValorMeta(rs.getDouble("valor"));
                    meta.setTotalReserva(rs.getDouble("valor_reserva"));
                    return meta;
                }
            }
        }
        return null;
    }

    @Override
    public List<MetaFinanceira> findAll() throws SQLException {
        List<MetaFinanceira> metas = new ArrayList<>();
        String sql = "SELECT * FROM metaFinanceira";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                metas.add(getMetaFromResultSet(rs));
            }
        }
        return metas;
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private static MetaFinanceira getMetaFromResultSet(ResultSet rs) throws SQLException {

            MetaFinanceira meta = new MetaFinanceira();
            meta.setId(rs.getInt("id"));
            meta.setNome(rs.getString("nome"));
            meta.setValorMeta(rs.getDouble("valor"));
            return meta;

    }
}
