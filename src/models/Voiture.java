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
public class Voiture {
    private int id;
    private String plaqueImmatriculation;
    private String marque;
    private String modele;
    private int annee;
    private String couleur;
    private int kilometrage;
    private int dernier_vidange_km;
    private Date dernierControleTechnique;
    private Date prochainControleTechnique;
    
    
    
    // Constructors
    public Voiture() {}
    
    public Voiture(
            String plaque, String marque, String modele,int annee,String couleur, int km,int dernier_vidange_km) {
        this.plaqueImmatriculation = plaque;
        this.marque = marque;
        this.modele = modele;
        this.annee = annee;
        this.couleur = couleur;
        this.kilometrage = km;
        this.dernier_vidange_km=dernier_vidange_km;
        /*this.dernierControleTechnique=dernierControleTechnique;
        this.prochainControleTechnique=prochainControleTechnique;*/
    }
    
    // Getters & Setters (generate these in NetBeans with Right-click → Insert Code)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    // ... (add others for all fields)
    public String getPlaqueImmatriculation() { return plaqueImmatriculation; }
    public void setPlaqueImmatriculation(String plaqueImmatriculation) { this.plaqueImmatriculation = plaqueImmatriculation; }
    //
    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }
    //
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    //
    public int getKilometrage() { return kilometrage; }
    public void setKilometrage(int kilometrage) { this.kilometrage = kilometrage; }
    //
    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }
    //
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }
    //
    public int getDernierVidange() { return dernier_vidange_km; }
    public void setDernierVidange(int dernier_vidange_km) { this.dernier_vidange_km = dernier_vidange_km; }
    public Date getDernierControleTechnique() { return dernierControleTechnique; }
    public void setDernierControleTechnique(Date date) { this.dernierControleTechnique = date; }
    
    public Date getProchainControleTechnique() { return prochainControleTechnique; }
    public void setProchainControleTechnique(Date date) { this.prochainControleTechnique = date;}
}
