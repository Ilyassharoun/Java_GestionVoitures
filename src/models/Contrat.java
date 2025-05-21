/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author samsung
 */
public class Contrat {
     private int id;
    private int clientId;
    private int voitureId;
    private double prixJournalier;
    private int duree;  // Added duration field
    private double total;  // Added total field
    private double avance;
    private String modePaiement;
    private double resteAPayer;
    private Date dateDebut;  // java.sql.Date
private Date dateFin;
private int reservationId;
private Client secondDriver;
    
    // Vehicle state
    private boolean carrosserieOk;
    private boolean pneusOk;
    private boolean siegesOk;
    private String autresRemarques;
    
    // Options
    private boolean roueSecours;
    private boolean cric;
    private boolean siegeBebe;
    private boolean cleRoue;

    // Calculation methods
    public double calculateTotal() {
        this.total = this.prixJournalier * this.duree;
        return this.total;
    }
    
    public double calculateResteAPayer() {
        this.resteAPayer = this.calculateTotal() - this.avance;
        return this.resteAPayer;
    }
    
    public String getEtatVehiculeAsString() {
        List<String> etats = new ArrayList<>();
        if (carrosserieOk) etats.add("Carrosserie OK");
        if (pneusOk) etats.add("Pneus OK");
        if (siegesOk) etats.add("Sièges OK");
        if (autresRemarques != null && !autresRemarques.isEmpty()) {
            etats.add("Autres: " + autresRemarques);
        }
        return etats.isEmpty() ? "Aucun état spécifié" : String.join(", ", etats);
    }
    
    public String getOptionsAsString() {
        List<String> options = new ArrayList<>();
        if (roueSecours) options.add("Roue de secours");
        if (cric) options.add("Cric");
        if (siegeBebe) options.add("Siège bébé");
        if (cleRoue) options.add("Clé de roue");
        return options.isEmpty() ? "Aucune option" : String.join(", ", options);
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public int getVoitureId() {
        return voitureId;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public double getPrixJournalier() {
        return prixJournalier;
    }

    public int getDuree() {
        return duree;
    }

    public double getTotal() {
        return total;
    }

    public double getAvance() {
        return avance;
    }

    public double getResteAPayer() {
        return resteAPayer;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public boolean isCarrosserieOk() {
        return carrosserieOk;
    }

    public boolean isPneusOk() {
        return pneusOk;
    }

    public boolean isSiegesOk() {
        return siegesOk;
    }

    public String getAutresRemarques() {
        return autresRemarques;
    }

    public boolean isRoueSecours() {
        return roueSecours;
    }

    public boolean isCric() {
        return cric;
    }

    public boolean isSiegeBebe() {
        return siegeBebe;
    }

    public boolean isCleRoue() {
        return cleRoue;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setVoitureId(int voitureId) {
        this.voitureId = voitureId;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public void setPrixJournalier(double prixJournalier) {
        this.prixJournalier = prixJournalier;
        this.calculateTotal(); // Auto-update total when price changes
        this.calculateResteAPayer();
    }

    public void setDuree(int duree) {
        this.duree = duree;
        this.calculateTotal(); // Auto-update total when duration changes
        this.calculateResteAPayer();
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setAvance(double avance) {
        this.avance = avance;
        this.calculateResteAPayer(); // Auto-update remaining payment when advance changes
    }

    public void setResteAPayer(double resteAPayer) {
        this.resteAPayer = resteAPayer;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public void setCarrosserieOk(boolean carrosserieOk) {
        this.carrosserieOk = carrosserieOk;
    }

    public void setPneusOk(boolean pneusOk) {
        this.pneusOk = pneusOk;
    }

    public void setSiegesOk(boolean siegesOk) {
        this.siegesOk = siegesOk;
    }

    public void setAutresRemarques(String autresRemarques) {
        this.autresRemarques = autresRemarques;
    }

    public void setRoueSecours(boolean roueSecours) {
        this.roueSecours = roueSecours;
    }

    public void setCric(boolean cric) {
        this.cric = cric;
    }

    public void setSiegeBebe(boolean siegeBebe) {
        this.siegeBebe = siegeBebe;
    }

    public void setCleRoue(boolean cleRoue) {
        this.cleRoue = cleRoue;
    }

    @Override
    public String toString() {
        return "Contrat{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", voitureId=" + voitureId +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", prixJournalier=" + prixJournalier +
                ", duree=" + duree +
                ", total=" + total +
                ", avance=" + avance +
                ", modePaiement='" + modePaiement + '\'' +
                ", resteAPayer=" + resteAPayer +
                ", carrosserieOk=" + carrosserieOk +
                ", pneusOk=" + pneusOk +
                ", siegesOk=" + siegesOk +
                ", autresRemarques='" + autresRemarques + '\'' +
                ", roueSecours=" + roueSecours +
                ", cric=" + cric +
                ", siegeBebe=" + siegeBebe +
                ", cleRoue=" + cleRoue +
                '}';
    }
    public void setDatesFromReservation(SortieVoiture reservation) {
    if (reservation != null) {
        this.dateDebut = new Date(reservation.getDateSortie().getTime());
        this.dateFin = new Date(reservation.getDateRetour().getTime());
    }
}
    public int getReservationId() {
    return reservationId;
}

public void setReservationId(int reservationId) {
    this.reservationId = reservationId;
}
public Client getSecondDriver() {
        return secondDriver;
    }
    
    public void setSecondDriver(Client secondDriver) {
        this.secondDriver = secondDriver;
    }
}
