/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author samsung
 */
import models.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class ClientDAO {

    public ClientDAO() {
    }

    public static List<Client> getAllClients() throws SQLException {
    String sql = "SELECT * FROM clients";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        List<Client> clients = new ArrayList<>();
        while (rs.next()) {
            Client client = new Client();
            client.setId(rs.getInt("id"));
            client.setNom(rs.getString("nom"));
            client.setPrenom(rs.getString("prenom"));
            client.setCin(rs.getString("cin"));
            client.setPermis(rs.getString("permis"));
            client.setTelephone(rs.getString("telephone"));
            client.setAdresse(rs.getString("adresse"));
            // Add other fields as needed
            clients.add(client);
        }
        return clients;
    }
}

 public static Client getClientByName(String nom, String prenom) throws SQLException {
    String sql = "SELECT * FROM clients WHERE nom = ? AND prenom = ? LIMIT 1";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, nom);
        stmt.setString(2, prenom);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Client client = new Client();
                client.setId(rs.getInt("id"));
                client.setNom(rs.getString("nom"));
                client.setPrenom(rs.getString("prenom"));
                client.setCin(rs.getString("cin"));
                client.setPermis(rs.getString("permis"));
                client.setTelephone(rs.getString("telephone"));
                client.setAdresse(rs.getString("adresse"));
                return client;
            }
            return null;
        }
    }
}
// Updated addClient method with all fields
public static int addClient(Client client) throws SQLException {
    String sql = "INSERT INTO clients (nom, prenom, cin, permis, telephone, adresse) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        // Set all parameters including telephone and adresse
        stmt.setString(1, client.getNom());
        stmt.setString(2, client.getPrenom());
        stmt.setString(3, client.getCin());
        stmt.setString(4, client.getPermis());
        
        // Handle potential null values for telephone and adresse
        stmt.setString(5, client.getTelephone() != null ? client.getTelephone() : "");
        stmt.setString(6, client.getAdresse() != null ? client.getAdresse() : "");
        
        stmt.executeUpdate();
        
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        throw new SQLException("Creating client failed, no ID obtained.");
    }
}
// Get client by CIN (national ID)
    public static Client getClientByCIN(String cin) throws SQLException {
        String sql = "SELECT * FROM clients WHERE cin = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cin);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Client client = new Client();
                    client.setId(rs.getInt("id"));
                    client.setNom(rs.getString("nom"));
                    client.setPrenom(rs.getString("prenom"));
                    client.setCin(rs.getString("cin"));
                    client.setPermis(rs.getString("permis"));
                    client.setTelephone(rs.getString("telephone"));
                    client.setAdresse(rs.getString("adresse"));
                    return client;
                }
            }
        }
        return null;
    }
     public static void updateClient(Client client) throws SQLException {
        String sql = "UPDATE clients SET nom=?, prenom=?, permis=?, telephone=?, adresse=? WHERE cin=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getPermis());
            stmt.setString(4, client.getTelephone());
            stmt.setString(5, client.getAdresse());
            stmt.setString(6, client.getCin());
            
            stmt.executeUpdate();
        }
    }
}