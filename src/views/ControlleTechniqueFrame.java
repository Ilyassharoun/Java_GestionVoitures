/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package views;

/**
 *
 * @author samsung
 */
import java.sql.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import com.toedter.calendar.JDateChooser;
import dao.AlerteDAO;
import dao.VoitureDAO;
import models.Voiture;
import java.util.Date;
public class ControlleTechniqueFrame extends javax.swing.JFrame {

    /**
     * Creates new form ControlleTechniqueFrame
     */
    private VoitureDAO VoitureDAO;
    private List<Voiture> vehicles;
    public ControlleTechniqueFrame() {
        initComponents();
        setTitle("Enregistrement du Contrôle Technique");  
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon(getClass().getResource("/resources/jmlogo.png")).getImage());
        initCustomComponents();
        loadVehiclesToComboBox();
        setResizable(false); 
        dateControleField.setDate(null);
        lblNextControll.setText("");
    }
      private void initCustomComponents() {
        VoitureDAO = new VoitureDAO();
        dateControleField.setDateFormatString("dd-MM-yyyy");
        
        // Add listeners
        vehicleCombo.addActionListener(e -> loadVehicleData());
        dateControleField.addPropertyChangeListener("date", e -> updateNextControlDate());
        BTN_EP.addActionListener(e -> saveControl());
        BTN_Annuler.addActionListener(e -> clearForm());
    }

    private void loadVehiclesToComboBox() {
    try {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        vehicleCombo.setModel(model);
        
        vehicles = VoitureDAO.getAllVoitures();
        
        if (vehicles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Aucun véhicule trouvé dans la base de données",
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Add items with consistent formatting
        for (Voiture v : vehicles) {
            model.addElement(formatVehicleDisplay(v));
        }
        
        if (vehicleCombo.getItemCount() > 0) {
            vehicleCombo.setSelectedIndex(0);
        }
        
    } catch (SQLException e) {
        showError("Erreur de base de données", 
                 "Impossible de charger les véhicules: " + e.getMessage());
        e.printStackTrace();
    }
}

private String formatVehicleDisplay(Voiture v) {
    return v.getPlaqueImmatriculation() + " - " + v.getMarque() + " " + v.getModele();
}

private Voiture getSelectedVoiture() {
    int selectedIndex = vehicleCombo.getSelectedIndex();
    if (selectedIndex >= 0 && selectedIndex < vehicles.size()) {
        return vehicles.get(selectedIndex);
    }
    return null;
}

    private void loadVehicleData() {
    int selectedIndex = vehicleCombo.getSelectedIndex();
    if (selectedIndex < 0 || selectedIndex >= vehicles.size()) {
        return;
    }
     Voiture selected = getSelectedVoiture();
    if (selected != null) {
        dateControleField.setDate(selected.getDernierControleTechnique());
        updateNextControlDate();
    }

    try {
        Voiture voiture = VoitureDAO.getVoitureByIdControll(selected.getId());
        
        if (voiture == null) {
            showError("Erreur", "Véhicule non trouvé dans la base de données");
            return;
        }

        // Only set date if it exists in database, otherwise keep empty
        dateControleField.setDate(voiture.getDernierControleTechnique());
        
        // Update next control date only if current date exists
        if (voiture.getDernierControleTechnique() != null) {
            updateNextControlDate();
        } else {
            lblNextControll.setText(""); // Clear next control date
        }
        
    }catch (SQLException e) {
        showError("Erreur de base de données",
                 "Impossible de charger les détails du véhicule: " + e.getMessage());
        e.printStackTrace();
    }
}

private void updateNextControlDate() {
    Date current = dateControleField.getDate();
    if (current != null) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        cal.add(Calendar.MONTH, 6);
        lblNextControll.setText(new SimpleDateFormat("dd-MM-yyyy").format(cal.getTime()));
    } else {
        lblNextControll.setText(""); // Clear if no date selected
    }
}

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
    private void showSuccess(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblNextControll = new javax.swing.JLabel();
        BTN_EP = new javax.swing.JButton();
        BTN_Annuler = new javax.swing.JButton();
        vehicleCombo = new javax.swing.JComboBox<>();
        BTN_Retour = new javax.swing.JButton();
        dateControleField = new com.toedter.calendar.JDateChooser();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(0, 153, 255));

        jLabel1.setBackground(new java.awt.Color(51, 153, 255));
        jLabel1.setFont(new java.awt.Font("Arial Black", 0, 25)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("ENREGISTREMENT DU CONTRÔLE TECHNIQUE ");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(57, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(54, 54, 54))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel1)
                .addContainerGap(57, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel2.setText("Sélectionnez un véhicule");

        jLabel5.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel5.setText("Dernier Controle Technique");

        lblNextControll.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblNextControll.setText("PC");

        BTN_EP.setBackground(new java.awt.Color(204, 204, 204));
        BTN_EP.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        BTN_EP.setText("Enregistrer");
        BTN_EP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_EPActionPerformed(evt);
            }
        });

        BTN_Annuler.setBackground(new java.awt.Color(204, 204, 204));
        BTN_Annuler.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        BTN_Annuler.setText("Annuler");
        BTN_Annuler.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_AnnulerActionPerformed(evt);
            }
        });

        vehicleCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vehicleComboActionPerformed(evt);
            }
        });

        BTN_Retour.setBackground(new java.awt.Color(204, 204, 204));
        BTN_Retour.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        BTN_Retour.setText("Retour");
        BTN_Retour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_RetourActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel6.setText("Prochain Conrolle:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblNextControll)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(vehicleCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dateControleField, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
                .addGap(181, 181, 181))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(169, 169, 169)
                .addComponent(BTN_EP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(BTN_Annuler, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(BTN_Retour, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vehicleCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(50, 50, 50)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateControleField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(54, 54, 54)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNextControll)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_EP, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BTN_Annuler, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BTN_Retour, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(72, 72, 72))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BTN_EPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_EPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BTN_EPActionPerformed

    private void BTN_AnnulerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_AnnulerActionPerformed
        // TODO add your handling code here:
        // Reopen main menu
    }//GEN-LAST:event_BTN_AnnulerActionPerformed

    private void vehicleComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vehicleComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_vehicleComboActionPerformed

    private void BTN_RetourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_RetourActionPerformed
        // TODO add your handling code here:
        this.dispose(); // Close current window
        new MainMenuFrame().setVisible(true);

    }//GEN-LAST:event_BTN_RetourActionPerformed
    private void saveControl() {
    Voiture selectedVehicle = getSelectedVoiture();
    if (selectedVehicle == null) {
        showError("Erreur", "Aucun véhicule sélectionné");
        return;
    }

    if (dateControleField.getDate() == null) {
        showError("Erreur", "Veuillez sélectionner une date valide");
        return;
    }

    try {
        // Update vehicle dates
        selectedVehicle.setDernierControleTechnique(dateControleField.getDate());
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateControleField.getDate());
        cal.add(Calendar.MONTH, 6);
        selectedVehicle.setProchainControleTechnique(cal.getTime());
        
        // Save to database
        VoitureDAO.updateControleTechnique(selectedVehicle);
        
        showSuccess("Succès", "Contrôle technique enregistré pour: " + 
                   formatVehicleDisplay(selectedVehicle));
        
        // Optional: refresh the list
        loadVehiclesToComboBox();
        
    } catch (SQLException e) {
        showError("Erreur DB", "Échec de sauvegarde: " + e.getMessage());
    }
}

    private void clearForm() {
        dateControleField.setDate(new Date());
        if (vehicleCombo.getItemCount() > 0) {
            vehicleCombo.setSelectedIndex(0);
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ControlleTechniqueFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ControlleTechniqueFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ControlleTechniqueFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ControlleTechniqueFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ControlleTechniqueFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BTN_Annuler;
    private javax.swing.JButton BTN_EP;
    private javax.swing.JButton BTN_Retour;
    private com.toedter.calendar.JDateChooser dateControleField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblNextControll;
    private javax.swing.JComboBox<String> vehicleCombo;
    // End of variables declaration//GEN-END:variables
}
