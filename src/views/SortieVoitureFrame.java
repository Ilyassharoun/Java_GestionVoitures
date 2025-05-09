/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package views;


import java.util.Optional;
import javax.swing.JOptionPane;
import dao.SortieDAO;
import dao.VoitureDAO;
import models.SortieVoiture;
import models.Voiture;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author samsung
 */
public class SortieVoitureFrame extends javax.swing.JFrame {

    /**
     * Creates new form SortieVoitureFrame
     */
    // src/views/SortieVoitureFrame.java
    private VoitureDAO voitureDAO = new VoitureDAO();
    private SortieDAO sortieDAO = new SortieDAO();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    // Add these fields
    
    
    
    public SortieVoitureFrame() {
        initComponents();
        setLocationRelativeTo(null);
        setTitle("Gestion Sortie Des Voitures");
        initCustomComponents();
        setIconImage(new ImageIcon(getClass().getResource("/resources/jmlogo.png")).getImage());
        startStatusUpdater();
        setupTableColumns(); // Set up columns first
        setResizable(false); 
        
    }
    private void startStatusUpdater() {
    Timer timer = new Timer(60000, e -> { // Check every minute
        try {
            // 1. Convert eligible reservations to active rentals
            int updatedCount = sortieDAO.activateEligibleReservations();
            
            // 2. Refresh table if any changes were made
            if (updatedCount > 0) {
                refreshTableData();
                System.out.println("Updated " + updatedCount + " reservations to active status");
            }
        } catch (SQLException ex) {
            showError("Erreur de mise à jour: " + ex.getMessage());
        }
    });
    timer.setInitialDelay(0); // Run immediately on startup
    timer.start();
}
    
    private void setupTableColumns() {
    TabledeDonnees.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
    TabledeDonnees.getColumnModel().getColumn(1).setPreferredWidth(100); // Plaque
    TabledeDonnees.getColumnModel().getColumn(2).setPreferredWidth(100); // Marque
    TabledeDonnees.getColumnModel().getColumn(3).setPreferredWidth(100); // Modèle
    TabledeDonnees.getColumnModel().getColumn(4).setPreferredWidth(120); // Statut
    TabledeDonnees.getColumnModel().getColumn(5).setPreferredWidth(120); // Date Sortie
    TabledeDonnees.getColumnModel().getColumn(6).setPreferredWidth(120); // Date Retour
    TabledeDonnees.getColumnModel().getColumn(7).setPreferredWidth(150); // Date Reservation
    
    // Keep your existing StatusCellRenderer
    TabledeDonnees.setDefaultRenderer(Object.class, new StatusCellRenderer());
}
    private void initCustomComponents() {
        // Configure JDateChoosers (already added in NetBeans GUI builder)
        DateDebut.setDate(new Date()); // Today
        DateDebut.setDateFormatString("dd/MM/yyyy");
        Date_Prolongation.setDateFormatString("dd/MM/yyyy");
        
        // Set default dates
Calendar cal = Calendar.getInstance();
Date today = cal.getTime();

// Set default start date (today)
DateDebut.setDate(today);
DateDebut.setDateFormatString("dd/MM/yyyy");

// Set default return date (3 days from today)
cal.add(Calendar.DAY_OF_MONTH, 3);
DateRetour.setDate(cal.getTime());
DateRetour.setDateFormatString("dd/MM/yyyy");

// Set prolongation date to match return date initially
Date_Prolongation.setDate(cal.getTime());
Date_Prolongation.setDateFormatString("dd/MM/yyyy");

// Add listeners for automatic date updates
DateDebut.addPropertyChangeListener("date", evt -> {
    Date startDate = DateDebut.getDate();
    if (startDate != null) {
        // Calculate minimum return date (3 days after start)
        Calendar minCal = Calendar.getInstance();
        minCal.setTime(startDate);
        minCal.add(Calendar.DAY_OF_MONTH, 3);
        
        // Update return date if current is before minimum
        if (DateRetour.getDate() == null || DateRetour.getDate().before(minCal.getTime())) {
            DateRetour.setDate(minCal.getTime());
        }
        
        // Update prolongation date to match new return date
        Date_Prolongation.setDate(DateRetour.getDate());
    }
});
DateRetour.addPropertyChangeListener("date", evt -> {
    if (DateRetour.getDate() != null) {
        Date_Prolongation.setDate(DateRetour.getDate());
    }
});
        // Configure table
        TabledeDonnees.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        updateProlongationDateFromSelection();
    }
});
        TabledeDonnees.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());

        // Set button actions
        BTN_ESortie.addActionListener(e -> enregistrerSortie());
        BTN_ERetour.addActionListener(e -> enregistrerRetour());
        BTN_Prolonger.addActionListener(e -> prolongerLocation());
        BTN_Annuler.addActionListener(e -> annulerLocationOuReservation());

        // Load initial data
        refreshTableData();
    }
    private void updateProlongationDateFromSelection() {
    int selectedRow = TabledeDonnees.getSelectedRow();
    if (selectedRow >= 0) {
        // Get the return date from the table
        String dateRetourStr = (String) TabledeDonnees.getValueAt(selectedRow, 6);
        
        try {
            if (dateRetourStr != null && !dateRetourStr.isEmpty()) {
                // Parse the return date from table
                Date dateRetour = dateFormat.parse(dateRetourStr);
                // Set prolongation date to match
                Date_Prolongation.setDate(dateRetour);
            } else {
                // If no return date, set prolongation to 3 days after today
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 3);
                Date_Prolongation.setDate(cal.getTime());
            }
        } catch (ParseException ex) {
            // Handle parse error if needed
            Date_Prolongation.setDate(new Date());
        }
    }
}

  private void refreshTableData() {
    DefaultTableModel model = (DefaultTableModel) TabledeDonnees.getModel();
    model.setRowCount(0); // Clear existing data
    
    try {
        Date now = new Date();
        List<SortieVoiture> allSorties = sortieDAO.getAllSorties();
        
        for (Voiture voiture : voitureDAO.getAllVoitures()) {
            // Find current status
            String status = "Disponible";
            String dateSortie = "";
            String dateRetour = "";
            String dateReservation = "";
            
            // Check for active rentals (non-reservation, not canceled)
            Optional<SortieVoiture> activeRental = allSorties.stream()
                .filter(s -> s.getVoitureId() == voiture.getId())
                .filter(s -> !s.isReservation())
                .filter(s -> !s.isAnnule())
                .filter(s -> s.getDateSortie() != null && s.getDateSortie().before(now))
                .filter(s -> s.getDateRetour() == null || s.getDateRetour().after(now))
                .findFirst();
            
            if (activeRental.isPresent()) {
                status = "En sortie";
                dateSortie = dateFormat.format(activeRental.get().getDateSortie());
                dateRetour = activeRental.get().getDateRetour() != null ? 
                    dateFormat.format(activeRental.get().getDateRetour()) : "";
            }
            
            // Check for upcoming reservations
            List<SortieVoiture> reservations = allSorties.stream()
                .filter(s -> s.getVoitureId() == voiture.getId())
                .filter(SortieVoiture::isReservation)
                .filter(s -> !s.isAnnule())
                .filter(s -> s.getDateDebut() != null)
                .sorted(Comparator.comparing(SortieVoiture::getDateDebut))
                .collect(Collectors.toList());
            
            if (!reservations.isEmpty()) {
                if (status.equals("En sortie")) {
                    status += "/Réservé";
                } else {
                    status = "Réservé";
                }
                dateReservation = reservations.stream()
                    .map(r -> dateFormat.format(r.getDateDebut()) + "-" + dateFormat.format(r.getDateRetour()))
                    .collect(Collectors.joining(", "));
            }
            
            // Add row to table
            model.addRow(new Object[]{
                voiture.getId(),
                voiture.getPlaqueImmatriculation(),
                voiture.getMarque(),
                voiture.getModele(),
                status,
                dateSortie,
                dateRetour,
                dateReservation
            });
        }
    } catch (SQLException ex) {
        showError("Erreur de chargement: " + ex.getMessage());
    }
}

private String formatReservationDates(List<SortieVoiture> reservations) {
    return reservations.stream()
        .map(r -> dateFormat.format(r.getDateDebut()) + "-" + dateFormat.format(r.getDateRetour()))
        .collect(Collectors.joining(", "));
}
private void annulerLocationOuReservation() {
    try {
        int selectedRow = TabledeDonnees.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("Veuillez sélectionner une voiture", "Information");
            return;
        }

        int voitureId = (int) TabledeDonnees.getValueAt(selectedRow, 0);
        String status = (String) TabledeDonnees.getValueAt(selectedRow, 4);

        if (status.contains("En sortie") && status.contains("Réservé")) {
            // Case when both active rental and reservation exist
            annulerChoixMultiple(voitureId);
        } else if (status.startsWith("En sortie")) {
            annulerLocationActive(voitureId);
        } else if (status.startsWith("Réservé")) {
            annulerReservation(voitureId);
        } else {
            showMessage("Cette voiture n'est pas en location ou réservée", "Information");
        }
    } catch (SQLException e) {
        showError("Erreur de base de données: " + e.getMessage());
    }
}
private void annulerChoixMultiple(int voitureId) {
    try {
        // Get active rental
        Optional<SortieVoiture> activeRental = sortieDAO.getSortiesActives().stream()
            .filter(s -> s.getVoitureId() == voitureId)
            .findFirst();

        // Get future reservations
        List<SortieVoiture> reservations = sortieDAO.getReservationsByVoiture(voitureId).stream()
            .filter(r -> r.getDateDebut().after(new Date()))
            .collect(Collectors.toList());

        // Create options
        List<String> options = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();

        // Add rental option
        activeRental.ifPresent(rental -> {
            options.add("Annuler la location en cours (Client: " + rental.getClient() + ")");
            actions.add(createCancelAction(rental, false));
        });

        // Add reservation options
        reservations.forEach(res -> {
            options.add("Annuler la réservation future (" + 
                       dateFormat.format(res.getDateDebut()) + " - " + 
                       dateFormat.format(res.getDateRetour()) + ")");
            actions.add(createCancelAction(res, true));
        });

        // Show dialog
        String selected = (String) JOptionPane.showInputDialog(
            this,
            "Cette voiture a à la fois une location active et des réservations futures.\nQue souhaitez-vous annuler ?",
            "Choix d'annulation",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options.toArray(),
            options.isEmpty() ? null : options.get(0));

        // Execute action
        if (selected != null) {
            int index = options.indexOf(selected);
            actions.get(index).run();
        }

    } catch (SQLException e) {
        showError("Erreur de base de données: " + e.getMessage());
    }
}

private Runnable createCancelAction(SortieVoiture record, boolean isReservation) {
    return () -> {
        try {
            boolean success = isReservation 
                ? sortieDAO.annulerReservation(record.getId())
                : sortieDAO.annulerLocation(record.getId());
            
            if (success) {
                showMessage((isReservation ? "Réservation" : "Location") + 
                          " annulée avec succès", "Succès");
                refreshTableData();
            }
        } catch (SQLException ex) {
            showError("Erreur lors de l'annulation: " + ex.getMessage());
        }
    };
}
private void annulerLocationActive(int voitureId) throws SQLException {
    Optional<SortieVoiture> activeRental = sortieDAO.getSortiesActives().stream()
        .filter(s -> s.getVoitureId() == voitureId)
        .findFirst();

    if (!activeRental.isPresent()) {
        showMessage("Aucune location active trouvée", "Erreur");
        return;
    }

    SortieVoiture rental = activeRental.get();
    
    String message;
    if (rental.getDateSortie() == null) {
        message = "Confirmez-vous l'annulation de la réservation?\n\n" +
                 "Le client n'a pas encore pris la voiture.\n" +
                 "Client: " + rental.getClient();
    } else {
        message = "Confirmez-vous l'annulation de la location en cours?\n\n" +
                 "Client: " + rental.getClient() + "\n" +
                 "Voiture: " + rental.getPlaque() + "\n" +
                 "Depuis le: " + dateFormat.format(rental.getDateSortie());
    }

    int confirm = JOptionPane.showConfirmDialog(this,
        message,
        "Confirmation d'annulation",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);

    if (confirm == JOptionPane.YES_OPTION) {
        if (sortieDAO.annulerLocation(rental.getId())) {
            showMessage(rental.getDateSortie() == null 
                ? "Réservation annulée - voiture disponible" 
                : "Location annulée - voiture disponible", 
                "Succès");
            refreshTableData();
        }
    }
}
private void annulerReservation(int voitureId) throws SQLException {
    List<SortieVoiture> reservations = sortieDAO.getReservationsByVoiture(voitureId).stream()
        .filter(r -> r.getDateDebut().after(new Date()))
        .sorted(Comparator.comparing(SortieVoiture::getDateDebut))
        .collect(Collectors.toList());

    if (reservations.isEmpty()) {
        showMessage("Aucune réservation trouvée", "Information");
        return;
    }

    if (reservations.size() == 1) {
        // Single reservation case
        SortieVoiture reservation = reservations.get(0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirmez-vous l'annulation de la réservation?\n\n" +
            "Client: " + reservation.getClient() + "\n" +
            "Période: " + dateFormat.format(reservation.getDateDebut()) + " - " + 
                         dateFormat.format(reservation.getDateRetour()),
            "Confirmation d'annulation",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (sortieDAO.annulerReservation(reservation.getId())) {
                refreshTableData();
                showMessage("Réservation annulée avec succès", "Succès");
            }
        }
    } else {
        // Multiple reservations case
        String[] options = reservations.stream()
            .map(r -> "Client: " + r.getClient() + " (" + 
                      dateFormat.format(r.getDateDebut()) + " - " + 
                      dateFormat.format(r.getDateRetour()) + ")")
            .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(this,
            "Cette voiture a plusieurs réservations.\nVeuillez sélectionner celle à annuler:",
            "Annulation de réservation",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (selected != null) {
            int index = Arrays.asList(options).indexOf(selected);
            SortieVoiture toCancel = reservations.get(index);
            
            if (sortieDAO.annulerReservation(toCancel.getId())) {
                refreshTableData();
                showMessage("Réservation annulée avec succès", "Succès");
            }
        }
    }
}
private void showMessage(String message, String title) {
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

        BTN_TOUTE3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TabledeDonnees = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        NomClt = new javax.swing.JTextField();
        Avance = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        DateDebut = new com.toedter.calendar.JDateChooser();
        jLabel5 = new javax.swing.JLabel();
        DateRetour = new com.toedter.calendar.JDateChooser();
        BTN_ESortie = new javax.swing.JButton();
        BTN_ERetour = new javax.swing.JButton();
        BTN_Retour = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        Date_Prolongation = new com.toedter.calendar.JDateChooser();
        BTN_Prolonger = new javax.swing.JToggleButton();
        BTN_Annuler = new javax.swing.JButton();

        BTN_TOUTE3.setBackground(new java.awt.Color(204, 204, 204));
        BTN_TOUTE3.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        BTN_TOUTE3.setText("Modifier Location");
        BTN_TOUTE3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_TOUTE3ActionPerformed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(51, 153, 255));

        jLabel1.setBackground(new java.awt.Color(51, 153, 255));
        jLabel1.setFont(new java.awt.Font("Arial Black", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("GESTION SORTIE DES VOITURES");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(126, 126, 126))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jLabel1)
                .addContainerGap(52, Short.MAX_VALUE))
        );

        TabledeDonnees.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Plaque", "Marque", "Modéle", "Statut", "Date Sortie", "Date Retour", "Date Reservation"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(TabledeDonnees);

        jLabel2.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel2.setText("Nom de Client");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel3.setText("Avance (MAD)");

        NomClt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NomCltActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel4.setText("Date Debut");

        jLabel5.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel5.setText("Date Retour ");

        BTN_ESortie.setBackground(new java.awt.Color(204, 204, 204));
        BTN_ESortie.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        BTN_ESortie.setText("Enregistrer ");
        BTN_ESortie.setActionCommand("Enregistrer");
        BTN_ESortie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_ESortieActionPerformed(evt);
            }
        });

        BTN_ERetour.setBackground(new java.awt.Color(204, 204, 204));
        BTN_ERetour.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        BTN_ERetour.setText("Enregistrer Retour");
        BTN_ERetour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_ERetourActionPerformed(evt);
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
        jLabel6.setText("Date Prolongation");

        BTN_Prolonger.setBackground(new java.awt.Color(204, 204, 204));
        BTN_Prolonger.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        BTN_Prolonger.setText("Prolonger");
        BTN_Prolonger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_ProlongerActionPerformed(evt);
            }
        });

        BTN_Annuler.setBackground(new java.awt.Color(204, 204, 204));
        BTN_Annuler.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        BTN_Annuler.setText("Annuler ");
        BTN_Annuler.setActionCommand("Enregistrer");
        BTN_Annuler.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTN_AnnulerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 69, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel4)
                                    .addGap(39, 39, 39)
                                    .addComponent(DateDebut, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel6)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(Date_Prolongation, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(12, 12, 12)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(NomClt, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(52, 52, 52)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel3))
                                .addGap(20, 20, 20)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(DateRetour, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(Avance, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(BTN_Prolonger, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)))
                        .addGap(127, 127, 127))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addComponent(BTN_ESortie, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(BTN_ERetour)
                .addGap(18, 18, 18)
                .addComponent(BTN_Annuler, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(BTN_Retour, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(NomClt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Avance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(DateRetour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DateDebut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(BTN_Prolonger)
                    .addComponent(Date_Prolongation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_ESortie)
                    .addComponent(BTN_ERetour)
                    .addComponent(BTN_Annuler)
                    .addComponent(BTN_Retour))
                .addContainerGap(41, Short.MAX_VALUE))
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

    private void NomCltActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NomCltActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NomCltActionPerformed

    private void BTN_TOUTE3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_TOUTE3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BTN_TOUTE3ActionPerformed

    private void BTN_ESortieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_ESortieActionPerformed

    }//GEN-LAST:event_BTN_ESortieActionPerformed

    private void BTN_ERetourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_ERetourActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BTN_ERetourActionPerformed

    private void BTN_RetourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_RetourActionPerformed
        // TODO add your handling code here:
        this.dispose(); // Close current window
        new MainMenuFrame().setVisible(true); 
    }//GEN-LAST:event_BTN_RetourActionPerformed

    private void BTN_ProlongerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_ProlongerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BTN_ProlongerActionPerformed

    private void BTN_AnnulerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTN_AnnulerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BTN_AnnulerActionPerformed
    private void debugTableStructure() {
    System.out.println("=== TABLE STRUCTURE ===");
    System.out.println("Column count: " + TabledeDonnees.getColumnCount());
    for (int i = 0; i < TabledeDonnees.getColumnCount(); i++) {
        System.out.println(i + ": " + TabledeDonnees.getColumnName(i));
    }
}
    private void prolongerLocation() {
    try {
        // 1. Verify selection
        int selectedRow = TabledeDonnees.getSelectedRow();
        if (selectedRow == -1) {
            showError("Veuillez sélectionner une voiture");
            return;
        }

        // 2. Get the prolongation date
        Date nouvelleDate = Date_Prolongation.getDate();
        if (nouvelleDate == null) {
            showError("Veuillez sélectionner une date valide");
            return;
        }

        // 3. Get current return date
        Date currentReturnDate = DateRetour.getDate();
        if (nouvelleDate.equals(currentReturnDate)) {
            showError("La date de prolongation doit être après la date de retour actuelle");
            return;
        }

        // 4. Get current rental info
        int voitureId = (int) TabledeDonnees.getValueAt(selectedRow, 0);
        Optional<SortieVoiture> activeRental = sortieDAO.getSortiesActives().stream()
            .filter(s -> s.getVoitureId() == voitureId)
            .findFirst();

        if (!activeRental.isPresent()) {
            showError("Aucune location active trouvée");
            return;
        }

        // 5. Perform prolongation
        SortieVoiture rental = activeRental.get();
        if (sortieDAO.prolongerLocation(rental.getId(), nouvelleDate)) {
            // Update UI
            DateRetour.setDate(nouvelleDate); // Update return date picker
            TabledeDonnees.setValueAt(dateFormat.format(nouvelleDate), selectedRow, 6); // Update table
            
            showSuccess("Location prolongée jusqu'au " + dateFormat.format(nouvelleDate));
        } else {
            showError("Échec de la prolongation");
        }
    } catch (SQLException e) {
        showError("Erreur de base de données: " + e.getMessage());
    }
}
    private void enregistrerSortie() {
    try {
        // 1. Validate selection
        int selectedRow = TabledeDonnees.getSelectedRow();
        if (selectedRow == -1) {
            showError("Veuillez sélectionner une voiture");
            return;
        }

        // 2. Get vehicle info
        int voitureId = (int) TabledeDonnees.getValueAt(selectedRow, 0);
        String plaque = TabledeDonnees.getValueAt(selectedRow, 1).toString();
        String currentStatus = (String) TabledeDonnees.getValueAt(selectedRow, 4);

        // 3. Validate client info
        if (NomClt.getText().trim().isEmpty()) {
            showError("Le nom du client est requis");
            NomClt.requestFocus();
            return;
        }

        // 4. Validate payment
        double avance;
        try {
            avance = Double.parseDouble(Avance.getText());
            if (avance < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showError("Montant d'avance invalide");
            Avance.requestFocus();
            return;
        }

        // 5. Validate dates
        Date dateDebut = DateDebut.getDate();
        Date dateRetour = DateRetour.getDate();
        Date now = new Date();
        Calendar minCal = Calendar.getInstance();
        minCal.setTime(dateDebut);
        minCal.add(Calendar.DAY_OF_MONTH, 3);
        Date minReturnDate = minCal.getTime();
        if (dateRetour.before(minReturnDate)) {
            showError("La durée minimum de location est 3 jours");
            return;
        }
        if (dateDebut == null || dateRetour == null) {
            showError("Veuillez sélectionner des dates valides");
            return;
        }

        if (dateRetour.before(dateDebut)) {
            showError("La date de retour doit être après la date de début");
            return;
        }

        // 6. Determine rental type
        boolean isReservation = dateDebut.after(now);

        // 7. Check availability
        if (!isReservation) {
            // For immediate rentals
            if (voitureDAO.hasActiveRental(voitureId, dateDebut, dateRetour)) {
                showError("La voiture est déjà en location pendant cette période");
                return;
            }
            
            if (voitureDAO.hasOverlappingReservation(voitureId, dateDebut, dateRetour)) {
                showError("La voiture est réservée pendant cette période");
                return;
            }
        } else {
            // For reservations
            if (!voitureDAO.isAvailableForReservation(voitureId, dateDebut, dateRetour)) {
                showError("La voiture n'est pas disponible pour cette période");
                return;
            }
        }

        // 8. Create rental/reservation record
        SortieVoiture sortie = new SortieVoiture();
        sortie.setVoitureId(voitureId);
        sortie.setPlaque(plaque);
        sortie.setClient(NomClt.getText().trim());
        sortie.setAvance(avance);
        sortie.setDateDebut(dateDebut);
        sortie.setDateRetour(dateRetour);
        sortie.setReservation(isReservation);
        sortie.setProlongee(false);

        // Set actual start date for immediate rentals
        if (!isReservation) {
            sortie.setDateSortie(now);
        }

        // 9. Save to database
        if (sortieDAO.enregistrerSortie(sortie)) {
            // Update UI
            DefaultTableModel model = (DefaultTableModel) TabledeDonnees.getModel();
            
            if (isReservation) {
                // For reservations
                model.setValueAt("Réservé", selectedRow, 4);
                model.setValueAt("", selectedRow, 5); // Clear Date Sortie
                model.setValueAt("", selectedRow, 6); // Clear Date Retour
                model.setValueAt(
                    dateFormat.format(dateDebut) + "-" + dateFormat.format(dateRetour), 
                    selectedRow, 7
                );
            } else {
                // For immediate rentals
                model.setValueAt("En sortie", selectedRow, 4);
                model.setValueAt(dateFormat.format(now), selectedRow, 5);
                model.setValueAt(dateFormat.format(dateRetour), selectedRow, 6);
                model.setValueAt("", selectedRow, 7); // Clear Date Reservation
            }
            
            showSuccess(isReservation ? 
                "Réservation enregistrée" : 
                "Sortie enregistrée - Retour prévu le " + dateFormat.format(dateRetour));
            clearForm();
        } else {
            showError("Échec de l'enregistrement");
        }
        
        DefaultTableModel model = (DefaultTableModel) TabledeDonnees.getModel();
        selectedRow = TabledeDonnees.getSelectedRow();
        
        if (isReservation) {
            // For new reservation
            model.setValueAt("Réservé", selectedRow, 4);
            model.setValueAt("", selectedRow, 5); // Clear Date Sortie
            model.setValueAt("", selectedRow, 6); // Clear Date Retour
            model.setValueAt(dateFormat.format(dateDebut), selectedRow, 7); // Only show start date
        } else {
            // For immediate rental
            model.setValueAt("En sortie", selectedRow, 4);
            model.setValueAt(dateFormat.format(new Date()), selectedRow, 5); // Actual start date
            model.setValueAt(dateFormat.format(dateRetour), selectedRow, 6); // Planned return
            model.setValueAt("", selectedRow, 7); // Clear reservation date
        }
        
        showSuccess(isReservation ? 
            "Réservation enregistrée" : 
            "Sortie enregistrée - Retour prévu le " + dateFormat.format(dateRetour));
        refreshTableData(); // Refresh to show any status changes
    } catch (SQLException e) {
        showError("Erreur de base de données: " + e.getMessage());
    } catch (Exception e) {
        showError("Erreur inattendue: " + e.getMessage());
    }
}

   private void enregistrerRetour() {
    try {
        // 1. Verify selection
        int selectedRow = TabledeDonnees.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner une voiture à retourner",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 2. Get vehicle info
        int voitureId = (int) TabledeDonnees.getValueAt(selectedRow, 0);
        String currentStatus = (String) TabledeDonnees.getValueAt(selectedRow, 4);

        // 3. Check if vehicle is rented
        if (!currentStatus.startsWith("En sortie")) {
            JOptionPane.showMessageDialog(this,
                "Cette voiture n'est pas actuellement en location",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4. Find active rental
        Optional<SortieVoiture> activeRental = sortieDAO.getSortiesActives().stream()
            .filter(s -> s.getVoitureId() == voitureId && !s.isReservation())
            .findFirst();

        if (!activeRental.isPresent()) {
            JOptionPane.showMessageDialog(this,
                "Aucune location active trouvée pour cette voiture",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 5. Process return (using current date as return date)
        if (sortieDAO.enregistrerRetour(activeRental.get().getId())) {
            // Update UI
            DefaultTableModel model = (DefaultTableModel) TabledeDonnees.getModel();
            
            // Check for future reservations
            List<SortieVoiture> reservations = sortieDAO.getReservationsByVoiture(voitureId).stream()
                .filter(r -> r.getDateDebut().after(new Date()))
                .collect(Collectors.toList());
            
            if (!reservations.isEmpty()) {
                model.setValueAt("Réservé", selectedRow, 4);
                model.setValueAt("", selectedRow, 5); // Clear Date Sortie
                model.setValueAt("", selectedRow, 6); // Clear Date Retour
                model.setValueAt(
                    reservations.stream()
                        .map(r -> dateFormat.format(r.getDateDebut()))
                        .collect(Collectors.joining(", ")),
                    selectedRow, 7
                );
            } else {
                model.setValueAt("Disponible", selectedRow, 4);
                model.setValueAt("", selectedRow, 5); // Clear Date Sortie
                model.setValueAt("", selectedRow, 6); // Clear Date Retour
                model.setValueAt("", selectedRow, 7); // Clear Date Reservation
            }
            
            JOptionPane.showMessageDialog(this,
                "Retour enregistré avec succès",
                "Succès",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Échec de l'enregistrement du retour",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Erreur de base de données: " + e.getMessage(),
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
    }
}

     private void clearForm() {
        NomClt.setText("");
        Avance.setText("0");
        DateDebut.setDate(new Date());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        DateRetour.setDate(cal.getTime());
    }
     private void handleError(Exception e) {
    if (e instanceof ParseException) {
        showError("Format de date invalide (JJ/MM/AAAA)");
    } else if (e instanceof NumberFormatException) {
        showError("Montant d'avance invalide");
    } else if (e instanceof SQLException) {
        showError("Erreur BD: " + e.getMessage());
    } else {
        showError("Erreur: " + e.getMessage());
    }
}


    // Helper methods
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (isSelected) return c;
        
        String status = (String) table.getValueAt(row, 4);
        
        if (status.contains("/")) {
            if (status.equals("Réservé/Réservé")) {
                c.setBackground(new Color(220, 230, 255)); // Light blue
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            } else {
                // Combined status
                c.setBackground(new Color(200, 230, 255)); // Light blue
            }
        }else if (status.contains("Annulé")) {
    c.setBackground(new Color(255, 220, 220)); // Light red
    c.setForeground(Color.GRAY);
        } else if (status.equals("En sortie")) {
            c.setBackground(new Color(255, 200, 200)); // Light red
        } else if (status.equals("Réservé")) {
            c.setBackground(new Color(200, 230, 255)); // Light blue
        } else {
            c.setBackground(Color.WHITE);
        }
        
        return c;
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
            java.util.logging.Logger.getLogger(SortieVoitureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SortieVoitureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SortieVoitureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SortieVoitureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SortieVoitureFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField Avance;
    private javax.swing.JButton BTN_Annuler;
    private javax.swing.JButton BTN_ERetour;
    private javax.swing.JButton BTN_ESortie;
    private javax.swing.JToggleButton BTN_Prolonger;
    private javax.swing.JButton BTN_Retour;
    private javax.swing.JButton BTN_TOUTE3;
    private com.toedter.calendar.JDateChooser DateDebut;
    private com.toedter.calendar.JDateChooser DateRetour;
    private com.toedter.calendar.JDateChooser Date_Prolongation;
    private javax.swing.JTextField NomClt;
    private javax.swing.JTable TabledeDonnees;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
