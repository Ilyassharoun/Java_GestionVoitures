/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

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
