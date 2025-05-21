/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author samsung
 */
import java.io.ByteArrayInputStream;
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
    String sql = "SELECT * FROM clients WHERE nom = ? AND prenom = ?";
    
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
                client.setCinExpiration(rs.getDate("cin_expiration"));
                client.setPermisExpiration(rs.getDate("permis_expiration"));
                
                // Get image data and filenames
                client.setCinImageData(rs.getBytes("cin_image"));
                client.setPermisImageData(rs.getBytes("permis_image"));
                client.setCinFilename(rs.getString("cin_filename"));
                client.setPermisFilename(rs.getString("permis_filename"));
                
                return client;
            }
        }
    }
    return null;
}
// Updated addClient method with all fields
 public static int addClient(Client client) throws SQLException {
        String sql = "INSERT INTO clients (nom, prenom, cin, permis, telephone, adresse, cin_expiration, permis_expiration, cin_image, permis_image, cin_filename, permis_filename) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getCin());
            stmt.setString(4, client.getPermis());
            stmt.setString(5, client.getTelephone());
            stmt.setString(6, client.getAdresse());
            stmt.setDate(7, client.getCinExpiration() != null ? new java.sql.Date(client.getCinExpiration().getTime()) : null);
            stmt.setDate(8, client.getPermisExpiration() != null ? new java.sql.Date(client.getPermisExpiration().getTime()) : null);
            
            if (client.getCinImageData() != null) {
                stmt.setBytes(9, client.getCinImageData());
            } else {
                stmt.setNull(9, Types.BLOB);
            }
            
            if (client.getPermisImageData() != null) {
                stmt.setBytes(10, client.getPermisImageData());
            } else {
                stmt.setNull(10, Types.BLOB);
            }
            
            stmt.setString(11, client.getCinFilename() != null ? client.getCinFilename() : "");
            stmt.setString(12, client.getPermisFilename() != null ? client.getPermisFilename() : "");
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;
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
                    client.setCinExpiration(rs.getDate("cin_expiration"));
                    client.setPermisExpiration(rs.getDate("permis_expiration"));
                    Blob cinBlob = rs.getBlob("cin_image");
                if (cinBlob != null) {
                    client.setCinImageData(cinBlob.getBytes(1, (int)cinBlob.length()));
                }
                
                Blob permisBlob = rs.getBlob("permis_image");
                if (permisBlob != null) {
                    client.setPermisImageData(permisBlob.getBytes(1, (int)permisBlob.length()));
                }
                    client.setCinFilename(rs.getString("cin_filename")); // NEW
                    client.setPermisFilename(rs.getString("permis_filename")); // NEW
                    return client;
                }
            }
            return null;
        }
    }
     // Update client with images
    public static boolean updateClient(Client client) throws SQLException {
        String sql = "UPDATE clients SET nom=?, prenom=?, permis=?, telephone=?, adresse=?, cin_expiration=?, permis_expiration=?, cin_image=?, permis_image=?, cin_filename=?, permis_filename=? WHERE id=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getPermis());
            stmt.setString(4, client.getTelephone());
            stmt.setString(5, client.getAdresse());
            stmt.setDate(6, client.getCinExpiration() != null ? new java.sql.Date(client.getCinExpiration().getTime()) : null);
            stmt.setDate(7, client.getPermisExpiration() != null ? new java.sql.Date(client.getPermisExpiration().getTime()) : null);
            
            if (client.getCinImageData() != null) {
                stmt.setBytes(8, client.getCinImageData());
            } else {
                stmt.setNull(8, Types.BLOB);
            }
            
            if (client.getPermisImageData() != null) {
                stmt.setBytes(9, client.getPermisImageData());
            } else {
                stmt.setNull(9, Types.BLOB);
            }
            
            stmt.setString(10, client.getCinFilename() != null ? client.getCinFilename() : "");
            stmt.setString(11, client.getPermisFilename() != null ? client.getPermisFilename() : "");
            stmt.setInt(12, client.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
}