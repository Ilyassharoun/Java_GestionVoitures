/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author samsung
 */
public class SortieVoiture {
    private int id;
    private int voitureId;
    private String plaque;
    private String client;
    private double avance;
    private Date dateSortie;
    private Date dateRetour;
    private boolean retourne;
    private static boolean prolongee; 
    private boolean isReservation;
    private Date dateDebut;
     private boolean annule;
      private String marque;
    private String immatriculation;
    private String modele; 
    private Date dateReservation;
    private String statut;
    private String reservationDates;
    private Date dateRetourEffectif;
    private boolean estAnnule;
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVoitureId() { return voitureId; }
    public void setVoitureId(int voitureId) { this.voitureId = voitureId; }
    public String getPlaque() { return plaque; }
   
    public void setPlaque(String plaque) {
        if (plaque == null || plaque.trim().isEmpty()) {
            throw new IllegalArgumentException("La plaque ne peut pas être vide");
        }
        this.plaque = plaque.trim();
    }
    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
    public double getAvance() { return avance; }
    public void setAvance(double avance) { this.avance = avance; }
    public Date getDateSortie() { return dateSortie; }
    public void setDateSortie(Date dateSortie) { this.dateSortie = dateSortie; }
    public Date getDateRetour() { return dateRetour; }
    public void setDateRetour(Date dateRetour) { this.dateRetour = dateRetour; }
    public boolean isRetourne() { return retourne; }
    public void setRetourne(boolean retourne) { this.retourne = retourne; }
    public static boolean isProlongee() {
        return prolongee;
    }
    public boolean isReservation() { return isReservation; }
    public Date getDateDebut() {
        return dateDebut != null ? dateDebut : new Date(0);
    }

    public void setProlongee(boolean prolongee) {
        this.prolongee = prolongee;
    }
    public void setReservation(boolean reservation) {
        isReservation = reservation;
    }
    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }
     public boolean isAnnule() {
        return annule;
    }
    
    public void setAnnule(boolean annule) {
        this.annule = annule;
    }
    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }
    
    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
// In your SortieVoiture class
public String getDureeEnJours() {
    try {
        // For reservations (date_sortie is null, date_debut is set)
        if (this.dateSortie == null && this.dateReservation != null && this.dateRetour != null) {
            long diff = this.dateRetour.getTime() - this.dateReservation.getTime();
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            return days + " jours";
        }
        // For active rentals (date_sortie is set)
        else if (this.dateSortie != null && this.dateRetour != null) {
            long diff = this.dateRetour.getTime() - this.dateSortie.getTime();
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) ;
            return days + " jours";
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "0 jours";
}
    
    private long calculateDaysBetween(Date start, Date end) {
        long diffInMillis = end.getTime() - start.getTime();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) + 1; // +1 to include both days
    }
    public String getMarqueModele() {
        return marque + " " + modele;
    }
    
    // Getter for status
    public String getStatut() {
    // For active rentals (has checkout date)
    if (dateSortie != null && dateRetour != null) {
        return "En sortie";
    }
    // For reservations (has reservation date but no checkout date)
    else if (dateReservation != null && dateRetour != null) {
        return "Réservé";
    }
    // For available vehicles (no dates set)
    else {
        return "Disponible";
    }
}
    // Setter for status
    public void setStatut(String statut) {
        this.statut = statut;
    }
    public Date getDateReservation() {
        return dateReservation;
    }
    public void setDateReservation(Date dateReservation) {
        this.dateReservation = dateReservation;
    }
    public boolean isReservation2() {
        // If dateSortie is null but we have reservation dates, it's a reservation
        return dateSortie == null && dateReservation != null;
    }
    public Date getDateRetourEffectif() {
        return dateRetourEffectif;
    }

    /**
     * @return true if the reservation/sortie was cancelled
     */
    public boolean isEstAnnule() {
        return estAnnule;
    }

    // Corresponding setters
    public void setDateRetourEffectif(Date dateRetourEffectif) {
        this.dateRetourEffectif = dateRetourEffectif;
    }

    public void setEstAnnule(boolean estAnnule) {
        this.estAnnule = estAnnule;
    }
}
