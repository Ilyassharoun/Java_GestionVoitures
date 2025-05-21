/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import models.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Contrat;
/**
 *
 * @author samsung
 */
public class ContratDAO {
    public static int createContrat(Contrat contrat) throws SQLException {
        String sql = "INSERT INTO contrats (client_id, voiture_id, prix_journalier, duree, " +
                     "avance, mode_paiement, total, reste_a_payer, carrosserie_ok, pneus_ok, " +
                     "sieges_ok, autres_remarques, roue_secours, cric, siege_bebe, cle_roue, is_printed) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Set all parameters
            stmt.setInt(1, contrat.getClientId());
            stmt.setInt(2, contrat.getVoitureId());
            stmt.setDouble(3, contrat.getPrixJournalier());
            stmt.setInt(4, contrat.getDuree());
            stmt.setDouble(5, contrat.getAvance());
            stmt.setString(6, contrat.getModePaiement());
            stmt.setDouble(7, contrat.getTotal());
            stmt.setDouble(8, contrat.getResteAPayer());
            stmt.setBoolean(9, contrat.isCarrosserieOk());
            stmt.setBoolean(10, contrat.isPneusOk());
            stmt.setBoolean(11, contrat.isSiegesOk());
            stmt.setString(12, contrat.getAutresRemarques());
            stmt.setBoolean(13, contrat.isRoueSecours());
            stmt.setBoolean(14, contrat.isCric());
            stmt.setBoolean(15, contrat.isSiegeBebe());
            stmt.setBoolean(16, contrat.isCleRoue());
            stmt.setBoolean(17, false); // is_printed defaults to false
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating contract failed, no rows affected.");
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Creating contract failed, no ID obtained.");
                }
            }
        }
    }
    public static void updateSecondDriver(int contratId, Integer secondDriverId) throws SQLException {
        String sql = "UPDATE contrats SET second_driver_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (secondDriverId != null) {
                stmt.setInt(1, secondDriverId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, contratId);
            stmt.executeUpdate();
        }
    }
}
