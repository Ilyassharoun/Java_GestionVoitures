/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author samsung
 */
import models.Voiture;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import models.SortieVoiture;

public class VoitureDAO {
    // In VoitureDAO.java
public static List<Voiture> getAllVoitures() throws SQLException {
    List<Voiture> voitures = new ArrayList<>();
    String sql = "SELECT * FROM voitures";
    
    try (Connection conn = DBConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            Voiture v = new Voiture();
            v.setId(rs.getInt("id"));
            v.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
            v.setMarque(rs.getString("marque"));
            v.setModele(rs.getString("modele"));
            v.setAnnee(rs.getInt("annee"));
            v.setCouleur(rs.getString("couleur"));
            v.setKilometrage(rs.getInt("kilometrage"));
            v.setDernierVidange(rs.getInt("dernier_vidange_km"));
            voitures.add(v);
        }
    }
    return voitures;
}

public static void addVoiture(Voiture v) throws SQLException {
    String sql = "INSERT INTO voitures (plaque_immatriculation, marque, modele,annee,couleur, kilometrage, dernier_vidange_km) VALUES (?, ?, ?, ?,?,?,?)";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, v.getPlaqueImmatriculation());
        stmt.setString(2, v.getMarque());
        stmt.setString(3, v.getModele());
        stmt.setInt(4, v.getAnnee ());
        stmt.setString(5, v.getCouleur());
        stmt.setInt(6, v.getKilometrage());
        stmt.setInt(7, v.getDernierVidange());
        stmt.executeUpdate();
    }
}
    // Create
    /*public static void ajouterVoiture(Voiture voiture) throws SQLException {
        String sql = "INSERT INTO voitures (plaque_immatriculation, marque, modele, kilometrage) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, voiture.getPlaqueImmatriculation());
            stmt.setString(2, voiture.getMarque());
            stmt.setString(3, voiture.getModele());
            stmt.setInt(4, voiture.getKilometrage());
            stmt.executeUpdate();
        }
    }*/

    // Read All
    public static List<Voiture> listerVoitures() throws SQLException {
        List<Voiture> voitures = new ArrayList<>();
        String sql = "SELECT * FROM voitures";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Voiture v = new Voiture();
                v.setId(rs.getInt("id"));
                v.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
                v.setMarque(rs.getString("marque"));
                v.setModele(rs.getString("modele"));
                v.setAnnee(rs.getInt("annee"));
                v.setCouleur(rs.getString("couleur"));
                v.setKilometrage(rs.getInt("kilometrage"));
                v.setDernierVidange(rs.getInt("dernier_vidange_km"));
                voitures.add(v);
            }
        }
        return voitures;
    }

    // Update
    public static void modifierVoiture(String oldPlaque, Voiture newData) throws SQLException {
        String sql = "UPDATE voitures SET plaque_immatriculation=?, marque=?, modele=?,annee=?,couleur=?, kilometrage=?,dernier_vidange_km=? WHERE plaque_immatriculation=?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, newData.getPlaqueImmatriculation());
        stmt.setString(2, newData.getMarque());
        stmt.setString(3, newData.getModele());
        stmt.setInt(4, newData.getAnnee ());
        stmt.setString(5, newData.getCouleur());
        stmt.setInt(6, newData.getKilometrage());
        stmt.setInt(7, newData.getDernierVidange());
        stmt.setString(8, oldPlaque);
        
        
        stmt.executeUpdate();
    }
    }
    public static List<Voiture> searchVoitures(String keyword) throws SQLException {
    List<Voiture> results = new ArrayList<>();
    String sql = "SELECT * FROM voitures WHERE plaque_immatriculation LIKE ? OR marque LIKE ? OR modele LIKE ? OR couleur LIKE ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        String likeParam = "%" + keyword + "%";
        stmt.setString(1, likeParam);
        stmt.setString(2, likeParam);
        stmt.setString(3, likeParam);
        stmt.setString(4, likeParam);
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Voiture v = new Voiture();
            v.setId(rs.getInt("id"));
            v.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
            v.setMarque(rs.getString("marque"));
            v.setModele(rs.getString("modele"));
            v.setAnnee(rs.getInt("annee"));
            v.setCouleur(rs.getString("couleur"));
            v.setKilometrage(rs.getInt("kilometrage"));
            v.setDernierVidange(rs.getInt("dernier_vidange_km"));
            results.add(v);
        }
    }
    return results;
    }
    // Delete
    public static void supprimerVoiture(String plaque) throws SQLException {
        String sql = "DELETE FROM voitures WHERE plaque_immatriculation=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, plaque);
            stmt.executeUpdate();
        }
    }
    public static Voiture getVoitureByPlaque(String plaque) throws SQLException {
    String sql = "SELECT * FROM voitures WHERE plaque_immatriculation = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, plaque);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            Voiture v = new Voiture();
            v.setId(rs.getInt("id"));
            v.setPlaqueImmatriculation(plaque);
            v.setMarque(rs.getString("marque"));
            v.setModele(rs.getString("modele"));
            v.setAnnee(rs.getInt("annee"));
            v.setCouleur(rs.getString("couleur"));
            v.setKilometrage(rs.getInt("kilometrage"));
            v.setDernierVidange(rs.getInt("dernier_vidange_km"));
            return v;
        }
    }
    throw new SQLException("Véhicule non trouvé");
}

    // Search
    public static List<Voiture> rechercherVoitures(String searchText) throws SQLException {
        List<Voiture> voitures = new ArrayList<>();
        String sql = "SELECT * FROM voitures WHERE plaque_immatriculation LIKE ? OR marque LIKE ? OR modele LIKE ? OR annee LIKE ? OR couleur LIKE ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String likeParam = "%" + searchText + "%";
            stmt.setString(1, likeParam);
            stmt.setString(2, likeParam);
            stmt.setString(3, likeParam);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Voiture v = new Voiture();
                v.setId(rs.getInt("id"));
                v.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
                v.setMarque(rs.getString("marque"));
                v.setModele(rs.getString("modele"));
                v.setAnnee(rs.getInt("annee"));
                v.setCouleur(rs.getString("couleur"));
                v.setKilometrage(rs.getInt("kilometrage"));
                v.setDernierVidange(rs.getInt("dernier_vidange_km"));
                voitures.add(v);
            }
        }
        return voitures;
    }
    public static void mettreAJourVidange(String plaque) throws SQLException {
    // This updates only the dernier_vidange_km column
    // prochain_vidange_km will auto-calculate as dernier_vidange_km + 10000
    String sql = "UPDATE voitures SET dernier_vidange_km = kilometrage WHERE plaque_immatriculation = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, plaque);
        stmt.executeUpdate();
    }
}
    public static List<Voiture> getAllVoituresWithDetails() throws SQLException {
        List<Voiture> voitures = new ArrayList<>();
        String query = "SELECT id, plaque_immatriculation, marque, modele FROM voitures";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Voiture v = new Voiture();
                v.setId(rs.getInt("id"));
                v.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
                v.setMarque(rs.getString("marque"));
                v.setModele(rs.getString("modele"));
                voitures.add(v);
            }
        }
        return voitures;
    }
    
    // Get car ID by license plate
    public static int getVoitureIdByImmatriculation(String immatriculation) throws SQLException {
        String query = "SELECT id FROM voitures WHERE plaque_immatriculation = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, immatriculation);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }

public static int getCurrentKilometrage(String immatriculation) throws SQLException {
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(
             "SELECT kilometrage FROM voitures WHERE plaque_immatriculation = ?")) {
        
        stmt.setString(1, immatriculation);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt("kilometrage") : -1;
    }
}
// In VoitureDAO.java
public static Voiture getVoitureById(int id) throws SQLException {
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(
             "SELECT * FROM voitures WHERE id = ?")) {
        
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            Voiture v = new Voiture();
            v.setId(id);
            v.setPlaqueImmatriculation(rs.getString("palque_immatriculation"));
            v.setMarque(rs.getString("marque"));
            v.setModele(rs.getString("modele"));
            return v;
        }
        return null;
    }
}
// In VoitureDAO.java
public static Voiture getVoitureByImmatriculation(String immatriculation) throws SQLException {
    String query = "SELECT id, immatriculation FROM voitures WHERE plaque_immatriculation LIKE ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, "%" + immatriculation + "%");
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            Voiture v = new Voiture();
            v.setId(rs.getInt("id"));
            v.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
            return v;
        }
        return null;
    }
}
public static void updateDernierChangementPneus(String immatriculation, java.sql.Date date) throws SQLException {
    String sql = "UPDATE voitures SET dernier_changement_pneus = ? WHERE plaque_immatriculation = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setDate(1, date);
        stmt.setString(2, immatriculation);
        stmt.executeUpdate();
    }
}
public static int getProchainChangementPneusKm(String plaqueImmatriculation) throws SQLException {
    String sql = "SELECT prochain_changement_pneus_km FROM voitures WHERE plaque_immatriculation = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, plaqueImmatriculation);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("prochain_changement_pneus_km");
        }
        throw new SQLException("Prochain changement pneus non trouvé");
    }
}
// When adding a new car
public static void setInitialPneusAlert(String plaque, int currentKm) throws SQLException {
    int nextChange = currentKm + 20000; // First alert at 20,000 km
    String sql = "UPDATE voitures SET prochain_changement_pneus_km = ? WHERE plaque_immatriculation = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, nextChange);
        pstmt.setString(2, plaque);
        pstmt.executeUpdate();
    }
}

// When marking tires as changed
public static void mettreAJourPneus(String plaqueImmatriculation) throws SQLException {
    // Get current mileage first
    int currentKm = getKilometrage(plaqueImmatriculation);
    
    String sql = """
        UPDATE voitures 
        SET dernier_changement_pneus_km = ?,
            prochain_changement_pneus_km = ? + 20000
        WHERE plaque_immatriculation = ?""";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, currentKm);
        pstmt.setInt(2, currentKm);
        pstmt.setString(3, plaqueImmatriculation);
        pstmt.executeUpdate();
    }
}

/**
 * Alternative method using vehicle ID instead of plate
 * @param voitureId The vehicle ID
 * @return true if update was successful
 * @throws SQLException If database error occurs
 */
public static boolean marquerPneusCommeFaits(int voitureId) throws SQLException {
    String sql = """
        UPDATE voitures 
        SET dernier_changement_pneus_km = kilometrage,
            prochain_changement_pneus_km = kilometrage + 20000
        WHERE id = ?""";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, voitureId);
        return pstmt.executeUpdate() > 0;
    }
}
public static void updateProchainChangementPneus(String immatriculation, java.sql.Date date) throws SQLException {
    String sql = "UPDATE voitures SET prochain_changement_pneus = ? WHERE plaque_immatriculation = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setDate(1, date);
        stmt.setString(2, immatriculation);
        stmt.executeUpdate();
    }
}
// Get current tire change information
public static int[] getPneusInfo(String plaque) throws SQLException {
    String sql = "SELECT kilometrage, dernier_changement_pneus_km, prochain_changement_pneus_km " +
                 "FROM voitures WHERE plaque_immatriculation = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, plaque);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            return new int[]{
                rs.getInt("kilometrage"),
                rs.getInt("dernier_changement_pneus_km"),
                rs.getInt("prochain_changement_pneus_km")
            };
        }
        throw new SQLException("Véhicule non trouvé");
    }
}

// Update tire change information
public static void updatePneus(String plaque) throws SQLException {
    int[] info = getPneusInfo(plaque);
    int currentKm = info[0];
    int nextChangeKm = currentKm + 20000; // Next change in 20,000 km
    
    String sql = "UPDATE voitures SET " +
                 "dernier_changement_pneus_km = ?, " +
                 "prochain_changement_pneus_km = ? " +
                 "WHERE plaque_immatriculation = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, currentKm);
        pstmt.setInt(2, nextChangeKm);
        pstmt.setString(3, plaque);
        
        int updated = pstmt.executeUpdate();
        if (updated == 0) {
            throw new SQLException("Échec de la mise à jour");
        }
    }
}
//-----------------------------------
public static boolean vehicleExists(String plaque) throws SQLException {
    String sql = "SELECT COUNT(*) FROM voitures WHERE plaque_immatriculation = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, plaque);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }
}
public static int getKilometrage(String plaque) throws SQLException {
    String sql = "SELECT kilometrage FROM voitures WHERE plaque_immatriculation = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, plaque);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("kilometrage");
        }
        throw new SQLException("Véhicule non trouvé pour la plaque: " + plaque);
    }
}

public static int getProchainVidangeKm(String plaque) throws SQLException {
    String sql = "SELECT prochain_vidange_km FROM voitures WHERE plaque_immatriculation = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, plaque);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("prochain_vidange_km");
        }
        throw new SQLException("Véhicule non trouvé");
    }
}
public static int getVehicleIdByPlate(String plaque) throws SQLException {
    String sql = "SELECT id FROM voitures WHERE plaque_immatriculation = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, plaque);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt("id") : -1;
    }
}

/*public static int getProchainChangementPneusKm(String plaque) throws SQLException {
    String sql = "SELECT dernier_changement_pneus_km + 20000 FROM voitures WHERE plaque_immatriculation = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, plaque);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        throw new SQLException("Véhicule non trouvé");
    }
}*/
//-------------SortieVoiture----------------

// In VoitureDAO.java
// In VoitureDAO.java
public static void updateControleTechnique(String plaque, Date controleDate) throws SQLException {
    String sql = "UPDATE voitures " +
                 "SET dernier_controle_technique = ?, " +
                 "prochain_controle_technique = DATE_ADD(?, INTERVAL 6 MONTH) " +
                 "WHERE immatriculation = ?";
    
    try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setDate(1, new java.sql.Date(controleDate.getTime()));
        stmt.setDate(2, new java.sql.Date(controleDate.getTime()));
        stmt.setString(3, plaque);
        stmt.executeUpdate();
    }
}
public boolean isAvailableForRental(int voitureId, Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sorties WHERE voiture_id = ? AND " +
                     "((date_debut <= ? AND date_retour >= ?) OR " +
                     "(date_debut BETWEEN ? AND ?) OR " +
                     "(date_retour BETWEEN ? AND ?)) AND " +
                     "is_reservation = false";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, voitureId);
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));
            stmt.setTimestamp(3, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(4, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(5, new Timestamp(endDate.getTime()));
            stmt.setTimestamp(6, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(7, new Timestamp(endDate.getTime()));
            
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        }
    }
public boolean hasOnlyFutureReservations(int voitureId, Date startDate, Date endDate) throws SQLException {
    String sql = "SELECT COUNT(*) FROM sorties WHERE voiture_id = ? AND " +
               "((date_retour > ? AND date_debut < ?) AND " +
               "(is_reservation = true AND date_debut > ?))";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, voitureId);
        pstmt.setDate(2, new java.sql.Date(startDate.getTime()));
        pstmt.setDate(3, new java.sql.Date(endDate.getTime()));
        pstmt.setDate(4, new java.sql.Date(new Date().getTime()));
        
        ResultSet rs = pstmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }
}
public boolean hasActiveRental(int voitureId, Date startDate, Date endDate) throws SQLException {
    String sql = "SELECT COUNT(*) FROM sorties WHERE voiture_id = ? " +
               "AND is_reservation = false " +
               "AND ((date_sortie < ? AND date_retour > ?) OR " +
               "(date_sortie < ? AND date_retour > ?))";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, voitureId);
        pstmt.setDate(2, new java.sql.Date(endDate.getTime()));
        pstmt.setDate(3, new java.sql.Date(startDate.getTime()));
        pstmt.setDate(4, new java.sql.Date(endDate.getTime()));
        pstmt.setDate(5, new java.sql.Date(startDate.getTime()));
        
        ResultSet rs = pstmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }
}
public boolean hasOverlappingReservation(int voitureId, Date startDate, Date endDate) throws SQLException {
    String sql = "SELECT COUNT(*) FROM sorties WHERE voiture_id = ? AND is_reservation = true " +
                 "AND ((date_debut BETWEEN ? AND ?) OR (date_retour BETWEEN ? AND ?) " +
                 "OR (date_debut <= ? AND date_retour >= ?))";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, voitureId);
        stmt.setTimestamp(2, new Timestamp(startDate.getTime()));
        stmt.setTimestamp(3, new Timestamp(endDate.getTime()));
        stmt.setTimestamp(4, new Timestamp(startDate.getTime()));
        stmt.setTimestamp(5, new Timestamp(endDate.getTime()));
        stmt.setTimestamp(6, new Timestamp(startDate.getTime()));
        stmt.setTimestamp(7, new Timestamp(endDate.getTime()));
        
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }
}
public boolean isBeforeAllReservations(int voitureId, Date endDate) throws SQLException {
    String sql = "SELECT COUNT(*) FROM sorties WHERE voiture_id = ? " +
               "AND is_reservation = true " +
               "AND date_debut <= ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, voitureId);
        pstmt.setDate(2, new java.sql.Date(endDate.getTime()));
        
        ResultSet rs = pstmt.executeQuery();
        return rs.next() && rs.getInt(1) == 0;
    }
}
 public boolean isAvailableForReservation(int voitureId, Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sorties WHERE voiture_id = ? AND " +
                     "((date_debut <= ? AND date_retour >= ?) OR " +
                     "(date_debut BETWEEN ? AND ?) OR " +
                     "(date_retour BETWEEN ? AND ?))";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, voitureId);
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));
            stmt.setTimestamp(3, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(4, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(5, new Timestamp(endDate.getTime()));
            stmt.setTimestamp(6, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(7, new Timestamp(endDate.getTime()));
            
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        }
    }


public Date getFirstReservationDate(int voitureId) throws SQLException {
    String sql = "SELECT MIN(date_debut) FROM sorties WHERE voiture_id = ? AND is_reservation = true AND date_debut > ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, voitureId);
        stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
        
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getTimestamp(1) : null;
    }
}
public boolean isAvailable(int voitureId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM sorties WHERE voiture_id = ? "
               + "AND (date_retour > NOW() OR is_reservation = true)";
    
    try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, voitureId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getInt(1) == 0;
    }
}
public static  void updateControleTechnique(Voiture voiture) throws SQLException {
    String sql = "UPDATE voitures SET dernier_controle_technique = ?, prochain_controle_technique = ? WHERE id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setDate(1, new java.sql.Date(voiture.getDernierControleTechnique().getTime()));
        stmt.setDate(2, new java.sql.Date(voiture.getProchainControleTechnique().getTime()));
        stmt.setInt(3, voiture.getId());
        stmt.executeUpdate();
    }
}
//for controll
public Voiture getVoitureByIdControll(int id) throws SQLException {
    String sql = "SELECT id, plaque_immatriculation, marque, modele, dernier_controle_technique FROM voitures WHERE id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Voiture v = new Voiture();
            v.setId(rs.getInt("id"));
            v.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));  // Correct column name
            v.setMarque(rs.getString("marque"));
            v.setModele(rs.getString("modele"));
            v.setDernierControleTechnique(rs.getDate("dernier_controle_technique"));
            return v;
        }
    }
    return null;
}
public static void mettreAJourControleTechnique(String plaqueImmatriculation) throws SQLException {
    String sql = """
        UPDATE voitures 
        SET dernier_controle_technique = prochain_controle_technique,
            prochain_controle_technique = DATE_ADD(prochain_controle_technique, INTERVAL 6 MONTH)
        WHERE plaque_immatriculation = ?""";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, plaqueImmatriculation);
        pstmt.executeUpdate();
    }
}
public static Date getProchainControleTechnique(String plaqueImmatriculation) throws SQLException {
    String sql = "SELECT prochain_controle_technique FROM voitures WHERE plaque_immatriculation = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, plaqueImmatriculation);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDate("prochain_controle_technique");
            }
        }
    }
    throw new SQLException("Véhicule non trouvé ou date de contrôle technique non définie");
}
//contrats frame
public static List<Voiture> getAvailableVoitures() throws SQLException {
    String sql = "SELECT * FROM voitures";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        List<Voiture> voitures = new ArrayList<>();
        while (rs.next()) {
            Voiture voiture = new Voiture();
            voiture.setId(rs.getInt("id"));
            voiture.setMarque(rs.getString("marque"));
            voiture.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
            // Add other fields as needed
            voitures.add(voiture);
        }
        return voitures;
    }
}

public static Voiture getVoitureByImmatriculation2(String immatriculation) throws SQLException {
    String sql = "SELECT * FROM voitures WHERE plaque_immatriculation = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, immatriculation);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Voiture voiture = new Voiture();
                voiture.setId(rs.getInt("id"));
                voiture.setMarque(rs.getString("marque"));
                voiture.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
                return voiture;
            }
        }
    }
    return null;
}
}
