/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author samsung
 */
import models.Voiture;
import java.sql.SQLException;
public class TestDAO {
    public static void main(String[] args) {
        try {
            // 1. Test connection
            System.out.println("Testing database connection...");
            DBConnection.getConnection().close();
            System.out.println("✅ Connection successful!");

            // 2. Test CRUD operations
            System.out.println("\nTesting CRUD operations...");
            
            // Create
            Voiture testCar = new Voiture("TEST-123", "TestBrand", "TestModel", 2009,"rouge", 5000,4000);
            VoitureDAO.addVoiture(testCar);
            System.out.println("✅ Car added successfully!");

            // Read
            System.out.println("\nAll vehicles in database:");
            VoitureDAO.listerVoitures().forEach(v -> 
                System.out.println(v.getPlaqueImmatriculation() + " - " + v.getMarque())
            );

        } catch (SQLException e) {
            System.err.println("❌ Database error:");
            e.printStackTrace();
        }
    }
}
