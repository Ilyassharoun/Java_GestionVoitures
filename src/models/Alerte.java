package models;

import java.util.Date;

public class Alerte {
    private String type;
    private String vehiculeInfo;
    private String kmJoursRestants;
    private String statut;
    private String details;
    private Voiture voiture;
    private int assuranceId;
    private Date dateExpiration;
    
    // Constructors
    public Alerte() {}
    
    public Alerte(String type, String vehiculeInfo, String kmJoursRestants, 
                 String statut, String details, Voiture voiture) {
        this.type = type;
        this.vehiculeInfo = vehiculeInfo;
        this.kmJoursRestants = kmJoursRestants;
        this.statut = statut;
        this.details = details;
        this.voiture = voiture;
    }
    
    // Getters and setters for all fields
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getVehiculeInfo() { return vehiculeInfo; }
    public void setVehiculeInfo(String vehiculeInfo) { this.vehiculeInfo = vehiculeInfo; }
    
    public String getKmJoursRestants() { return kmJoursRestants; }
    public void setKmJoursRestants(String kmJoursRestants) { this.kmJoursRestants = kmJoursRestants; }
   
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public Voiture getVoiture() { return voiture; }
    public void setVoiture(Voiture voiture) { this.voiture = voiture; }
    
    public int getAssuranceId() { return assuranceId; }
    public void setAssuranceId(int assuranceId) { this.assuranceId = assuranceId; }
    
    public Date getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(Date dateExpiration) { this.dateExpiration = dateExpiration; }
    public void setVehicleInfoFromVoiture(Voiture v) {
        this.voiture = v;
        this.vehiculeInfo = v.getPlaqueImmatriculation() + " - " + v.getMarque() + " " + v.getModele();
    }
}