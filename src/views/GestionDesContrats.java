/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package views;

import dao.ClientDAO;
import dao.ContratDAO;
import dao.DBConnection;
import dao.SortieDAO;
import java.sql.SQLException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import models.Client;
import models.SortieVoiture;
import java.sql.*;

import models.Contrat;
// Core PDFBox classes
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import services.PDFGenerator;
import java.util.Date;
/**
 *
 * @author samsung
 */
public class GestionDesContrats extends javax.swing.JFrame {

    /**
     * Creates new form GestionDesContrats
     */
    private Contrat currentContrat;
private Client currentClient;
private SortieVoiture currentReservation;
 private Map<String, SortieVoiture> reservationMap = new HashMap<>();
    public GestionDesContrats() {
        initComponents();
        initializeComponents();
        setResizable(false); 
        addCalculationListeners();
        populateReservationComboBox();
        setIconImage(new ImageIcon(getClass().getResource("/resources/jmlogo.png")).getImage());
        
    }
    private void initializeComponents() {
    setTitle("Gestion Des Contrats - JMCars");
    setLocationRelativeTo(null);
    
    // Load initial data
    refreshClientComboBox();
    populateReservationComboBox();
    setupComboBoxListeners();
    
    // Set default payment method
    CMBMode.setSelectedIndex(0);
    
    // Setup calculation listener only for prixparjour
    addCalculationListeners();
    
    // Hide "autres remarques" field initially
    // In your initializeComponents() method:
    RBAutres.addActionListener(e -> toggleAutresField());
    txtAutres.setVisible(false);
    
    RBAutres.addActionListener(e -> {
    txtAutres.setVisible(RBAutres.isSelected());
    if (!RBAutres.isSelected()) {
        txtAutres.setText(""); // Clear field when unchecked
    }
});
}
    private void toggleAutresField() {
    txtAutres.setVisible(RBAutres.isSelected());
    if (RBAutres.isSelected()) {
        txtAutres.requestFocus(); // Focus the field when shown
    } else {
        txtAutres.setText(""); // Clear when hidden
    }
    this.revalidate(); // Refresh the layout
}
    // Improved calculation method
   private void calculateContractValues() {
    try {
        // Get daily price
        double prixJournalier = Double.parseDouble(txtPrixparjour.getText().trim());
        
        // Extract just the number from "X jours" text
        String dureeText = txtDuree.getText().replaceAll("[^0-9]", "");
        int duree = dureeText.isEmpty() ? 0 : Integer.parseInt(dureeText);
        
        // Get advance payment
        double avance = txtAvance.getText().trim().isEmpty() ? 0 : 
                       Double.parseDouble(txtAvance.getText().trim());
        
        // Calculate
        double total = prixJournalier * duree;
        double resteAPayer = total - avance;
        
        // Update UI
        txtTotal.setText(String.format("%.2f", total));
        txtReste.setText(String.format("%.2f", resteAPayer));
        
    } catch (NumberFormatException e) {
        showWarningMessage("Veuillez entrer un prix journalier valide");
        txtTotal.setText("");
        txtReste.setText("");
    }
}
    private void addCalculationListeners() {
    DocumentListener docListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent e) { calculate(); }
        public void removeUpdate(DocumentEvent e) { calculate(); }
        public void insertUpdate(DocumentEvent e) { calculate(); }
        
        private void calculate() {
            // Only calculate if prixparjour has a value
            if (!txtPrixparjour.getText().trim().isEmpty()) {
                calculateContractValues();
            }
        }
    };
    
    // Only add listener to prixparjour field
    txtPrixparjour.getDocument().addDocumentListener(docListener);
}
    // Call this when your frame opens or data changes


   private void populateReservationComboBox() {
    try {
        List<SortieVoiture> reservations = SortieDAO.getAllReservationsWithVehicles();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        
        for (SortieVoiture reservation : reservations) {
            String status = reservation.isReservation() ? " [Réservé]" : "";
            String displayText = reservation.getClient() + status + " - " + 
                              reservation.getMarque() + 
                              (reservation.getModele() != null ? " " + reservation.getModele() : "") + 
                              " (" + reservation.getImmatriculation() + ")";
            
            model.addElement(displayText);
        }
        
        CMBReservations.setModel(model);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading reservations: " + e.getMessage());
    }
}
private void setupComboBoxListeners() {
     CMBClients.addActionListener(e -> {
        String selected = (String) CMBClients.getSelectedItem();
        if (selected != null && !selected.trim().isEmpty()) {
            try {
                String[] names = selected.trim().split("\\s+", 2);
                if (names.length >= 2) {
                    Client client = ClientDAO.getClientByName(names[0], names[1]);
                    if (client != null) {
                        txtNom.setText(client.getNom());
                        txtPrenom.setText(client.getPrenom());
                        txtCIN.setText(client.getCin());
                        txtPermis.setText(client.getPermis());
                        
                        // Fix: Set fields separately
                        txtTele.setText(client.getTelephone() != null ? client.getTelephone() : "");
                        txtAdresse.setText(client.getAdresse() != null ? client.getAdresse() : "");
                    }else{
                        showErrorMessage("selection", "Aucune client trouvée");
                    }
                }
            } catch (SQLException ex) {
                showErrorMessage("selection", "Erreur de chargement des détails de Clients");
            }
        }
    });

    CMBReservations.addActionListener(e -> {
    String selected = (String) CMBReservations.getSelectedItem();
    if (selected == null || selected.isEmpty()) {
        clearReservationFields();
        return;
    }

    try {
        String clientIdentifier = selected.split(" - ")[0].trim();
        currentReservation = SortieDAO.getReservationWithVehicle(clientIdentifier);
        
        if (currentReservation != null) {
            SwingUtilities.invokeLater(() -> {
                // Set vehicle info
                txtMarque.setText(currentReservation.getMarqueModele() != null ? 
                                currentReservation.getMarqueModele() : "");
                txtImmatriculation.setText(currentReservation.getImmatriculation() != null ? 
                                         currentReservation.getImmatriculation() : "");
                
                // Calculate and set duration based on reservation status
                String duration = calculateDuree(currentReservation);
                txtDuree.setText(duration);
                
                // Set payment info
                txtAvance.setText(String.format("%.2f", 
                    currentReservation.getAvance() > 0 ? currentReservation.getAvance() : 0.0));
                
                // Auto-calculate contract
                if (!txtPrixparjour.getText().trim().isEmpty()) {
                    calculateContractValues();
                }
            });
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        clearReservationFields();
    }
});
}

private String calculateDuree(SortieVoiture reservation) {
    try {
        // For "En sortie" status - use date_sortie and date_retour
        if ("En sortie".equals(reservation.getStatut())) {
            if (reservation.getDateSortie() != null && reservation.getDateRetour() != null) {
                long diff = reservation.getDateRetour().getTime() - reservation.getDateSortie().getTime();
                long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1; // +1 to include both start and end days
                return days + " jours";
            }
        }
        // For "Réservé" status - use date_debut (stored in date_reservation) and date_retour
        else if ("Réservé".equals(reservation.getStatut())) {
            if (reservation.getDateReservation() != null && reservation.getDateRetour() != null) {
                // For reservations, date_reservation contains the start date (date_debut)
                Date startDate = reservation.getDateReservation(); // This should be date_debut
                Date endDate = reservation.getDateRetour();
                
                long diff = endDate.getTime() - startDate.getTime();
                long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
                return days + " jours";
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "0 jours";
}

// Clear fields helper
private void clearReservationFields() {
    txtMarque.setText("");
    txtImmatriculation.setText("");
    txtDuree.setText("0 jours");
    txtAvance.setText("0.00");
}
private boolean validateInputs() {
    // Client Information Validation
    if (txtNom.getText().trim().isEmpty()) {
        showFieldError("Nom", "Veuillez entrer le nom du client");
        txtNom.requestFocus();
        return false;
    }
    
    if (txtPrenom.getText().trim().isEmpty()) {
        showFieldError("Prénom", "Veuillez entrer le prénom du client");
        txtPrenom.requestFocus();
        return false;
    }
    
    if (txtCIN.getText().trim().isEmpty()) {
        showFieldError("CIN/Passeport", "Ce document d'identité est obligatoire");
        txtCIN.requestFocus();
        return false;
    }

    // Vehicle Information Validation
    if (txtMarque.getText().trim().isEmpty()) {
        showFieldError("Marque", "Veuillez sélectionner un véhicule");
        CMBReservations.requestFocus();
        return false;
    }

    // Contract Information Validation
    if (txtPrixparjour.getText().trim().isEmpty()) {
        showFieldError("Prix/Jour", "Veuillez entrer le prix journalier");
        txtPrixparjour.requestFocus();
        return false;
    }
    
    try {
        Double.parseDouble(txtPrixparjour.getText().trim());
    } catch (NumberFormatException e) {
        showFieldError("Prix/Jour", "Veuillez entrer un montant valide");
        txtPrixparjour.requestFocus();
        return false;
    }

    if (txtDuree.getText().trim().isEmpty()) {
        showFieldError("Durée", "Veuillez spécifier la durée de location");
        txtDuree.requestFocus();
        return false;
    }

    return true;
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
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtNom = new javax.swing.JTextField();
        txtPrenom = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        BTN_Enregitrer = new javax.swing.JButton();
        BTN_Retour = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        txtCIN = new javax.swing.JTextField();
        txtPermis = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        CMBClients = new javax.swing.JComboBox<>();
        CMBReservations = new javax.swing.JComboBox<>();
        txtMarque = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtImmatriculation = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        btnImprrimer = new javax.swing.JToggleButton();
        txtPrixparjour = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        txtAvance = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtReste = new javax.swing.JTextField();
        RBCarrosserie = new javax.swing.JRadioButton();
        RBPneus = new javax.swing.JRadioButton();
        RBSieges = new javax.swing.JRadioButton();
        RBAutres = new javax.swing.JRadioButton();
        jLabel17 = new javax.swing.JLabel();
        CMBMode = new javax.swing.JComboBox<>();
        RBRoue = new javax.swing.JRadioButton();
        RBCric = new javax.swing.JRadioButton();
        RBBeBe = new javax.swing.JRadioButton();
        RBCleDeRoue = new javax.swing.JRadioButton();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        txtAutres = new javax.swing.JTextField();
        txtTele = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtAdresse = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtDuree = new javax.swing.JTextField();
        BTN_NouveauCLT = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jPanel3.setBackground(new java.awt.Color(51, 153, 255));

        jLabel1.setBackground(new java.awt.Color(51, 153, 255));
        jLabel1.setFont(new java.awt.Font("Arial Black", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("CONTRAT DE LOCATION JMCARS");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(114, 114, 114))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jLabel1)
                .addContainerGap(51, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel2.setText("Nom");

        txtNom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel4.setText("Prénom");

        jLabel5.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel5.setText("CIN/Passeport");

        BTN_Enregitrer.setBackground(new java.awt.Color(204, 204, 204));
        BTN_Enregitrer.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        BTN_Enregitrer.setText("Enregistrer ");
        BTN_Enregitrer.setActionCommand("Enregistrer");
        BTN_Enregitrer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_EnregitrerActionPerformed(evt);
            }
        });

        BTN_Retour.setBackground(new java.awt.Color(204, 204, 204));
        BTN_Retour.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        BTN_Retour.setText("Retour");
        BTN_Retour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_RetourActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel6.setText("Permis");

        txtPermis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPermisActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel3.setText("Clients");

        jLabel8.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel8.setText("Reservations");

        CMBClients.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        CMBReservations.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        txtMarque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMarqueActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel9.setText("Marque");

        jLabel10.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N

        txtImmatriculation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtImmatriculationActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel11.setText("Immatriculation");

        btnImprrimer.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        btnImprrimer.setText("IMPRIMER");
        btnImprrimer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprrimerActionPerformed(evt);
            }
        });

        txtPrixparjour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPrixparjourActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel12.setText("Prix/Jour");

        jLabel13.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel13.setText("Durée");

        jLabel14.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel14.setText("Total");

        jLabel15.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel15.setText("Avance");

        jLabel16.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel16.setText("Mode Paiement");

        txtReste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtResteActionPerformed(evt);
            }
        });

        RBCarrosserie.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        RBCarrosserie.setText("Carrousserie");

        RBPneus.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        RBPneus.setText("Pneus");

        RBSieges.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        RBSieges.setText("Sièges");

        RBAutres.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        RBAutres.setText("Autres");

        jLabel17.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel17.setText("Reste A Payer");

        CMBMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Espèce", "Carte Bancaire", "Chèque" }));

        RBRoue.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        RBRoue.setText("Roue de secours");

        RBCric.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        RBCric.setText("Cric");
        RBCric.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RBCricActionPerformed(evt);
            }
        });

        RBBeBe.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        RBBeBe.setText("Siège BéBé");

        RBCleDeRoue.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        RBCleDeRoue.setText("Clé de Roue");

        jLabel19.setFont(new java.awt.Font("Arial Black", 1, 15)); // NOI18N
        jLabel19.setText("Etat du vehicule");

        jLabel20.setFont(new java.awt.Font("Arial Black", 1, 15)); // NOI18N
        jLabel20.setText("Options");

        jLabel21.setFont(new java.awt.Font("Arial Black", 1, 15)); // NOI18N
        jLabel21.setText("Tarifs et Paiement");

        jLabel22.setFont(new java.awt.Font("Arial Black", 1, 15)); // NOI18N
        jLabel22.setText("Client et Vehicule");

        jLabel7.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel7.setText("Telephone");

        jLabel18.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel18.setText("Adresse");

        txtDuree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDureeActionPerformed(evt);
            }
        });

        BTN_NouveauCLT.setBackground(new java.awt.Color(204, 204, 204));
        BTN_NouveauCLT.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        BTN_NouveauCLT.setText("Nouveau Client");
        BTN_NouveauCLT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_NouveauCLTActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel11)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtImmatriculation, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addGap(106, 106, 106)
                                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtPrenom, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtNom, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(58, 58, 58)
                                .addComponent(txtMarque, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel6)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtPermis, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel5)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtCIN))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addGap(31, 31, 31)
                                        .addComponent(txtTele, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel18)
                                        .addGap(48, 48, 48)
                                        .addComponent(txtAdresse, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jLabel22))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21)
                            .addComponent(jLabel12)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addGap(18, 18, 18)
                                .addComponent(txtAvance, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(66, 66, 66)
                                .addComponent(txtPrixparjour, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel13))
                                .addGap(24, 24, 24)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtDuree)
                                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtReste, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel16)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(112, 112, 112)
                                .addComponent(CMBMode, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(RBBeBe, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(RBRoue, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(RBCric, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(RBCleDeRoue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(149, 149, 149)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(RBPneus, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(RBAutres, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(RBCarrosserie, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(RBSieges, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(7, 7, 7)
                                        .addComponent(jLabel19)))
                                .addContainerGap())
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtAutres, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addComponent(BTN_Enregitrer, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(94, 94, 94)
                        .addComponent(BTN_NouveauCLT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(BTN_Retour, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(87, 87, 87))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CMBClients, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(CMBReservations, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52)
                        .addComponent(btnImprrimer, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(24, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel8)
                    .addComponent(CMBClients, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CMBReservations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnImprrimer))
                .addGap(60, 60, 60)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel21)
                            .addComponent(jLabel22))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(RBCarrosserie)
                                    .addComponent(RBPneus))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(RBSieges)
                                    .addComponent(RBAutres))
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(105, 105, 105)
                                        .addComponent(jLabel10)
                                        .addGap(83, 83, 83))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtAutres, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(RBRoue)
                                                    .addComponent(RBCric))
                                                .addGap(30, 30, 30)
                                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(RBBeBe)
                                                    .addComponent(RBCleDeRoue)))
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel20)
                                                .addGap(76, 76, 76)))
                                        .addGap(60, 60, 60))))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtPrixparjour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel12))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(txtNom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel2))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel4)
                                            .addComponent(txtPrenom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel13)
                                            .addComponent(txtDuree, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(txtCIN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel5)
                                            .addComponent(jLabel14)
                                            .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel7)
                                            .addComponent(txtTele, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel15)
                                            .addComponent(txtAvance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(txtAdresse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel17)
                                            .addComponent(txtReste, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel18))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel6)
                                            .addComponent(txtPermis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(8, 8, 8)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(txtMarque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel9))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel11)
                                            .addComponent(txtImmatriculation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel16)
                                        .addComponent(CMBMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_Enregitrer)
                    .addComponent(BTN_Retour)
                    .addComponent(BTN_NouveauCLT))
                .addGap(29, 29, 29))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void txtResteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtResteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtResteActionPerformed

    private void txtPrixparjourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPrixparjourActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPrixparjourActionPerformed

    private void txtMarqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMarqueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMarqueActionPerformed

    private void BTN_RetourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_RetourActionPerformed
        // TODO add your handling code here:
        this.dispose(); // Close current window
        new MainMenuFrame().setVisible(true);
    }//GEN-LAST:event_BTN_RetourActionPerformed

    private void BTN_EnregitrerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_EnregitrerActionPerformed
       // Validate before anything else
    if (!validateInputs()) {
        return;
    }
    
    try {
        // Proceed with registration if validation passes
        Client client = validateAndRegisterClient();
        if (client == null) return;
        
        Contrat contrat = createAndValidateContract(client.getId());
        if (contrat == null) return;
        
        saveContractWithTransaction(contrat);
        updateUIAfterSave(client);
        
    } catch (SQLException ex) {
        showErrorMessage("database", "Erreur lors de la sauvegarde: " + 
            getFriendlyDatabaseError(ex));
    }
    }//GEN-LAST:event_BTN_EnregitrerActionPerformed

    private void txtNomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNomActionPerformed

    private void RBCricActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RBCricActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_RBCricActionPerformed

    private void txtDureeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDureeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDureeActionPerformed

    private void BTN_NouveauCLTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_NouveauCLTActionPerformed
        // TODO add your handling code here:
        clearFormFields();
    }//GEN-LAST:event_BTN_NouveauCLTActionPerformed

    private void btnImprrimerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprrimerActionPerformed
       try {
        // Validate that we have all required data
        if (currentContrat == null || currentClient == null || currentReservation == null) {
            showErrorMessage("impression", "Veuillez d'abord enregistrer le contrat avant d'imprimer");
            return;
        }
        
        PDFGenerator generator = new PDFGenerator();
        File pdfFile = generator.generateContractPDF(currentContrat, currentClient, currentReservation);
        
        if (pdfFile.exists()) {
            Desktop.getDesktop().open(pdfFile);
            showSuccessMessage("impression");
        }
    } catch (Exception e) {
        showErrorMessage("impression", "Erreur lors de la génération du PDF: " + e.getMessage());
        e.printStackTrace();
    }
    }//GEN-LAST:event_btnImprrimerActionPerformed

    private void txtImmatriculationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtImmatriculationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtImmatriculationActionPerformed

    private void txtPermisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPermisActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPermisActionPerformed
    // Helper methods
    // Helper method to register client
    private void saveContractWithTransaction(Contrat contrat) throws SQLException {
    Connection conn = null;
    try {
        conn = DBConnection.getConnection();
        conn.setAutoCommit(false); // Start transaction
        
        // Save contract
        int contratId = ContratDAO.createContrat(contrat);
        contrat.setId(contratId);
        
        conn.commit(); // Commit transaction
        currentContrat = contrat;
        
    } catch (SQLException e) {
        if (conn != null) {
            conn.rollback(); // Rollback on error
        }
        throw e;
    } finally {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
  // Update your validateAndRegisterClient method
private Client validateAndRegisterClient() {
    String nom = txtNom.getText().trim();
    String prenom = txtPrenom.getText().trim();
    String cin = txtCIN.getText().trim();

    if (nom.isEmpty() || prenom.isEmpty() || cin.isEmpty()) {
        showWarningMessage("Nom, Prénom et CIN sont obligatoires");
        return null;
    }

    try {
        Client client = new Client();
        client.setNom(nom);
        client.setPrenom(prenom);
        client.setCin(cin);
        
        // Make sure to set fields separately
        client.setPermis(txtPermis.getText().trim());
        client.setTelephone(txtTele.getText().trim());  // Telephone
        client.setAdresse(txtAdresse.getText().trim()); // Adresse
        
        // Check if client exists
        Client existing = ClientDAO.getClientByCIN(cin);
        if (existing != null) {
            // Update existing client
            existing.setNom(nom);
            existing.setPrenom(prenom);
            existing.setPermis(client.getPermis());
            existing.setTelephone(client.getTelephone());
            existing.setAdresse(client.getAdresse());
            ClientDAO.updateClient(existing);
            return existing;
        } else {
            // Add new client
            int clientId = ClientDAO.addClient(client);
            client.setId(clientId);
            return client;
        }
    } catch (SQLException e) {
        showErrorMessage("database", "Erreur lors de l'enregistrement du client");
        return null;
    }
}
  

    // Improved contract creation and validation
    private Contrat createAndValidateContract(int clientId) {
    try {
        // Validate required fields
        if (txtPrixparjour.getText().trim().isEmpty() || 
            txtDuree.getText().trim().isEmpty() ||
            txtAvance.getText().trim().isEmpty()) {
            showWarningMessage("Prix, durée et avance sont obligatoires");
            return null;
        }
        
        Contrat contrat = new Contrat();
        contrat.setClientId(clientId);
        
        // Set vehicle info
        if (!setVehicleInfoFromReservation(contrat)) {
            return null;
        }
        
        // Set pricing - extract number from "X jours" format
        String dureeText = txtDuree.getText().replaceAll("[^0-9]", "");
        if (dureeText.isEmpty() || Integer.parseInt(dureeText) <= 0) {
    showWarningMessage("La durée doit être d'au moins 1 jour");
    return null;
}
contrat.setDuree(Integer.parseInt(dureeText));
        
        contrat.setPrixJournalier(Double.parseDouble(txtPrixparjour.getText().trim()));
        contrat.setDuree(Integer.parseInt(dureeText)); // Use extracted number
        contrat.setAvance(Double.parseDouble(txtAvance.getText().trim()));
        
        // Set payment method
        contrat.setModePaiement(CMBMode.getSelectedItem().toString());
        
        // Set vehicle condition
        setVehicleCondition(contrat);
        
        // Set options
        setRentalOptions(contrat);
        
        // Calculate totals
        contrat.calculateTotal();
        contrat.calculateResteAPayer();
        
        return contrat;
        
    } catch (NumberFormatException e) {
        showWarningMessage("Veuillez entrer des valeurs numériques valides");
        return null;
    }
}

    private boolean setVehicleInfoFromReservation(Contrat contrat) {
        String selectedReservation = (String) CMBReservations.getSelectedItem();
        if (selectedReservation == null || selectedReservation.trim().isEmpty()) {
            showWarningMessage("Veuillez sélectionner une réservation");
            return false;
        }

        try {
            String clientName = selectedReservation.split(" - ")[0].trim();
            SortieVoiture reservation = SortieDAO.getReservationWithVehicle(clientName);
            
            if (reservation == null) {
                showWarningMessage("Aucune réservation trouvée pour ce client");
                return false;
            }
            
            contrat.setVoitureId(reservation.getVoitureId());
            
            // Auto-fill vehicle info
            txtMarque.setText(reservation.getMarque());
            txtImmatriculation.setText(reservation.getImmatriculation());
            
            return true;
            
        } catch (SQLException e) {
            showErrorMessage("base de données","Erreur dans setVehicleInfoFromReservations");
            return false;
        }
    }

    private void setVehicleCondition(Contrat contrat) {
    contrat.setCarrosserieOk(RBCarrosserie.isSelected());
    contrat.setPneusOk(RBPneus.isSelected());
    contrat.setSiegesOk(RBSieges.isSelected());
    
    // Handle autres remarks
    if (RBAutres.isSelected() && !txtAutres.getText().trim().isEmpty()) {
        contrat.setAutresRemarques(txtAutres.getText().trim());
    } else {
        contrat.setAutresRemarques(null);
    }
}

    private void setRentalOptions(Contrat contrat) {
        contrat.setRoueSecours(RBRoue.isSelected());
        contrat.setCric(RBCric.isSelected());
        contrat.setSiegeBebe(RBBeBe.isSelected());
        contrat.setCleRoue(RBCleDeRoue.isSelected());
    }

    

    private void updateUIAfterSave(Client client) {
    currentClient = client;
    refreshClientComboBox();
    clearFormFields();
    showSuccessMessage("save"); // Use the new version
}
    
private void showFieldError(String fieldName, String message) {
    showErrorMessage("validation", "<html>Champ <b>" + fieldName + "</b>: " + message + "</html>");
}


// Utility message methods
private void showWarningMessage(String message) {
    JOptionPane.showMessageDialog(this, message, "Avertissement", JOptionPane.WARNING_MESSAGE);
}


private void showSuccessMessage(String operation) {
    String message = "";
    switch(operation) {
        case "save":
            message = "Votre contrat a été enregistré avec succès!";
            break;
        case "impression":
            message = "Le contrat a été généré avec succès";
            break;
        default:
            message = "Opération réussie";
    }
    JOptionPane.showMessageDialog(this, 
        message, 
        "Succès", 
        JOptionPane.INFORMATION_MESSAGE);
}

private void showErrorMessage(String context, String details) {
    String message = "Désolé, une erreur s'est produite";
    
    if (context.contains("base de données")) {
        message = "Problème de connexion à la base de données";
    } else if (context.contains("validation")) {
        message = "Informations manquantes ou incorrectes";
    } else if (context.equals("impression")) {
        message = "Impossible d'imprimer le contrat";
    }
    
    JOptionPane.showMessageDialog(this, 
        "<html><b>" + message + "</b><br>" + (details != null ? details : ""), 
        "Attention", 
        JOptionPane.WARNING_MESSAGE);
}


    private void refreshClientComboBox() {
    try {
        List<Client> clients = ClientDAO.getAllClients();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (Client client : clients) {
            model.addElement(client.getNom() + " " + client.getPrenom());
        }
        CMBClients.setModel(model);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Error loading clients: " + e.getMessage(),
            "Database Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}
    private void clearFormFields() {
    // Client Information
    txtNom.setText("");
    txtPrenom.setText("");
    txtCIN.setText("");
    txtPermis.setText("");
    txtTele.setText("");
    txtAdresse.setText("");
    txtMarque.setText("");
    txtImmatriculation.setText("");
    
    // Contract Information
    txtPrixparjour.setText("");
    txtDuree.setText("");
    txtAvance.setText("");
    txtTotal.setText("");
    txtReste.setText("");
    CMBMode.setSelectedIndex(-1);
    
    // Vehicle State Checkboxes
    RBCarrosserie.setSelected(false);
    RBPneus.setSelected(false);
    RBSieges.setSelected(false);
    RBAutres.setSelected(false);
    txtAutres.setText("");
    txtAutres.setVisible(false); // Hide if not already hidden
    
    // Options Checkboxes
    RBRoue.setSelected(false);
    RBCric.setSelected(false);
    RBBeBe.setSelected(false);
    RBCleDeRoue.setSelected(false);
     // Clear any selection in combo boxes
    CMBClients.setSelectedIndex(-1);
    CMBReservations.setSelectedIndex(-1);
    // Reset focus
    txtNom.requestFocus();
}
     private String getFriendlyDatabaseError(SQLException ex) {
    if (ex.getMessage().contains("foreign key")) {
        return "Référence invalide - véhicule ou client introuvable";
    } else if (ex.getMessage().contains("duplicate")) {
        return "Ce contrat existe déjà";
    } else if (ex.getMessage().contains("connection")) {
        return "Problème de connexion à la base de données";
    }
    return "Erreur technique lors de l'enregistrement";
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
            java.util.logging.Logger.getLogger(GestionDesContrats.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GestionDesContrats.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GestionDesContrats.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GestionDesContrats.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GestionDesContrats().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BTN_Enregitrer;
    private javax.swing.JButton BTN_NouveauCLT;
    private javax.swing.JButton BTN_Retour;
    private javax.swing.JComboBox<String> CMBClients;
    private javax.swing.JComboBox<String> CMBMode;
    private javax.swing.JComboBox<String> CMBReservations;
    private javax.swing.JRadioButton RBAutres;
    private javax.swing.JRadioButton RBBeBe;
    private javax.swing.JRadioButton RBCarrosserie;
    private javax.swing.JRadioButton RBCleDeRoue;
    private javax.swing.JRadioButton RBCric;
    private javax.swing.JRadioButton RBPneus;
    private javax.swing.JRadioButton RBRoue;
    private javax.swing.JRadioButton RBSieges;
    private javax.swing.JToggleButton btnImprrimer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField txtAdresse;
    private javax.swing.JTextField txtAutres;
    private javax.swing.JTextField txtAvance;
    private javax.swing.JTextField txtCIN;
    private javax.swing.JTextField txtDuree;
    private javax.swing.JTextField txtImmatriculation;
    private javax.swing.JTextField txtMarque;
    private javax.swing.JTextField txtNom;
    private javax.swing.JTextField txtPermis;
    private javax.swing.JTextField txtPrenom;
    private javax.swing.JTextField txtPrixparjour;
    private javax.swing.JTextField txtReste;
    private javax.swing.JTextField txtTele;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
