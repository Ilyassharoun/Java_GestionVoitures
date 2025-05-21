/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.util.Date;

/**
 *
 * @author samsung
 */
public class Client {
    private int id;
    private String nom;
    private String prenom;
    private String cin;
    private String permis;
    private String adresse;
    private String Telephone;
    private Date cinExpiration;
    private Date permisExpiration;
    private byte[] cinImageData;
    private byte[] permisImageData;
    private String cinFilename;  
    private String permisFilename;  

    // Constructors
    public Client() {}

    public Client(String nom, String prenom, String cin) {
        this.nom = nom;
        this.prenom = prenom;
        this.cin = cin;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getPermis() { return permis; }
    public void setPermis(String permis) { this.permis = permis; }
    public String getTelephone() { return Telephone; }
    public void setTelephone(String adresse) { this.Telephone = adresse; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public Date getCinExpiration() {
        return cinExpiration;
    }
    
    public void setCinExpiration(Date cinExpiration) {
        this.cinExpiration = cinExpiration;
    }
    
    public Date getPermisExpiration() {
        return permisExpiration;
    }
    
    public void setPermisExpiration(Date permisExpiration) {
        this.permisExpiration = permisExpiration;
    }
    public byte[] getCinImageData() { return cinImageData; }
    public void setCinImageData(byte[] cinImageData) { this.cinImageData = cinImageData; }
    public byte[] getPermisImageData() { return permisImageData; }
    public void setPermisImageData(byte[] permisImageData) { this.permisImageData = permisImageData; }
    public String getCinFilename() { return cinFilename; }
    public void setCinFilename(String cinFilename) { this.cinFilename = cinFilename; }
    public String getPermisFilename() { return permisFilename; }
    public void setPermisFilename(String permisFilename) { this.permisFilename = permisFilename; }
     public boolean hasCinImage() {
        return cinImageData != null && cinImageData.length > 0;
    }

    public boolean hasPermisImage() {
        return permisImageData != null && permisImageData.length > 0;
    }
    @Override
    public String toString() {
        return nom + " " + prenom;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Client client = (Client) obj;
        return id == client.id;
    }
}
