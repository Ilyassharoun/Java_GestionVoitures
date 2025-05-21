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
import java.awt.Dimension;
import java.awt.Graphics2D;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import services.PDFGenerator;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import static org.apache.commons.io.IOUtils.writer;

import org.apache.commons.io.output.ByteArrayOutputStream;
/**
 *
 * @author samsung
 */
public class GestionDesContrats extends javax.swing.JFrame {

    /**
     * Creates new form GestionDesContrats
     */
    private BufferedImage cinImage;
private BufferedImage permisImage;
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
        PanelSecondDriver.setVisible(false); 
        
    }
    private void initializeComponents() {
    setTitle("Gestion Des Contrats - JMCars");
    setLocationRelativeTo(null);
    //-----------
    dateexpiration_CIN.setDateFormatString("dd/MM/yyyy");
    dateexpiration_permis.setDateFormatString("dd/MM/yyyy");
    // Load initial data
    refreshClientComboBox();
    populateReservationComboBox();
    setupComboBoxListeners();
    //seconddriver 
    chkSecondDriver.addActionListener(e -> toggleSecondDriverPanel());
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
    private void toggleSecondDriverPanel() {
    boolean showPanel = chkSecondDriver.isSelected();
    PanelSecondDriver.setVisible(showPanel);
    
    if (showPanel) {
        PanelSecondDriver.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    } else {
        PanelSecondDriver.setMaximumSize(new Dimension(0, 0));
        // Clear fields when hiding
        txtSecondNom.setText("");
        txtSecondPrenom.setText("");
        txtSecondCIN.setText("");
        txtSecondPermis.setText("");
        txtSecondTele.setText("");
        txtSecondAdresse.setText("");
    }
    
    revalidate();
    repaint();
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
        List<SortieVoiture> reservations = SortieDAO.getActiveReservationsWithVehicles();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        
        for (SortieVoiture reservation : reservations) {
            // Build vehicle information
            StringBuilder vehicleInfo = new StringBuilder();
            
            // Add marque and modele if available
            if (reservation.getMarque() != null) {
                vehicleInfo.append(reservation.getMarque());
                if (reservation.getModele() != null) {
                    vehicleInfo.append(" ").append(reservation.getModele());
                }
            }
            
            // Add immatriculation if available
            if (reservation.getImmatriculation() != null) {
                if (vehicleInfo.length() > 0) {
                    vehicleInfo.append(" ");
                }
                vehicleInfo.append("(").append(reservation.getImmatriculation()).append(")");
            }
            
            // If no vehicle info at all
            if (vehicleInfo.length() == 0) {
                vehicleInfo.append("Véhicule non spécifié");
            }
            
            // Combine client and vehicle info without status
            String displayText = String.format("%s - %s",
                reservation.getClient(),
                vehicleInfo.toString());
            
            model.addElement(displayText);
        }
        
        CMBReservations.setModel(model);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Erreur de chargement des réservations: " + e.getMessage(),
            "Erreur", JOptionPane.ERROR_MESSAGE);
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
                    loadClientData(client);
                    displayClientImages(client); // New method to handle image display
                }
            }
        } catch (SQLException ex) {
            showErrorMessage("database", "Erreur de chargement du client: " + ex.getMessage());
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
                // Set vehicle info
                txtMarque.setText(currentReservation.getMarqueModele());
                txtImmatriculation.setText(currentReservation.getImmatriculation());
                
                // Calculate duration based on reservation status and dates
                String duration = calculateDuree(currentReservation);
                txtDuree.setText(duration);
                
                // Set payment info
                txtAvance.setText(String.format("%.2f", 
                    currentReservation.getAvance() > 0 ? currentReservation.getAvance() : 0.0));
                
                // Auto-calculate contract values if price is set
                if (!txtPrixparjour.getText().trim().isEmpty()) {
                    calculateContractValues();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            clearReservationFields();
        }
    });
}
private void displayClientImages(Client client) {
    // Display CIN image
    
    if (client.getCinImageData() != null && client.getCinImageData().length > 0) {
        cinImage = bytesToImage(client.getCinImageData());
        lblCinpreview.setText(client.getCinFilename() != null ? 
                           client.getCinFilename() : "CIN_" + client.getCin() + ".jpg");
       
    } else {
        cinImage = null;
        lblCinpreview.setText("Aucune image disponible");
    }

    // Display Permis image
    if (client.getPermisImageData() != null && client.getPermisImageData().length > 0) {
        permisImage = bytesToImage(client.getPermisImageData());
        lblPermispreview.setText(client.getPermisFilename() != null ? 
                              client.getPermisFilename() : "Permis_" + client.getCin() + ".jpg");
        
    } else {
        permisImage = null;
        lblPermispreview.setText("Aucune image disponible");
    }
}
private void loadClientData(Client client) {
    
    if (client == null) {
        
        return;
    }

    // Load basic client information
    txtNom.setText(client.getNom());
    txtPrenom.setText(client.getPrenom());
    txtCIN.setText(client.getCin());
    txtPermis.setText(client.getPermis());
    txtTele.setText(client.getTelephone());
    txtAdresse.setText(client.getAdresse());
    dateexpiration_CIN.setDate(client.getCinExpiration());
    dateexpiration_permis.setDate(client.getPermisExpiration());

    // Load CIN image data

    if (client.getCinImageData() != null && client.getCinImageData().length > 0) {

        
        // Set filename in UI
        String cinFilename = client.getCinFilename() != null ? 
                           client.getCinFilename() : "CIN_" + client.getCin() + ".jpg";
        lblCinpreview.setText(cinFilename);
        
        try {
            cinImage = ImageIO.read(new ByteArrayInputStream(client.getCinImageData()));
          
        } catch (IOException e) {
        
            lblCinpreview.setText("Image corrompue");
            cinImage = null;
        }
    } else {
       
        lblCinpreview.setText("Aucune image disponible");
        cinImage = null;
    }

    // Load Permis image data
 
    if (client.getPermisImageData() != null && client.getPermisImageData().length > 0) {
      
        
        // Set filename in UI
        String permisFilename = client.getPermisFilename() != null ? 
                              client.getPermisFilename() : "Permis_" + client.getCin() + ".jpg";
        lblPermispreview.setText(permisFilename);
      
        
        // Load image data
        try {
            permisImage = ImageIO.read(new ByteArrayInputStream(client.getPermisImageData()));
            
        } catch (IOException e) {
            
            lblPermispreview.setText("Image corrompue");
            permisImage = null;
        }
    } else {
        
        lblPermispreview.setText("Aucune image disponible");
        permisImage = null;
    }

  
}
private BufferedImage convertToStandardRGB(BufferedImage image) {
    if (image == null) return null;
    
    // Create a new RGB image
    BufferedImage newImage = new BufferedImage(
        image.getWidth(), 
        image.getHeight(),
        BufferedImage.TYPE_INT_RGB);
    
    // Draw the original image onto the new RGB image
    Graphics2D g = newImage.createGraphics();
    try {
        g.drawImage(image, 0, 0, null);
    } finally {
        g.dispose();
    }
    return newImage;
}
private BufferedImage bytesToImage(byte[] imageData) {
    if (imageData == null || imageData.length == 0) {
        return null;
    }

    try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
        // Read the image
        BufferedImage originalImage = ImageIO.read(bais);
        if (originalImage == null) {
            throw new IOException("Failed to read image data");
        }

        // Convert to standard RGB format if needed
        BufferedImage convertedImage = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(),
            BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g = convertedImage.createGraphics();
        try {
            g.drawImage(originalImage, 0, 0, null);
        } finally {
            g.dispose();
        }
        
        return convertedImage;
    } catch (IOException e) {
        System.err.println("Error converting bytes to image: " + e.getMessage());
        return null;
    }
}
private String calculateDuree(SortieVoiture reservation) {
    try {
        // For "En sortie" status - use current date as start date if date_sortie is null
        if ("En sortie".equals(reservation.getStatut())) {
            Date startDate = reservation.getDateSortie() != null ? 
                           reservation.getDateSortie() : 
                           new Date(); // Use current date if date_sortie is null
            
            if (reservation.getDateRetour() != null) {
                long diff = reservation.getDateRetour().getTime() - startDate.getTime();
                long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                return days + " jours";
            }
        }
        // For "Réservé" status - use date_debut (stored in date_reservation) and date_retour
        else if ("Réservé".equals(reservation.getStatut())) {
            if (reservation.getDateReservation() != null && reservation.getDateRetour() != null) {
                long diff = reservation.getDateRetour().getTime() - reservation.getDateReservation().getTime();
                long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) ;
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
    if (dateexpiration_CIN.getDate() == null) {
        showFieldError("Date expiration CIN", "Veuillez sélectionner une date d'expiration");
        return false;
    }
    
    if (dateexpiration_permis.getDate() == null) {
        showFieldError("Date expiration permis", "Veuillez sélectionner une date d'expiration");
        return false;
    }
    // Payment Mode Validation
    if (CMBMode.getSelectedItem() == null) {
        showFieldError("Mode de paiement", "Veuillez sélectionner un mode de paiement");
        CMBMode.requestFocus();
        return false;
    }
    // Validate CIN image
    if (lblCinpreview.getText().equals("Aucune image disponible")) {
        showFieldError("CIN/Passeport", "Veuillez uploader une image de CIN/Passeport");
        btnCin.requestFocus();
        return false;
    }
    
    // Validate Permis image
    if (lblPermispreview.getText().equals("Aucune image disponible")) {
        showFieldError("Permis", "Veuillez uploader une image de Permis");
        btnPermis.requestFocus();
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
        jLabel10 = new javax.swing.JLabel();
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
        chkSecondDriver = new javax.swing.JCheckBox();
        PanelSecondDriver = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        txtSecondTele = new javax.swing.JTextField();
        txtSecondNom = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        txtSecondPrenom = new javax.swing.JTextField();
        txtSecondAdresse = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        txtSecondCIN = new javax.swing.JTextField();
        txtSecondPermis = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        txtImmatriculation = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtMarque = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        dateexpiration_CIN = new com.toedter.calendar.JDateChooser();
        dateexpiration_permis = new com.toedter.calendar.JDateChooser();
        jLabel33 = new javax.swing.JLabel();
        btnCin = new javax.swing.JButton();
        lblCinpreview = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        btnPermis = new javax.swing.JButton();
        lblPermispreview = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setPreferredSize(new java.awt.Dimension(500, 721));

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
                .addGap(100, 100, 100))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jLabel1)
                .addContainerGap(42, Short.MAX_VALUE))
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

        jLabel10.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N

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
        jLabel22.setText("Véhicule");

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

        chkSecondDriver.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        chkSecondDriver.setText("Ajouter un deuxième conducteur");

        PanelSecondDriver.setBackground(new java.awt.Color(255, 255, 255));

        jLabel23.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel23.setText("Nom");

        txtSecondNom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSecondNomActionPerformed(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel24.setText("Telephone");

        jLabel25.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel25.setText("Prénom");

        jLabel26.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel26.setText("Adresse");

        jLabel27.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel27.setText("CIN/Passeport");

        jLabel28.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel28.setText("Permis");

        txtSecondPermis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSecondPermisActionPerformed(evt);
            }
        });

        jLabel30.setFont(new java.awt.Font("Arial Black", 1, 15)); // NOI18N
        jLabel30.setText("Deuxiéme Conducteur");

        javax.swing.GroupLayout PanelSecondDriverLayout = new javax.swing.GroupLayout(PanelSecondDriver);
        PanelSecondDriver.setLayout(PanelSecondDriverLayout);
        PanelSecondDriverLayout.setHorizontalGroup(
            PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelSecondDriverLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel24)
                        .addGroup(PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PanelSecondDriverLayout.createSequentialGroup()
                                .addGroup(PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel27)
                                    .addComponent(jLabel26))
                                .addGroup(PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(PanelSecondDriverLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(txtSecondTele, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtSecondCIN, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(PanelSecondDriverLayout.createSequentialGroup()
                                        .addGap(8, 8, 8)
                                        .addComponent(txtSecondAdresse, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jLabel25)
                            .addComponent(jLabel23)
                            .addComponent(jLabel28)
                            .addComponent(jLabel30)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelSecondDriverLayout.createSequentialGroup()
                                .addGap(108, 108, 108)
                                .addComponent(txtSecondPermis, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(txtSecondPrenom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSecondNom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27))
        );
        PanelSecondDriverLayout.setVerticalGroup(
            PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelSecondDriverLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel30)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSecondNom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(txtSecondPrenom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSecondCIN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24)
                    .addGroup(PanelSecondDriverLayout.createSequentialGroup()
                        .addComponent(txtSecondTele, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSecondAdresse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelSecondDriverLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel28)
                    .addComponent(txtSecondPermis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel29.setFont(new java.awt.Font("Arial Black", 1, 15)); // NOI18N
        jLabel29.setText("Client Principale");

        txtImmatriculation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtImmatriculationActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel11.setText("Immatriculation");

        txtMarque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMarqueActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel9.setText("Marque");

        jLabel31.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel31.setText("Date expiration");

        jLabel32.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel32.setText("Date expiration");

        jLabel33.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel33.setText("Upload CIN/Passeport");

        btnCin.setBackground(new java.awt.Color(204, 204, 204));
        btnCin.setText("Choisir");
        btnCin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCinActionPerformed(evt);
            }
        });

        lblCinpreview.setText("Aucun fichier sélectionné");

        jLabel34.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel34.setText("Upload Permis");

        btnPermis.setBackground(new java.awt.Color(204, 204, 204));
        btnPermis.setText("Choisir");
        btnPermis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPermisActionPerformed(evt);
            }
        });

        lblPermispreview.setText("Aucun fichier sélectionné");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(RBRoue, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(RBCric, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(RBBeBe, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(RBCarrosserie, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(RBPneus, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(RBSieges, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(RBCleDeRoue, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(RBAutres)
                                .addGap(18, 18, 18)
                                .addComponent(txtAutres, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CMBClients, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addGap(138, 138, 138)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                                            .addComponent(jLabel13)
                                            .addComponent(jLabel14))
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
                                        .addComponent(CMBMode, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(jLabel21))))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(CMBReservations, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnImprrimer, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24))))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lblPermispreview, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel4)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGap(106, 106, 106)
                                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(txtPrenom, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(txtNom, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel5)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txtCIN))
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addGroup(jPanel2Layout.createSequentialGroup()
                                                    .addComponent(jLabel18)
                                                    .addGap(48, 48, 48)
                                                    .addComponent(txtAdresse, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(jPanel2Layout.createSequentialGroup()
                                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                                            .addComponent(jLabel7)
                                                            .addGap(31, 31, 31))
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                            .addComponent(jLabel32)
                                                            .addGap(3, 3, 3)))
                                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(txtTele, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(dateexpiration_CIN, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel6)
                                                    .addComponent(jLabel31))
                                                .addGap(5, 6, Short.MAX_VALUE)
                                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(txtPermis)
                                                    .addComponent(dateexpiration_permis, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE))))
                                        .addComponent(jLabel19)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(chkSecondDriver, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGap(18, 18, 18)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel11)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txtImmatriculation, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel9)
                                                .addGap(58, 58, 58)
                                                .addComponent(txtMarque)))
                                        .addComponent(PanelSecondDriver, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addGap(72, 72, 72)
                                    .addComponent(BTN_Enregitrer, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(68, 68, 68)
                                    .addComponent(BTN_NouveauCLT, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addGap(28, 28, 28)
                                    .addComponent(BTN_Retour, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(btnPermis, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(btnCin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(lblCinpreview, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGap(30, 30, 30))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(32, 32, 32)
                    .addComponent(jLabel29)
                    .addContainerGap(647, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel8)
                    .addComponent(CMBClients, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CMBReservations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnImprrimer))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addGap(17, 17, 17)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtMarque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel9))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel11)
                                    .addComponent(txtImmatriculation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtNom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4)
                                    .addComponent(txtPrenom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtCIN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(PanelSecondDriver, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel32)
                                    .addComponent(dateexpiration_CIN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel7)
                                    .addComponent(txtTele, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtAdresse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel18))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel6)
                                    .addComponent(txtPermis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel31)
                                    .addComponent(dateexpiration_permis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(chkSecondDriver)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(RBCarrosserie)
                            .addComponent(RBPneus)
                            .addComponent(RBSieges)
                            .addComponent(RBAutres)
                            .addComponent(txtAutres, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtPrixparjour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel12))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel13)
                                    .addComponent(txtDuree, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel14)
                                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel15)
                                    .addComponent(txtAvance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel17)
                                    .addComponent(txtReste, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(14, 14, 14)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(CMBMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(37, 37, 37)
                        .addComponent(jLabel33)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCin)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCinpreview)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel34)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnPermis)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPermispreview)
                        .addGap(0, 9, Short.MAX_VALUE)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(RBRoue)
                            .addComponent(RBCric)
                            .addComponent(RBBeBe)
                            .addComponent(RBCleDeRoue))
                        .addGap(33, 33, 33)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(BTN_Enregitrer)
                            .addComponent(BTN_NouveauCLT)
                            .addComponent(BTN_Retour))))
                .addContainerGap(19, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(180, 180, 180)
                    .addComponent(jLabel29)
                    .addContainerGap(519, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 824, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE)
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

    private void txtMarqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMarqueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMarqueActionPerformed

    private void txtImmatriculationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtImmatriculationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtImmatriculationActionPerformed

    private void txtSecondPermisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSecondPermisActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSecondPermisActionPerformed

    private void txtSecondNomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSecondNomActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSecondNomActionPerformed

    private void BTN_NouveauCLTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_NouveauCLTActionPerformed
        // TODO add your handling code here:
        clearFormFields();
    }//GEN-LAST:event_BTN_NouveauCLTActionPerformed

    private void txtDureeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDureeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDureeActionPerformed

    private void RBCricActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RBCricActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_RBCricActionPerformed

    private void txtResteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtResteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtResteActionPerformed

    private void txtPrixparjourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPrixparjourActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPrixparjourActionPerformed

    private void btnImprrimerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprrimerActionPerformed
    try {
        // 1. Validate we have all required data
        if (currentContrat == null || currentClient == null || currentReservation == null) {
            showErrorMessage("impression", "Veuillez d'abord enregistrer le contrat avant d'imprimer");
            return;
        }

        // 2. Generate PDF
        PDFGenerator generator = new PDFGenerator();
        File pdfFile = generator.generateContractPDF(currentContrat, currentClient, currentReservation);

        if (pdfFile.exists()) {
            Desktop.getDesktop().open(pdfFile);
            
            // Remove from combo box
            removeReservationFromComboBox(currentReservation);
            showSuccessMessage("impression");
        }
    } catch (Exception e) {
        showErrorMessage("impression", "Erreur lors de la génération du PDF: " + e.getMessage());
        e.printStackTrace();
    }
    }//GEN-LAST:event_btnImprrimerActionPerformed
   private void removeReservationFromComboBox(SortieVoiture reservation) {
    DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) CMBReservations.getModel();
    
    // Find and remove the matching reservation
    for (int i = 0; i < model.getSize(); i++) {
        String item = model.getElementAt(i);
        if (item.contains(reservation.getImmatriculation())) { // Match by license plate
            model.removeElementAt(i);
            break;
        }
    }
    
    currentReservation = null;
    txtMarque.setText("");
    txtImmatriculation.setText("");
}
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
        // Save primary client
        Client client = validateAndRegisterClient();
        if (client == null) return;

        // Create second driver object if checkbox is checked, but don't save to DB
        Client secondDriver = null;
        if (chkSecondDriver.isSelected()) {
            if (!validateSecondDriverInputs()) {
                return;
            }
            secondDriver = new Client();
            secondDriver.setNom(txtSecondNom.getText());
            secondDriver.setPrenom(txtSecondPrenom.getText());
            secondDriver.setCin(txtSecondCIN.getText());
            secondDriver.setPermis(txtSecondPermis.getText());
            secondDriver.setTelephone(txtSecondTele.getText());
            secondDriver.setAdresse(txtSecondAdresse.getText());
            // Note: We are NOT saving secondDriver to the database
        }

        Contrat contrat = createAndValidateContract(client.getId());
        if (contrat == null) return;

        // Set second driver if exists
        if (secondDriver != null) {
            contrat.setSecondDriver(secondDriver);
        }

        saveContractWithTransaction(contrat);
        updateUIAfterSave(client);

    } catch (SQLException ex) {
        showErrorMessage("database", "Erreur lors de la sauvegarde: " + ex.getMessage());
    }
    }//GEN-LAST:event_BTN_EnregitrerActionPerformed

    private void txtNomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNomActionPerformed

    private void txtPermisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPermisActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPermisActionPerformed

    private void btnCinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCinActionPerformed
        // TODO add your handling code here:
        uploadImage("CIN");
    }//GEN-LAST:event_btnCinActionPerformed
   private void uploadImage(String type) {
    JFileChooser fileChooser = createModernFileChooser();
    
    int returnValue = fileChooser.showOpenDialog(this);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile != null) {
            try {
                // Use ImageIO to read the image
                BufferedImage image = ImageIO.read(selectedFile);
                if (image == null) {
                    throw new IOException("Unsupported image format");
                }

                // Convert to standard RGB format
                BufferedImage convertedImage = convertToStandardRGB(image);

                if (type.equals("CIN")) {
                    cinImage = convertedImage;
                    lblCinpreview.setText(selectedFile.getName());
                } else {
                    permisImage = convertedImage;
                    lblPermispreview.setText(selectedFile.getName());
                }
            } catch (Exception ex) {
                showErrorMessage("image", 
                    "Erreur de chargement d'image. Veuillez essayer:\n" +
                    "1. Une autre image JPG/PNG\n" +
                    "2. Convertir l'image avec un autre logiciel\n" +
                    "3. Prendre une nouvelle photo\n\n" +
                    "Détails: " + ex.getMessage());
            }
        }
    }
}
    private JFileChooser createModernFileChooser() {
    try {
        // Set system look and feel for native file chooser
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
        // Fallback to default if system LAF fails
    }
    
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Select File");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setAcceptAllFileFilterUsed(false);
    
    // Modern file filters
    FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
        "Image Files (JPG, PNG)", "jpg", "jpeg", "png");
    fileChooser.addChoosableFileFilter(imageFilter);
    
    // Optional: Set default directory
    fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    
    return fileChooser;
}
    
    private void btnPermisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPermisActionPerformed
        // TODO add your handling code here:
        uploadImage("PERMIS");
    }//GEN-LAST:event_btnPermisActionPerformed
private boolean validateSecondDriverInputs() {
    if (txtSecondNom.getText().trim().isEmpty()) {
        showFieldError("Nom (2nd conducteur)", "Veuillez entrer le nom du deuxième conducteur");
        return false;
    }
    // Add validation for other required fields
    return true;
}    // Helper methods
    // Helper method to register client
    private void saveContractWithTransaction(Contrat contrat) throws SQLException {
    Connection conn = null;
    try {
        conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        
        // 1. Save primary client (always save this one)
        Client primaryClient = validateAndRegisterClient();
        if (primaryClient == null) {
            conn.rollback();
            return;
        }
        contrat.setClientId(primaryClient.getId());
        
        // 2. Save the contract
        int contratId = ContratDAO.createContrat(contrat);
        contrat.setId(contratId);
        
        // 3. Handle second driver WITHOUT saving to clients table
        if (chkSecondDriver.isSelected()) {
            // Create second driver object but don't save to DB
            Client secondDriver = new Client();
            secondDriver.setNom(txtSecondNom.getText());
            secondDriver.setPrenom(txtSecondPrenom.getText());
            secondDriver.setCin(txtSecondCIN.getText());
            secondDriver.setPermis(txtSecondPermis.getText());
            secondDriver.setTelephone(txtSecondTele.getText());
            secondDriver.setAdresse(txtSecondAdresse.getText());
            
            // Attach to contract but won't be saved in clients table
            contrat.setSecondDriver(secondDriver);
        }
        
        conn.commit();
        currentContrat = contrat;
        
    } catch (SQLException e) {
        if (conn != null) conn.rollback();
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
    if (txtNom.getText().trim().isEmpty() || txtPrenom.getText().trim().isEmpty() || txtCIN.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Nom, Prénom et CIN sont obligatoires", "Erreur", JOptionPane.ERROR_MESSAGE);
        return null;
    }

    try {
        Client client = new Client();
        client.setNom(txtNom.getText().trim());
        client.setPrenom(txtPrenom.getText().trim());
        client.setCin(txtCIN.getText().trim());
        client.setPermis(txtPermis.getText().trim());
        client.setTelephone(txtTele.getText().trim());
        client.setAdresse(txtAdresse.getText().trim());
        client.setCinExpiration(dateexpiration_CIN.getDate());
        client.setPermisExpiration(dateexpiration_permis.getDate());

        // Validate images before conversion
        if (cinImage != null) {
        try {
            // Test if we can convert the image
            BufferedImage testImage = new BufferedImage(
                cinImage.getWidth(), 
                cinImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);
            Graphics2D g = testImage.createGraphics();
            g.drawImage(cinImage, 0, 0, null);
            g.dispose();
            
            // If successful, proceed with conversion
            byte[] cinData = imageToBytes(cinImage);
            client.setCinImageData(cinData);
            client.setCinFilename(lblCinpreview.getText());
        } catch (Exception e) {
            showErrorMessage("image", "Erreur de conversion CIN: " + e.getMessage());
            return null;
        }
    }

        if (cinImage != null) {
            client.setCinImageData(imageToBytes(cinImage));
            client.setCinFilename(lblCinpreview.getText());
        }
        
        // Permis Image (missing in your current code)
        if (permisImage != null) {
            client.setPermisImageData(imageToBytes(permisImage)); // This was likely missing
            client.setPermisFilename(lblPermispreview.getText());
        }

        Client existing = ClientDAO.getClientByCIN(client.getCin());
        if (existing != null) {
            existing.setNom(client.getNom());
            existing.setPrenom(client.getPrenom());
            existing.setPermis(client.getPermis());
            existing.setTelephone(client.getTelephone());
            existing.setAdresse(client.getAdresse());
            existing.setCinExpiration(client.getCinExpiration());
            existing.setPermisExpiration(client.getPermisExpiration());
            
            if (client.getCinImageData() != null) {
                existing.setCinImageData(client.getCinImageData());
                existing.setCinFilename(client.getCinFilename());
            }
            if (client.getPermisImageData() != null) {
                existing.setPermisImageData(client.getPermisImageData());
                existing.setPermisFilename(client.getPermisFilename());
            }
            
            if (ClientDAO.updateClient(existing)) {
                return existing;
            }
        } else {
            int clientId = ClientDAO.addClient(client);
            if (clientId > 0) {
                client.setId(clientId);
                return client;
            }
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement du client: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
    return null;
}
 private byte[] imageToBytes(BufferedImage image) throws IOException {
    if (image == null) return null;

    // Ensure image is in RGB format
    BufferedImage rgbImage = convertToStandardRGB(image);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
        // Write as JPEG with 80% quality
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (writers.hasNext()) {
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.8f);
            
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(rgbImage, null, null), param);
                writer.dispose();
            }
        } else {
            // Fallback to PNG if JPEG writer not available
            ImageIO.write(rgbImage, "png", baos);
        }
        return baos.toByteArray();
    } finally {
        baos.close();
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
    String message;
    switch(context) {
        case "image":
            message = "<html><b>Erreur d'image</b><br>" +
                     "Le format d'image n'est pas supporté.<br>" +
                     "Veuillez utiliser des images JPG ou PNG standard.<br><br>" +
                     "<small>Détails: " + details + "</small></html>";
            break;
        default:
            message = "<html><b>Erreur</b><br>" + details + "</html>";
    }
    
    JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
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
    dateexpiration_CIN.setDate(null);  // Clear CIN expiration date
    dateexpiration_permis.setDate(null); 
    
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
    //--------
    txtSecondNom.setText("");
        txtSecondPrenom.setText("");
        txtSecondCIN.setText("");
        txtSecondPermis.setText("");
        txtSecondTele.setText("");
        txtSecondAdresse.setText("");
    //--------------
    lblCinpreview.setText("Aucun fichier sélectionné");
    lblPermispreview.setText("Aucun fichier sélectionné");
    cinImage = null;
    permisImage = null;
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
    private javax.swing.JPanel PanelSecondDriver;
    private javax.swing.JRadioButton RBAutres;
    private javax.swing.JRadioButton RBBeBe;
    private javax.swing.JRadioButton RBCarrosserie;
    private javax.swing.JRadioButton RBCleDeRoue;
    private javax.swing.JRadioButton RBCric;
    private javax.swing.JRadioButton RBPneus;
    private javax.swing.JRadioButton RBRoue;
    private javax.swing.JRadioButton RBSieges;
    private javax.swing.JButton btnCin;
    private javax.swing.JToggleButton btnImprrimer;
    private javax.swing.JButton btnPermis;
    private javax.swing.JCheckBox chkSecondDriver;
    private com.toedter.calendar.JDateChooser dateexpiration_CIN;
    private com.toedter.calendar.JDateChooser dateexpiration_permis;
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
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblCinpreview;
    private javax.swing.JLabel lblPermispreview;
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
    private javax.swing.JTextField txtSecondAdresse;
    private javax.swing.JTextField txtSecondCIN;
    private javax.swing.JTextField txtSecondNom;
    private javax.swing.JTextField txtSecondPermis;
    private javax.swing.JTextField txtSecondPrenom;
    private javax.swing.JTextField txtSecondTele;
    private javax.swing.JTextField txtTele;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
} 
