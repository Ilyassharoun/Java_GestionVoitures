/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package views;

import dao.AlerteDAO;
import dao.VoitureDAO;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import models.Alerte;
import models.Voiture;

/**
 *
 * @author samsung
 */
public class AlertsFrame extends javax.swing.JFrame {

    /**
     * Creates new form AlertsFrame
     */
    public AlertsFrame() {
        initComponents();
        setTitle("Alertes d'Entretien");
        setLocationRelativeTo(null);
        setupTable();
        setupContextMenu();
        loadAlertes("Tous");
        startAutoRefresh();
        setResizable(false); 
    }
    private void setupTable() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Type", "Véhicule", "Jours/Km Restants", "Statut", "Détails"}
        ) {
            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblAlerts.setModel(model);
        tblAlerts.setRowHeight(25);
        
        // Custom cell renderer for coloring
        tblAlerts.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getValueAt(row, 3);
                
                setToolTipText(table.getValueAt(row, 4).toString());
                
                if (isSelected) {
                    c.setBackground(new Color(200, 200, 255));
                } else {
                    switch (status) {
                        case "URGENT":
                            c.setBackground(new Color(255, 200, 200));
                            break;
                        case "ATTENTION":
                            c.setBackground(new Color(255, 204, 102));
                            break;
                        case "SAFE":
                            c.setBackground(new Color(200, 255, 200));
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        
        // Set column widths
        tblAlerts.getColumnModel().getColumn(0).setPreferredWidth(120);  // Type
        tblAlerts.getColumnModel().getColumn(1).setPreferredWidth(150); // Véhicule
        tblAlerts.getColumnModel().getColumn(2).setPreferredWidth(120); // Jours/Km Restants
        tblAlerts.getColumnModel().getColumn(3).setPreferredWidth(80);  // Statut
        tblAlerts.getColumnModel().getColumn(4).setPreferredWidth(250); // Détails
    }

    private void loadAlertes(String filterType) {
        DefaultTableModel model = (DefaultTableModel) tblAlerts.getModel();
        model.setRowCount(0);

        try {
            List<Alerte> alertes = switch (filterType) {
                case "Vidange" -> AlerteDAO.getAlertesVidange();
                case "Pneus" -> AlerteDAO.getAlertesPneus();
                case "Contrôle Technique" -> AlerteDAO.getAlertesControleTechnique();
                default -> {
                    List<Alerte> all = new ArrayList<>();
                    all.addAll(AlerteDAO.getAlertesVidange());
                    all.addAll(AlerteDAO.getAlertesPneus());
                    all.addAll(AlerteDAO.getAlertesControleTechnique());
                    yield all;
                }
            };

            if (alertes.isEmpty()) {
                String noAlertMessage = "Aucune alerte " + 
                    (filterType.equals("Tous") ? "" : "de type " + filterType.toLowerCase());
                model.addRow(new Object[]{"", noAlertMessage, "", "", ""});
            } else {
                alertes.sort((a1, a2) -> {
                    int priority1 = getPriorityLevel(a1.getStatut());
                    int priority2 = getPriorityLevel(a2.getStatut());
                    return Integer.compare(priority1, priority2);
                });

                for (Alerte a : alertes) {
                    model.addRow(new Object[]{
                        a.getType(),
                        a.getVehiculeInfo(),
                        a.getKmJoursRestants(),
                        a.getStatut(),
                        a.getDetails()
                    });
                }
            }
        } catch (SQLException e) {
            model.addRow(new Object[]{
                "Erreur", 
                "", 
                "", 
                "Échec du chargement", 
                e.getMessage()
            });
            e.printStackTrace();
        }
    }

    private int getPriorityLevel(String statut) {
        return switch (statut) {
            case "URGENT" -> 1;
            case "ATTENTION" -> 2;
            case "SAFE" -> 3;
            default -> 4;
        };
    }

    private void setupContextMenu() {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem markDone = new JMenuItem("Marquer comme fait");
        
        markDone.addActionListener(e -> markSelectedAlertAsDone());
        popup.add(markDone);
        tblAlerts.setComponentPopupMenu(popup);
    }

    private void startAutoRefresh() {
        Timer timer = new Timer(30 * 60 * 1000, e -> loadAlertes("Tous"));
        timer.setRepeats(true);
        timer.start();
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
        BTN_Retour = new javax.swing.JButton();
        BTN_MCF = new javax.swing.JButton();
        BTN_Vidange = new javax.swing.JButton();
        BTN_Pneus = new javax.swing.JButton();
        BTN_Assurance = new javax.swing.JButton();
        BTN_Actualiser = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAlerts = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(0, 153, 255));

        jLabel1.setBackground(new java.awt.Color(51, 153, 255));
        jLabel1.setFont(new java.awt.Font("Arial Black", 0, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("GESTION DES ALERTS");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(157, 157, 157))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabel1)
                .addContainerGap(56, Short.MAX_VALUE))
        );

        BTN_Retour.setBackground(new java.awt.Color(204, 204, 204));
        BTN_Retour.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        BTN_Retour.setText("Retour");
        BTN_Retour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_RetourActionPerformed(evt);
            }
        });

        BTN_MCF.setBackground(new java.awt.Color(204, 204, 204));
        BTN_MCF.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        BTN_MCF.setText("Marque comme fait");
        BTN_MCF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_MCFActionPerformed(evt);
            }
        });

        BTN_Vidange.setBackground(new java.awt.Color(204, 204, 204));
        BTN_Vidange.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        BTN_Vidange.setText("Vidange");
        BTN_Vidange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_VidangeActionPerformed(evt);
            }
        });

        BTN_Pneus.setBackground(new java.awt.Color(204, 204, 204));
        BTN_Pneus.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        BTN_Pneus.setText("Pneus");
        BTN_Pneus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_PneusActionPerformed(evt);
            }
        });

        BTN_Assurance.setBackground(new java.awt.Color(204, 204, 204));
        BTN_Assurance.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        BTN_Assurance.setText("Contrôle Technique");
        BTN_Assurance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_AssuranceActionPerformed(evt);
            }
        });

        BTN_Actualiser.setBackground(new java.awt.Color(204, 204, 204));
        BTN_Actualiser.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        BTN_Actualiser.setText("Actualiser");
        BTN_Actualiser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_ActualiserActionPerformed(evt);
            }
        });

        tblAlerts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Type", "Voiture", "Echéance"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblAlerts);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(BTN_MCF)
                            .addGap(18, 18, 18)
                            .addComponent(BTN_Actualiser, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                            .addComponent(BTN_Retour, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(BTN_Pneus, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(BTN_Vidange, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGap(18, 18, 18)
                            .addComponent(BTN_Assurance)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 657, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_Vidange, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BTN_Pneus, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BTN_Assurance, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_Retour, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BTN_MCF, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BTN_Actualiser, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38))
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

    private void BTN_RetourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_RetourActionPerformed
        // TODO add your handling code here:
        this.dispose(); // Close current window
        new MainMenuFrame().setVisible(true); // Reopen main menu
    }//GEN-LAST:event_BTN_RetourActionPerformed

    private void BTN_MCFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_MCFActionPerformed
        // TODO add your handling code here:
        markSelectedAlertAsDone();
    }//GEN-LAST:event_BTN_MCFActionPerformed

    private void BTN_VidangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_VidangeActionPerformed
        // TODO add your handling code here:
        loadAlertes("Vidange");
    }//GEN-LAST:event_BTN_VidangeActionPerformed

    private void BTN_PneusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_PneusActionPerformed
        // TODO add your handling code here:
        loadAlertes("Pneus");
    }//GEN-LAST:event_BTN_PneusActionPerformed

    private void BTN_AssuranceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_AssuranceActionPerformed
        // TODO add your handling code here:
        loadAlertes("Contrôle Technique");
    }//GEN-LAST:event_BTN_AssuranceActionPerformed

    private void BTN_ActualiserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_ActualiserActionPerformed
        // TODO add your handling code here:
        loadAlertes("Tous");
    }//GEN-LAST:event_BTN_ActualiserActionPerformed
    private void markSelectedAlertAsDone() {
        int row = tblAlerts.getSelectedRow();
        if (row < 0) {
            showWarning("Sélectionnez une alerte à traiter", "Aucune sélection");
            return;
        }

        try {
            String type = (String) tblAlerts.getValueAt(row, 0);
            String vehicleInfo = (String) tblAlerts.getValueAt(row, 1);
            String plaque = extractPlaque(vehicleInfo);

            if (!VoitureDAO.vehicleExists(plaque)) {
                showError("Véhicule non trouvé", "La plaque " + plaque + " n'existe pas dans la base");
                return;
            }

            switch (type) {
                case "Vidange":
                    handleVidange(plaque);
                    break;
                case "Pneus":
                    handlePneus(plaque);
                    break;
                case "Contrôle Technique":
                    handleControleTechnique(plaque);
                    break;
                default:
                    showError("Type d'alerte inconnu", "Le type '" + type + "' n'est pas reconnu");
            }
            
            loadAlertes("Tous");
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void handleVidange(String plaque) throws SQLException {
        int currentKm = VoitureDAO.getKilometrage(plaque);
        VoitureDAO.mettreAJourVidange(plaque);
        int nextVidangeKm = VoitureDAO.getProchainVidangeKm(plaque);
        showSuccess(
            "Vidange enregistrée",
            "Prochaine vidange dans " + (nextVidangeKm - currentKm) + " km"
        );
    }

    private void handlePneus(String plaque) {
    try {
        // 1. Get current tire info
        int[] pneusInfo = VoitureDAO.getPneusInfo(plaque);
        int currentKm = pneusInfo[0];
        int lastChangeKm = pneusInfo[1];
        int nextChangeKm = pneusInfo[2];
        
        // 2. Update tire maintenance record
        VoitureDAO.updatePneus(plaque);
        
        // 3. Get new values after update
        int[] updatedInfo = VoitureDAO.getPneusInfo(plaque);
        int newNextChangeKm = updatedInfo[2];
        
        // 4. Verify the update was successful
        if (newNextChangeKm != currentKm + 20000) {
            throw new SQLException("Tire update failed - next change not set correctly");
        }
        
        // 5. Calculate remaining kilometers
        int kmLeft = newNextChangeKm - currentKm;
        
        // 6. Show success message with new status
        String message = String.format(
            "<html><b>Pneus marqués comme changés</b><br><br>" +
            "• Dernier changement: %d km<br>" +
            "• Kilométrage actuel: %d km<br>" +
            "• Prochain changement: %d km<br>" +
            "• Nouveau statut: <font color='green'>SAFE</font> (%d km restants)</html>",
            currentKm,
            currentKm,
            newNextChangeKm,
            kmLeft
        );
        
        JOptionPane.showMessageDialog(
            this,
            message,
            "Mise à jour réussie",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // 7. Refresh alerts - will now show as SAFE
        loadAlertes("Tous"); // Refresh with current filter
        
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(
            this,
            "<html>Erreur lors de la mise à jour:<br>" + ex.getMessage() + "</html>",
            "Échec de la mise à jour",
            JOptionPane.ERROR_MESSAGE
        );
    }
}

    private void handleControleTechnique(String plaque) throws SQLException {
        VoitureDAO.mettreAJourControleTechnique(plaque);
        
        // Get the new control date to display
        Date nextControl = VoitureDAO.getProchainControleTechnique(plaque);
        String nextDate = new SimpleDateFormat("dd/MM/yyyy").format(nextControl);
        
        showSuccess(
            "Contrôle technique enregistré",
            "Prochain contrôle le: " + nextDate
        );
    }

    private String extractPlaque(String vehicleInfo) throws IllegalArgumentException {
        if (vehicleInfo == null || vehicleInfo.trim().isEmpty()) {
            throw new IllegalArgumentException("Les informations du véhicule sont vides");
        }

        String normalized = vehicleInfo.replaceAll("[^a-zA-Z0-9]", " ").trim().toUpperCase();
        Pattern platePattern = Pattern.compile("(\\d{2,4}[A-Z]\\d{4,6})|([A-Z]{2,3}\\d{5,6})");
        
        Matcher matcher = platePattern.matcher(normalized);
        if (matcher.find()) {
            return matcher.group().replaceAll("\\s", "");
        }
        
        String[] parts = normalized.split("\\s+");
        for (String part : parts) {
            if (part.length() >= 6) {
                return part;
            }
        }
        
        throw new IllegalArgumentException("Plaque non reconnue: " + vehicleInfo);
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccess(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
     /* @param args the command line arguments
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
            java.util.logging.Logger.getLogger(AlertsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AlertsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AlertsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AlertsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AlertsFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BTN_Actualiser;
    private javax.swing.JButton BTN_Assurance;
    private javax.swing.JButton BTN_MCF;
    private javax.swing.JButton BTN_Pneus;
    private javax.swing.JButton BTN_Retour;
    private javax.swing.JButton BTN_Vidange;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblAlerts;
    // End of variables declaration//GEN-END:variables
}
