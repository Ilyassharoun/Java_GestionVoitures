    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;


import models.SortieVoiture;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import models.Client;
import models.Voiture;

/**
 *
 * @author samsung
 */
public class SortieDAO {
    public List<SortieVoiture> getAllSorties() throws SQLException {
    List<SortieVoiture> sorties = new ArrayList<>();
    String sql = "SELECT id, voiture_id, plaque, client, avance, " +
                 "date_sortie, date_retour, is_reservation, date_debut, prolongee " +
                 "FROM sorties ORDER BY date_debut DESC";

    try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            SortieVoiture sortie = new SortieVoiture();
            sortie.setId(rs.getInt("id"));
            sortie.setVoitureId(rs.getInt("voiture_id"));
            sortie.setPlaque(rs.getString("plaque"));
            sortie.setClient(rs.getString("client"));
            sortie.setAvance(rs.getDouble("avance"));
            
            // Handle possible NULL date_sortie for reservations
            java.sql.Date dateSortie = rs.getDate("date_sortie");
            sortie.setDateSortie(dateSortie != null ? new Date(dateSortie.getTime()) : null);
            
            sortie.setDateRetour(new Date(rs.getDate("date_retour").getTime()));
            sortie.setReservation(rs.getBoolean("is_reservation"));
            
            java.sql.Date dateDebut = rs.getDate("date_debut");
            sortie.setDateDebut(dateDebut != null ? new Date(dateDebut.getTime()) : null);
            
            sortie.setProlongee(rs.getBoolean("prolongee"));
            
            sorties.add(sortie);
        }
    }
    return sorties;
}
    public boolean enregistrerSortie(SortieVoiture sortie) throws SQLException {
    String sql = "INSERT INTO sorties (voiture_id, plaque, client, avance, " +
               "date_sortie, date_retour, is_reservation, date_debut, prolongee) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, sortie.getVoitureId());
        pstmt.setString(2, sortie.getPlaque());
        pstmt.setString(3, sortie.getClient());
        pstmt.setDouble(4, sortie.getAvance());
        
        // Handle date_sortie (can be null for reservations)
        if (sortie.getDateSortie() != null) {
            pstmt.setDate(5, new java.sql.Date(sortie.getDateSortie().getTime()));
        } else {
            pstmt.setNull(5, Types.DATE);
        }
        
        // date_retour should never be null
        pstmt.setDate(6, new java.sql.Date(sortie.getDateRetour().getTime()));
        
        pstmt.setBoolean(7, sortie.isReservation());
        
        // date_debut should never be null
        pstmt.setDate(8, new java.sql.Date(sortie.getDateDebut().getTime()));
        
        pstmt.setBoolean(9, sortie.isProlongee());
        
        return pstmt.executeUpdate() > 0;
    }
}
    public boolean enregistrerRetour(int sortieId) throws SQLException {
    // Use the current date as the return date
    String sql = "UPDATE sorties SET date_retour = ? WHERE id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
        stmt.setInt(2, sortieId);
        return stmt.executeUpdate() > 0;
    }
}
    // In SortieDAO.java
public boolean annulerLocation(int sortieId) throws SQLException {
    // This handles both cases - whether car was picked up or not
    String sql = "UPDATE sorties SET est_annule = true, date_retour = ? WHERE id = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        // Set return date to current date if car was picked up (date_sortie exists)
        // Otherwise set to null (reservation that wasn't picked up)
        stmt.setTimestamp(1, 
            isCarPickedUp(sortieId) ? new Timestamp(new Date().getTime()) : null);
        stmt.setInt(2, sortieId);
        
        return stmt.executeUpdate() > 0;
    }
}

private boolean isCarPickedUp(int sortieId) throws SQLException {
    String sql = "SELECT date_sortie FROM sorties WHERE id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, sortieId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getTimestamp("date_sortie") != null;
    }
}

public boolean annulerReservation(int reservationId) throws SQLException {
    // Delete the reservation record
    String sql = "DELETE FROM sorties WHERE id = ? AND is_reservation = true";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, reservationId);
        return stmt.executeUpdate() > 0;
    }
}

    public List<SortieVoiture> getSortiesActives() throws SQLException {
    String sql = "SELECT * FROM sorties WHERE date_sortie IS NOT NULL " +
                 "AND (date_retour IS NULL OR date_retour > CURRENT_TIMESTAMP) " +
                 "AND is_reservation = false";
    
    List<SortieVoiture> sorties = new ArrayList<>();
    try (Connection conn = DBConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            sorties.add(mapResultSetToSortie(rs));
        }
    }
    return sorties;
}
    public boolean prolongerLocation(int sortieId, java.util.Date nouvelleDateRetour) throws SQLException {
    String query = "UPDATE sorties SET date_retour = ?, prolongee = TRUE WHERE id = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        // Convert java.util.Date to java.sql.Date safely
        java.sql.Date sqlDate = new java.sql.Date(nouvelleDateRetour.getTime());
        stmt.setDate(1, sqlDate);
        stmt.setInt(2, sortieId);
        
        return stmt.executeUpdate() > 0;
    }
}  
    // Updates reservations to active when their start date arrives
public void updateReservationStatuses() throws SQLException {
    Date now = new Date();
    
    // 1. Activate reservations whose start date has arrived
    String activateSql = "UPDATE sorties SET is_reservation = false, date_sortie = ? " +
                       "WHERE is_reservation = true AND date_debut <= ?";
    
    // 2. Mark overdue rentals
    String overdueSql = "UPDATE sorties SET is_overdue = true " +
                      "WHERE is_reservation = false AND date_retour < ? AND date_retour_effectif IS NULL";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement activateStmt = conn.prepareStatement(activateSql);
         PreparedStatement overdueStmt = conn.prepareStatement(overdueSql)) {
        
        // Activate reservations
        activateStmt.setTimestamp(1, new Timestamp(now.getTime()));
        activateStmt.setTimestamp(2, new Timestamp(now.getTime()));
        activateStmt.executeUpdate();
        
        // Mark overdue rentals
        overdueStmt.setTimestamp(1, new Timestamp(now.getTime()));
        overdueStmt.executeUpdate();
    }
}
// In SortieDAO.java
public void updateReservationsToActive() throws SQLException {
    String sql = "UPDATE sorties SET is_reservation = false, date_sortie = ? " +
               "WHERE is_reservation = true AND date_debut <= ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        Timestamp now = new Timestamp(new Date().getTime());
        stmt.setTimestamp(1, now);
        stmt.setTimestamp(2, now);
        stmt.executeUpdate();
    }
}

// For return processi
    // In SortieDAO.java

// Get active rentals


// Get reservations for a vehicle
public List<SortieVoiture> getReservationsByVoiture(int voitureId) throws SQLException {
    String sql = "SELECT * FROM sorties WHERE voiture_id = ? AND is_reservation = true " +
                 "ORDER BY date_debut";
    
    List<SortieVoiture> reservations = new ArrayList<>();
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, voitureId);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reservations.add(mapResultSetToSortie(rs));
            }
        }
    }
    return reservations;
}

// Record return
private SortieVoiture mapResultSetToSortie(ResultSet rs) throws SQLException {
    SortieVoiture sortie = new SortieVoiture();
    
    // Required fields (non-null in database)
    sortie.setId(rs.getInt("id"));
    sortie.setVoitureId(rs.getInt("voiture_id"));
    sortie.setPlaque(rs.getString("plaque"));
    sortie.setClient(rs.getString("client"));
    sortie.setAvance(rs.getDouble("avance"));
    sortie.setReservation(rs.getBoolean("is_reservation"));
    
    // Handle nullable dates
    Timestamp dateDebut = rs.getTimestamp("date_debut");
    sortie.setDateDebut(dateDebut != null ? new Date(dateDebut.getTime()) : null);
    
    Timestamp dateRetour = rs.getTimestamp("date_retour");
    sortie.setDateRetour(dateRetour != null ? new Date(dateRetour.getTime()) : null);
    
    Timestamp dateSortie = rs.getTimestamp("date_sortie");
    sortie.setDateSortie(dateSortie != null ? new Date(dateSortie.getTime()) : null);
    
    return sortie;
}
public int activateEligibleReservations() throws SQLException {
    String sql = "UPDATE sorties SET is_reservation = false, date_sortie = ? " +
                 "WHERE is_reservation = true " +
                 "AND date_debut <= ? " +  // Reservation start date has arrived
                 "AND date_sortie IS NULL " + // Not already marked as out
                 "AND est_annule = false"; // Not canceled
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        Date now = new Date();
        stmt.setDate(1, new java.sql.Date(now.getTime())); // Set date_sortie to now
        stmt.setDate(2, new java.sql.Date(now.getTime())); // Compare with date_debut
        
        return stmt.executeUpdate(); // Returns number of rows updated
    }
}
//contrarts 
// Get all reservations (all entries in sorties table)
public static List<SortieVoiture> getAllSorties2() throws SQLException {
        String sql = "SELECT * FROM sorties";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            List<SortieVoiture> sorties = new ArrayList<>();
            while (rs.next()) {
                SortieVoiture sortie = new SortieVoiture();
                sortie.setId(rs.getInt("id"));
                sortie.setClient(rs.getString("client"));
                sortie.setPlaque(rs.getString("plaque"));
                sortie.setDateSortie(rs.getDate("date_sortie"));
                sortie.setAvance(rs.getDouble("avance"));
                sorties.add(sortie);
            }
            return sorties;
        }
    }

// Get a specific sortie by client name
public static SortieVoiture getSortieByClientName(String nomClient) throws SQLException {
    String sql = "SELECT * FROM sorties WHERE client = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, nomClient);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                SortieVoiture sortie = new SortieVoiture();
                sortie.setId(rs.getInt("id"));
                sortie.setClient(rs.getString("client"));
                sortie.setPlaque(rs.getString("plaque"));
                sortie.setDateSortie(rs.getDate("date_sortie"));
                sortie.setAvance(rs.getDouble("avance"));
                return sortie;
            }
        }
    }
    return null;
}
public static List<SortieVoiture> getAllReservationsWithVehicles() throws SQLException {
    String sql = "SELECT s.*, v.marque, v.modele, v.plaque_immatriculation as plaque " +
                 "FROM sorties s " +
                 "JOIN voitures v ON s.voiture_id = v.id";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        List<SortieVoiture> reservations = new ArrayList<>();
        while (rs.next()) {
            SortieVoiture reservation = new SortieVoiture();
            // Set basic fields
            reservation.setId(rs.getInt("id"));
            reservation.setClient(rs.getString("client"));
            reservation.setVoitureId(rs.getInt("voiture_id"));
            reservation.setDateSortie(rs.getDate("date_sortie"));
            reservation.setAvance(rs.getDouble("avance"));
            
            // Set vehicle information - CRITICAL PART
            reservation.setMarque(rs.getString("marque"));
            reservation.setModele(rs.getString("modele"));
            reservation.setImmatriculation(rs.getString("plaque")); // Note the alias
            
            // Set reservation status
            reservation.setReservation(rs.getBoolean("is_reservation"));
            
            reservations.add(reservation);
        }
        return reservations;
    }
}
 // Temporary debug method - add this to your DAO class
public static void debugQueryResults() throws SQLException {
    String sql = "SELECT s.*, v.marque, v.modele, v.plaque_immatriculation " +
                 "FROM sorties s " +
                 "JOIN voitures v ON s.voiture_id = v.id";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        System.out.println("Debugging query results:");
        System.out.println("ID | Client | Marque | Modele | Plaque");
        while (rs.next()) {
            System.out.println(
                rs.getInt("id") + " | " +
                rs.getString("client") + " | " +
                rs.getString("marque") + " | " +
                rs.getString("modele") + " | " +
                rs.getString("plaque_immatriculation")
            );
        }
    }
}
    // Get specific reservation with vehicle info
    public static SortieVoiture getReservationWithVehicle(String nomClient) throws SQLException {
        String sql = "SELECT s.*, v.marque, v.modele, v.plaque_immatriculation,s.date_sortie, s.date_retour " +
                     "FROM sorties s " +
                     "JOIN voitures v ON s.voiture_id = v.id " +
                     "WHERE s.client = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomClient);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    SortieVoiture reservation = new SortieVoiture();
                    reservation.setId(rs.getInt("id"));
                    reservation.setClient(rs.getString("client"));
                    reservation.setVoitureId(rs.getInt("voiture_id"));
                    reservation.setDateSortie(rs.getDate("date_sortie"));
                    reservation.setAvance(rs.getDouble("avance"));
                    reservation.setModele(rs.getString("modele"));
                    reservation.setDateReservation(rs.getDate("date_debut"));
                    
                    
                    // Vehicle information
                    reservation.setMarque(rs.getString("marque"));
                    reservation.setImmatriculation(rs.getString("plaque"));
                    reservation.setDateSortie(rs.getDate("date_sortie"));
                reservation.setDateRetour(rs.getDate("date_retour"));
                    return reservation;
                }
            }
        }
        return null;
    }
    public static int calculateDuree(int sortieId) throws SQLException {
    String sql = "SELECT DATEDIFF(date_retour, date_sortie) FROM sorties WHERE id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, sortieId);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    }
    return 0;
}
    public static SortieVoiture getReservationByClientName(String nomClient) throws SQLException {
    String sql = "SELECT s.*, v.marque, v.plaque_immatriculation " +
                "FROM sorties s " +
                "JOIN voitures v ON s.voiture_id = v.id " +
                "WHERE s.client = ? "; // Assuming you have an 'etat' field
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, nomClient);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                SortieVoiture reservation = new SortieVoiture();
                // Basic reservation info
                reservation.setId(rs.getInt("id"));
                reservation.setClient(rs.getString("client"));
                reservation.setVoitureId(rs.getInt("voiture_id"));
                reservation.setDateSortie(rs.getDate("date_sortie"));
                reservation.setDateRetour(rs.getDate("date_retour"));
                reservation.setAvance(rs.getDouble("avance"));
                reservation.setReservation(rs.getBoolean("is_reservation")); 
                
                // Vehicle info from join
                reservation.setMarque(rs.getString("marque"));
                reservation.setImmatriculation(rs.getString("plaque"));
                
                
                return reservation;
            }
        }
    }
    return null; // No reservation found
}
    public static List<SortieVoiture> getActiveReservations() throws SQLException {
        // Use date_retour to determine if reservation is completed
        String sql = "SELECT sv.* FROM sorties sv " +
                     "WHERE sv.date_retour > CURRENT_DATE OR sv.date_retour IS NULL";
        
        List<SortieVoiture> reservations = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                SortieVoiture reservation = mapResultSetToSortieVoiture(rs);
                reservations.add(reservation);
            }
        }
        return reservations;
    }

    /**
     * Marks a reservation as completed by setting return date
     */
    public static boolean completeReservation(int sortieId) throws SQLException {
        String sql = "UPDATE sorties SET date_retour = CURRENT_DATE " +
                     "WHERE id = ? AND date_retour > CURRENT_DATE";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sortieId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

 private static SortieVoiture mapResultSetToSortieVoiture(ResultSet rs) throws SQLException {
    SortieVoiture sortie = new SortieVoiture();
    
    // Required fields
    sortie.setId(rs.getInt("id"));
    sortie.setVoitureId(rs.getInt("voiture_id"));  // This was missing
    sortie.setClient(rs.getString("client"));
    
    // Optional fields with null checks
    sortie.setAvance(rs.getDouble("avance"));
    if (rs.wasNull()) sortie.setAvance(0.0);
    
    // Date fields
    sortie.setDateSortie(rs.getTimestamp("date_sortie"));
    sortie.setDateRetour(rs.getTimestamp("date_retour"));
    sortie.setDateDebut(rs.getTimestamp("date_debut"));
    
    // Boolean flags
    sortie.setProlongee(rs.getBoolean("prolongee"));
    sortie.setReservation(rs.getBoolean("is_reservation"));
    
    // Vehicle info (from join)
    sortie.setMarque(rs.getString("marque"));
    sortie.setModele(rs.getString("modele"));
    sortie.setImmatriculation(rs.getString("plaque_immatriculation"));
    
    return sortie;
}
public static List<SortieVoiture> getActiveReservationsWithVehicles() throws SQLException {
    // Triple-check column names match your database exactly
    String sql = "SELECT "
            + "sv.id, "
            + "sv.voiture_id, " 
            + "sv.client, "
            + "sv.avance, "
            + "sv.date_sortie, "
            + "sv.date_retour, "
            + "sv.prolongee, "
            + "sv.is_reservation, "
            + "sv.date_debut, "
            + "v.marque, "
            + "v.modele, "
            + "v.plaque_immatriculation "
            + "FROM sorties sv "
            + "LEFT JOIN voitures v ON sv.voiture_id = v.id "
            + "WHERE sv.date_retour > CURRENT_DATE OR sv.date_retour IS NULL";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
       
        
        try (ResultSet rs = stmt.executeQuery()) {
            List<SortieVoiture> reservations = new ArrayList<>();
            while (rs.next()) {
                reservations.add(mapResultSetToSortieVoiture(rs));
            }
            return reservations;
        }
    }
}
}
