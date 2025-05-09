package dao;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import models.Alerte;
import models.Voiture;
import java.util.Date;

public class AlerteDAO {
    private static final int VIDANGE_INTERVAL = 10000; // Oil change every 10,000 km
    private static final int PNEUS_INTERVAL = 20000;   // Tire change every 20,000 km
    private static final int ASSURANCE_ALERT_DAYS = 30; // Insurance alert 30 days before expiry
    
    public static List<Alerte> getToutesAlertes() throws SQLException {
        List<Alerte> alertes = new ArrayList<>();
        alertes.addAll(getAlertesVidange());
        alertes.addAll(getAlertesPneus());
        alertes.addAll(getAlertesAssurances());
        return alertes;
    }

    
    public static List<Alerte> getAlertesVidange() throws SQLException {
    List<Alerte> alertes = new ArrayList<>();
    String sql = """
        SELECT v.id, v.plaque_immatriculation, v.marque, v.kilometrage,
               v.prochain_vidange_km, 
               (v.prochain_vidange_km - v.kilometrage) AS km_restant
        FROM voitures v
        WHERE v.prochain_vidange_km IS NOT NULL
        ORDER BY km_restant ASC""";  // Removed the urgency filter

    try (Connection conn = DBConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            int kmLeft = rs.getInt("km_restant");
            String plaque = rs.getString("plaque_immatriculation");
            String marque = rs.getString("marque");
            int voitureId = rs.getInt("id");
            
            // Determine status based on km remaining
            String urgency, details;
            if (kmLeft <= 500) {
                urgency = "URGENT";
                details = "Changez l'huile immédiatement !";
            } else if (kmLeft <= 1000) {
                urgency = "ATTENTION";
                details = "Vidange approchante (" + kmLeft + " km restants)";
            } else {
                urgency = "SAFE";
                details = "Vidange prévue dans " + kmLeft + " km";
            }
            
            Voiture voiture = new Voiture();
            voiture.setId(voitureId);
            voiture.setPlaqueImmatriculation(plaque);
            voiture.setMarque(marque);
            
            alertes.add(new Alerte(
                "Vidange",
                plaque + " (" + marque + ")",
                kmLeft + " km",
                urgency,
                details,
                voiture
            ));
        }
    }
    return alertes;
}

    public static List<Alerte> getAlertesPneus() throws SQLException {
    List<Alerte> alertes = new ArrayList<>();
    String sql = """
        SELECT v.plaque_immatriculation, v.marque, v.kilometrage,
               v.dernier_changement_pneus_km,
               v.prochain_changement_pneus_km,
               (v.prochain_changement_pneus_km - v.kilometrage) AS km_restant
        FROM voitures v
        WHERE (v.prochain_changement_pneus_km - v.kilometrage) <= 2000
        ORDER BY km_restant ASC""";

    try (Connection conn = DBConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            int kmLeft = rs.getInt("km_restant");
            String plaque = rs.getString("plaque_immatriculation");
            String marque = rs.getString("marque");
            int lastChange = rs.getInt("dernier_changement_pneus_km");
            int nextChange = rs.getInt("prochain_changement_pneus_km");
            int currentKm = rs.getInt("kilometrage");
            
            String status, details;
            if (kmLeft <= 0) {
                status = "URGENT";
                details = String.format("""
                    Pneus DÉPASSÉS!
                    - Dernier changement: %d km
                    - Actuel: %d km (+%d km depuis changement)
                    - Devrait être changé avant: %d km""",
                    lastChange, currentKm, (currentKm - lastChange), nextChange);
            } else if (kmLeft <= 500) {
                status = "URGENT";
                details = String.format("""
                    CHANGEZ IMMÉDIATEMENT!
                    - Kilométrage actuel: %d km
                    - Changement requis avant: %d km
                    - %d km restants""",
                    currentKm, nextChange, kmLeft);
            } else if (kmLeft <= 1000) {
                status = "ATTENTION";
                details = String.format("""
                    Changement approchant
                    - Actuel: %d km
                    - Prochain changement: %d km
                    - %d km restants""",
                    currentKm, nextChange, kmLeft);
            } else {
                status = "SAFE";
                details = String.format("""
                    Contrôle recommandé
                    - Kilométrage actuel: %d km
                    - Changement prévu à: %d km
                    - %d km restants""",
                    currentKm, nextChange, kmLeft);
            }

            alertes.add(new Alerte(
                "Pneus",
                plaque + " (" + marque + ")",
                (kmLeft <= 0 ? "+" + (-kmLeft) : kmLeft) + " km",
                status,
                details,
                null
            ));
        }
    }
    return alertes;
}

    public static List<Alerte> getAlertesAssurances() throws SQLException {
        List<Alerte> alertes = new ArrayList<>();
        String sql = """
            SELECT v.id, v.plaque_immatriculation, v.marque,
                   a.id as assurance_id, a.date_expiration,
                   DATEDIFF(a.date_expiration, CURDATE()) AS jours_restants
            FROM voitures v
            JOIN assurances a ON v.id = a.voiture_id
            WHERE a.date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY)
            ORDER BY a.date_expiration ASC""";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, ASSURANCE_ALERT_DAYS);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int daysLeft = rs.getInt("jours_restants");
                String plaque = rs.getString("plaque_immatriculation");
                String marque = rs.getString("marque");
                int voitureId = rs.getInt("id");
                int assuranceId = rs.getInt("assurance_id");
                Date dateExpiration = rs.getDate("date_expiration");
                
                String urgency = (daysLeft <= 7) ? "URGENT" : "ATTENTION";
                String status = (daysLeft <= 0) ? "EXPIRÉE" : daysLeft + " jours";
                
                Voiture voiture = new Voiture();
                voiture.setId(voitureId);
                voiture.setPlaqueImmatriculation(plaque);
                voiture.setMarque(marque);
                
                Alerte alerte = new Alerte(
                    "Assurance",
                    plaque + " (" + marque + ")",
                    status,
                    urgency,
                    "Renouvellement nécessaire",
                    voiture
                );
                alerte.setAssuranceId(assuranceId);
                alerte.setDateExpiration(dateExpiration);
                alertes.add(alerte);
            }
        }
        return alertes;
    }

    public boolean marquerVidangeCommeFaite(int voitureId) throws SQLException {
        String sql = "UPDATE voitures SET prochain_vidange_km = kilometrage + ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, VIDANGE_INTERVAL);
            stmt.setInt(2, voitureId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean marquerPneusCommeFaits(int voitureId) throws SQLException {
        String sql = "UPDATE voitures SET dernier_changement_pneus_km = kilometrage WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, voitureId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean renouvelerAssurance(int assuranceId, Date nouvelleDate) throws SQLException {
        String sql = "UPDATE assurances SET date_expiration = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(nouvelleDate.getTime()));
            stmt.setInt(2, assuranceId);
            return stmt.executeUpdate() > 0;
        }
    }
   // In AlerteDAO.java
public static List<Alerte> getAlertesControleTechnique() throws SQLException {
    List<Alerte> alertes = new ArrayList<>();
    String sql = """
        SELECT v.id, v.plaque_immatriculation, v.marque, 
               v.dernier_controle_technique, v.prochain_controle_technique,
               DATEDIFF(v.prochain_controle_technique, CURDATE()) AS jours_restants
        FROM voitures v
        WHERE v.prochain_controle_technique IS NOT NULL
        ORDER BY jours_restants ASC""";

    try (Connection conn = DBConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            int joursRestants = rs.getInt("jours_restants");
            String plaque = rs.getString("plaque_immatriculation");
            String marque = rs.getString("marque");
            int voitureId = rs.getInt("id");
            Date prochainControle = rs.getDate("prochain_controle_technique");
            Date dernierControle = rs.getDate("dernier_controle_technique");
            
            // Determine status based on days remaining
            String urgency, details;
            if (joursRestants <= 0) {
                urgency = "URGENT";
                details = "Contrôle technique expiré!";
            } else if (joursRestants <= 10) {
                urgency = "URGENT";
                details = "Contrôle dans " + joursRestants + " jours!";
            } else if (joursRestants <= 20) {
                urgency = "ATTENTION";
                details = "Contrôle dans " + joursRestants + " jours";
            } else {
                urgency = "SAFE";
                details = "Contrôle dans " + joursRestants + " jours";
            }
            
            Voiture voiture = new Voiture();
            voiture.setId(voitureId);
            voiture.setPlaqueImmatriculation(plaque);
            voiture.setMarque(marque);
            voiture.setDernierControleTechnique(dernierControle);
            voiture.setProchainControleTechnique(prochainControle);
            
            alertes.add(new Alerte(
                "Contrôle Technique",
                plaque + " (" + marque + ")",
                joursRestants + " jours",
                urgency,
                details,
                voiture
            ));
        }
    }
    return alertes;
}

}