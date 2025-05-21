/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

/**
 *
 * @author samsung
 */
// Core PDFBox classes
import java.awt.Color;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.awt.Desktop;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import models.Client;
import models.Contrat;
import models.SortieVoiture;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.util.List;
import java.time.LocalDate;
import java.sql.*;
import java.text.SimpleDateFormat;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class PDFGenerator {
    private static final float COLUMN_GAP = 20f; // Space between columns
private static final float CONDITION_MARGIN = 50f; // Margin for conditions page
   public File generateContractPDF(Contrat contrat, Client client, SortieVoiture reservation) throws IOException {
    if (contrat == null || client == null || reservation == null) {
        throw new IllegalArgumentException("Données du contrat incomplètes");
    }
    try (PDDocument document = new PDDocument()) {
        // Initialize fonts and sizes
        PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font textFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        final float mainTitleSize = 14;
        final float sectionTitleSize = 12;
        final float textSize = 10;
        final float lineHeight = 12;
        final float margin = 50;
        final float paragraphSpacing = 20;
        
        final float conditionTitleSize = 11;  // Article titles
        final float conditionTextSize = 9;    // Article text
        final float conditionLineHeight = 10; // Tight spacing for conditions

        // ============ PAGE 1 ============
        PDPage page1 = new PDPage(PDRectangle.A4);
        document.addPage(page1);
        
        try (PDPageContentStream content = new PDPageContentStream(document, page1)) {
            float pageWidth = page1.getMediaBox().getWidth();
            float y = page1.getMediaBox().getHeight() - margin;

            // 1. Header with 3-column layout
            drawText(content, "Sté. JMCARS s.a.r.l", margin, y, titleFont, mainTitleSize);
            drawText(content, "Location de Voitures - RentCar", margin, y - lineHeight, textFont, textSize);
            
            // Center: Logo
            try {
                InputStream logoStream = getClass().getResourceAsStream("/resources/jm logo black (2).png");
                if (logoStream != null) {
                    PDImageXObject logo = PDImageXObject.createFromByteArray(document, 
                        logoStream.readAllBytes(), "logo");
                    float logoWidth = 70;
                    float logoHeight = 64;
                    float logoX = (pageWidth - logoWidth) / 2;
                    float logoY = y - logoHeight + 20;
                    content.drawImage(logo, logoX, logoY, logoWidth, logoHeight);
                }
            } catch (IOException e) {
                System.err.println("Could not load company logo");
            }
            
            // Right: Contact info (vertical layout)
            float contactX = pageWidth - margin;
            String[] contactInfo = {
                "N° 14 Avenue Omar Benjelloune",
                "Riad Salam, Agadir",
                "Tél: 06 61 37 27 78",
                "Email: contact@jmcars-location.com"
            };
            for (String line : contactInfo) {
                float textWidth = textFont.getStringWidth(line) / 1000 * textSize;
                drawText(content, line, contactX - textWidth, y, textFont, textSize);
                y -= lineHeight;
            }
            y = page1.getMediaBox().getHeight() - margin - (contactInfo.length * lineHeight) - lineHeight * 2;

            // 2. Main Title
            drawCenteredText(content, "CONTRAT DE LOCATION", pageWidth, y, titleFont, mainTitleSize);
            y -= lineHeight * 2;

            // 3. Vehicle Info Section
    y = drawSectionTitle(content, "VÉHICULE ET RÉSERVATION", margin, y, titleFont, sectionTitleSize, lineHeight);
    y -= lineHeight;

    // Vehicle info in a 2-column layout
    float column1X = margin;
    float column2X = margin + 200;

    // Left column - Vehicle details
    drawText(content, "Marque: " + reservation.getMarque(), column1X, y, textFont, textSize);
    drawText(content, "Modèle: " + reservation.getModele(), column1X, y - lineHeight, textFont, textSize);
    drawText(content, "Immatriculation: " + reservation.getImmatriculation(), column1X, y - lineHeight * 2, textFont, textSize);

    // Right column - Rental dates
    drawText(content, "Durée: " + reservation.getDureeEnJours(), column2X, y, textFont, textSize);
    Date dateSortie = "Réservé".equals(reservation.getStatut()) ? 
                    reservation.getDateReservation() : 
                    reservation.getDateSortie() != null ? reservation.getDateSortie() : new Date();
    drawText(content, "Date de sortie: " + formatDate(dateSortie), column2X, y - lineHeight, textFont, textSize);
    drawText(content, "Date de retour: " + formatDate(reservation.getDateRetour()), column2X, y - lineHeight * 2, textFont, textSize);

    // Permis image (smaller and better positioned)
    if (client.getPermisImageData() != null) {
        PDImageXObject permisImage = PDImageXObject.createFromByteArray(document, client.getPermisImageData(), "permis");
        float permisWidth = 140;  // Reduced from 120
        float permisHeight = 75;   // Reduced from 90
        float permisImageX = pageWidth - margin - permisWidth;
        content.drawImage(permisImage, permisImageX, y - permisHeight, permisWidth, permisHeight);
        drawText(content, "Permis de conduire", permisImageX, y - permisHeight - 10, textFont, 8);
    }

    y -= lineHeight * 3 + 20;

    // 4. Client Info Section
    y = drawSectionTitle(content, "INFORMATIONS CLIENT", margin, y, titleFont, sectionTitleSize, lineHeight);
    y -= lineHeight;

    // Client info in a 2-column layout with CIN image
    float clientColX = margin;
    float initialClientY = y;

    // Left column - Client details
    drawText(content, "Nom: " + client.getNom(), clientColX, y, textFont, textSize);
    drawText(content, "Prénom: " + client.getPrenom(), clientColX, y - lineHeight, textFont, textSize);
    drawText(content, "CIN/Passeport: " + client.getCin(), clientColX, y - lineHeight * 2, textFont, textSize);
    drawText(content, "Date expiration: " + formatDate(client.getCinExpiration()), clientColX, y - lineHeight * 3, textFont, textSize);
    drawText(content, "Permis: " + client.getPermis(), clientColX, y - lineHeight * 4, textFont, textSize);
    drawText(content, "Date expiration: " + formatDate(client.getPermisExpiration()), clientColX, y - lineHeight * 5, textFont, textSize);
    drawText(content, "Téléphone: " + client.getTelephone(), clientColX, y - lineHeight * 6, textFont, textSize);
    drawText(content, "Adresse: " + client.getAdresse(), clientColX, y - lineHeight * 7, textFont, textSize);

    // Second driver (middle column)
    if (contrat.getSecondDriver() != null) {
        Client secondDriver = contrat.getSecondDriver();
        float secondDriverX = clientColX + 200;
        drawText(content, "DEUXIÈME CONDUCTEUR", secondDriverX, initialClientY, titleFont, sectionTitleSize - 1);
        drawText(content, "Nom: " + secondDriver.getNom(), secondDriverX, initialClientY - lineHeight * 1.5f, textFont, textSize);
        drawText(content, "Prénom: " + secondDriver.getPrenom(), secondDriverX, initialClientY - lineHeight * 2.5f, textFont, textSize);
        drawText(content, "CIN/Passeport: " + secondDriver.getCin(), secondDriverX, initialClientY - lineHeight * 3.5f, textFont, textSize);
        drawText(content, "Permis: " + secondDriver.getPermis(), secondDriverX, initialClientY - lineHeight * 4.5f, textFont, textSize);
        drawText(content, "Téléphone: " + secondDriver.getTelephone(), secondDriverX, initialClientY - lineHeight * 5.5f, textFont, textSize);
        drawText(content, "Adresse: " + secondDriver.getAdresse(), secondDriverX, initialClientY - lineHeight * 6.5f, textFont, textSize);
    }

    // CIN Image (right column, smaller)
    if (client.getCinImageData() != null) {
        PDImageXObject cinImage = PDImageXObject.createFromByteArray(document, client.getCinImageData(), "cin");
        float cinWidth = 140;   // Reduced from 120
        float cinHeight = 75;   // Reduced from 90
        float cinImageX = pageWidth - margin - cinWidth;
        content.drawImage(cinImage, cinImageX, initialClientY - cinHeight, cinWidth, cinHeight);
        drawText(content, "CIN/Passeport", cinImageX, initialClientY - cinHeight - 10, textFont, 8);
    }

    y = initialClientY - lineHeight * 8 - 10;

            // 5. Vehicle Condition Section
            y = drawSectionTitle(content, "ÉTAT DU VÉHICULE", margin, y, titleFont, sectionTitleSize, lineHeight);
            y -= paragraphSpacing;

            float imageWidth = 180;
            float imageHeight = 120;
            float imageX = pageWidth - margin - imageWidth;
            float colWidth = (pageWidth - 2 * margin) / 2;
            String[][] vehicleCondition = {
                {"[ ] Carrosserie", "[ ] Pneus"},
                {"[ ] Sièges", "[ ] Autres: " + (contrat.getAutresRemarques() != null ? contrat.getAutresRemarques() : "")}
            };

            if (contrat.isCarrosserieOk()) vehicleCondition[0][0] = "[X] Carrosserie";
            if (contrat.isPneusOk()) vehicleCondition[0][1] = "[X] Pneus";
            if (contrat.isSiegesOk()) vehicleCondition[1][0] = "[X] Sièges";

            float checkboxY = y;
            for (String[] row : vehicleCondition) {
                drawText(content, row[0], margin, checkboxY, textFont, textSize);
                drawText(content, row[1], margin + 150, checkboxY, textFont, textSize);
                checkboxY -= lineHeight;
            }

            try {
                InputStream imageStream = getClass().getResourceAsStream("/resources/car_inspection.png");
                if (imageStream != null) {
                    PDImageXObject image = PDImageXObject.createFromByteArray(document, imageStream.readAllBytes(), "car_inspection");
                    float actualY = Math.max(y - imageHeight, margin);
                    content.drawImage(image, imageX, actualY, imageWidth, imageHeight);
                }
            } catch (IOException e) {
                content.setNonStrokingColor(Color.LIGHT_GRAY);
                content.addRect(imageX, y - imageHeight, imageWidth, imageHeight);
                content.fill();
            }
            y -= paragraphSpacing;
            y -= vehicleCondition.length * lineHeight;

            // 6. Selected Options Section
            y = drawSectionTitle(content, "OPTIONS SÉLECTIONNÉES", margin, y, titleFont, sectionTitleSize, lineHeight);
            y -= paragraphSpacing;
            String[][] options = {
                {"[ ] Roue de secours", "[ ] Cric"},
                {"[ ] Siège bébé", "[ ] Clé de roue"}
            };
            if (contrat.isRoueSecours()) options[0][0] = "[X] Roue de secours";
            if (contrat.isCric()) options[0][1] = "[X] Cric";
            if (contrat.isSiegeBebe()) options[1][0] = "[X] Siège bébé";
            if (contrat.isCleRoue()) options[1][1] = "[X] Clé de roue";

            for (String[] row : options) {
                drawText(content, row[0], margin, y, textFont, textSize);
                drawText(content, row[1], margin + 150, y, textFont, textSize);
                y -= lineHeight;
            }
            y -= lineHeight;

            // 7. Pricing Section
            y = drawSectionTitle(content, "TARIFS ET PAIEMENT", margin, y, titleFont, sectionTitleSize, lineHeight);
            y -= paragraphSpacing;
            String[][] pricingInfo = {
                {"Prix journalier:", String.format("%.2f DH", contrat.getPrixJournalier())},
                {"Durée:", contrat.getDuree() + " jours"},
                {"Total:", String.format("%.2f DH", contrat.getTotal())},
                {"Avance:", String.format("%.2f DH", contrat.getAvance())},
                {"Reste à payer:", String.format("%.2f DH", contrat.getResteAPayer())},
                {"Mode de paiement:", contrat.getModePaiement()}
            };
            y = drawTable(content, margin, y, pricingInfo, textFont, textSize, lineHeight, 120);
            y -= lineHeight;

            // 8. Signatures Section
            y = drawSectionTitle(content, "SIGNATURES", margin, y, titleFont, sectionTitleSize, lineHeight);
            y -= paragraphSpacing;
            drawText(content, "Le Locataire", margin + 50, y, textFont, textSize);
            drawText(content, "Le Loueur", margin + 300, y, textFont, textSize);
            y -= lineHeight * 3;
            content.setLineWidth(1f);
            content.moveTo(margin + 50, y);
            content.lineTo(margin + 200, y);
            content.stroke();
            content.moveTo(margin + 300, y);
            content.lineTo(margin + 450, y);
            content.stroke();
            y -= lineHeight * 2;

            // 9. Observations
            y = drawSectionTitle(content, "OBSERVATIONS", margin, y, titleFont, sectionTitleSize, lineHeight);
            y -= paragraphSpacing;
            String observations = "Je reconnais avoir pris connaissance des conditions générales au verso de ce contrat et m'engage à les respecter.";
            for (String line : wrapText(observations, pageWidth - 2 * margin, textFont, textSize)) {
                drawText(content, line, margin, y, textFont, textSize);
                y -= lineHeight;
            }
        }

        // ============ PAGE 2 (Conditions) ============
        PDPage page2 = new PDPage(PDRectangle.A4);
        document.addPage(page2);
        
        try (PDPageContentStream content = new PDPageContentStream(document, page2)) {
            float y = page2.getMediaBox().getHeight() - margin;
            float columnWidth = (page2.getMediaBox().getWidth() - 3 * margin) / 2;
            
            drawCenteredText(content, "CONDITIONS GENERALES", page1.getMediaBox().getWidth(), y, titleFont, 14);
            y -= lineHeight * 2;
            y -= paragraphSpacing;
            
            float leftX = margin;
            float rightX = margin * 2 + columnWidth;
            float leftY = y;
            leftY = drawConditionArticle(content, "Article 1 - UTILISATION DE VOITURES", getArticle1Text(), leftX, leftY, columnWidth, titleFont, textFont, conditionTitleSize, conditionTextSize, conditionLineHeight);
            leftY = drawConditionArticle(content, "Article 2 - ÉTAT DE VOITURE", getArticle2Text(), leftX, leftY, columnWidth, titleFont, textFont, conditionTitleSize, conditionTextSize, conditionLineHeight);
            leftY = drawConditionArticle(content, "Article 3 - ESSENCE ET HUILES", getArticle3Text(), leftX, leftY, columnWidth, titleFont, textFont, conditionTitleSize, conditionTextSize, conditionLineHeight);
            leftY = drawConditionArticle(content, "Article 4 - ENTRETIEN ET RÉPARATIONS", getArticle4Text(), leftX, leftY, columnWidth, titleFont, textFont, conditionTitleSize, conditionTextSize, conditionLineHeight);

            float rightY = y;
            rightY = drawFlowingConditionArticle(content, "Article 5 - ASSURANCES", getArticle5Text(), rightX, rightY, columnWidth, titleFont, textFont, conditionTitleSize, conditionTextSize, conditionLineHeight, page2);
            rightY = drawConditionArticle(content, "Article 6 - LOCATION", getArticle6Text(), rightX, rightY, columnWidth, titleFont, textFont, conditionTitleSize, conditionTextSize, conditionLineHeight);
            rightY = drawConditionArticle(content, "Article 7 - RAPATRIEMENT DE VOITURE", getArticle7Text(), rightX, rightY, columnWidth, titleFont, textFont, conditionTitleSize, conditionTextSize, conditionLineHeight);
            rightY = drawConditionArticle(content, "Article 8 - PAPIERS DE VOITURE", getArticle8Text(), rightX, rightY, columnWidth, titleFont, textFont, conditionTitleSize, conditionTextSize, conditionLineHeight);
            rightY = drawConditionArticle(content, "Article 9 - RESPONSABILITÉ", getArticle9Text(), rightX, rightY, columnWidth, titleFont, textFont, conditionTitleSize, conditionTextSize, conditionLineHeight);
            rightY = drawConditionArticle(content, "Article 10 - COMPÉTENCE", getArticle10Text(), rightX, rightY, columnWidth, titleFont, textFont, conditionTitleSize, conditionTextSize, conditionLineHeight);
            rightY = drawConditionArticle(content, "Article 11 - DÉLAI ET CONTRAVENTIONS", getArticle11Text(), rightX, rightY, columnWidth, titleFont, textFont, conditionTitleSize, conditionTextSize, conditionLineHeight);
        }

        // Save PDF
        String fileName = "Contrat_JMCARS_" + client.getNom() + "_" + 
                         LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf";
        File pdfFile = new File(fileName);
        document.save(pdfFile);
        return pdfFile;
    }
}
   
private float drawSectionTitle(PDPageContentStream content, String title, 
                             float x, float y, PDFont font, float size, float lineHeight) throws IOException {
    y -= lineHeight * 0.5f; // Space before title
    drawText(content, title, x, y, font, size);
    return y - lineHeight; // Space after title
}

// Helper methods
private float drawConditionArticle(PDPageContentStream content, String title, String text,
                                 float x, float y, float width,
                                 PDFont titleFont, PDFont textFont,
                                 float titleSize, float textSize,
                                 float lineHeight) throws IOException {
    // Draw title
    drawText(content, title, x, y, titleFont, titleSize);
    y -= lineHeight;
    
    // Draw text with proper wrapping
    List<String> lines = wrapText(text, width, textFont, textSize);
    for (String line : lines) {
        drawText(content, line, x, y, textFont, textSize);
        y -= lineHeight;
    }
    
    // Add space after article
    y -= lineHeight/2;
    return y;
}
private void drawText(PDPageContentStream content, String text, float x, float y, 
                     PDFont font, float size) throws IOException {
    content.beginText();
    content.setFont(font, size);
    content.newLineAtOffset(x, y);
    content.showText(text);
    content.endText();
}

private void drawCenteredText(PDPageContentStream content, String text, 
                            float pageWidth, float y, PDFont font, float size) throws IOException {
    float textWidth = font.getStringWidth(text)/1000*size;
    float x = (pageWidth - textWidth)/2;
    drawText(content, text, x, y, font, size);
}

private void drawRightAlignedText(PDPageContentStream content, String text, 
                                float rightEdgeX, float y, PDFont font, float size) throws IOException {
    float textWidth = font.getStringWidth(text)/1000*size;
    drawText(content, text, rightEdgeX - textWidth, y, font, size);
}

// Helper methods
private void drawSectionTitle(PDPageContentStream content, String title, float x, float y, PDFont font, float size) throws IOException {
    content.beginText();
    content.setFont(font, size);
    content.newLineAtOffset(x, y);
    content.showText(title);
    content.endText();
}

private float drawTable(PDPageContentStream content, float x, float y,
                      String[][] data, PDFont font, float size, float lineHeight, float labelWidth) throws IOException {
    for (String[] row : data) {
        drawText(content, row[0], x, y, font, size);
        drawText(content, row[1], x + labelWidth, y, font, size);
        y -= lineHeight;
    }
    return y;
}
   // Helper method to draw articles at consistent heights
private float drawAlignedArticle(PDPageContentStream content, String title, String text,
                               float x, float y, float width,
                               PDFont titleFont, PDFont textFont,
                               float titleSize, float textSize,
                               float lineHeight, int maxLines) throws IOException {
    // Draw title
    content.beginText();
    content.setFont(titleFont, titleSize);
    content.newLineAtOffset(x, y);
    content.showText(title);
    content.endText();
    
    // Draw text
    List<String> lines = wrapText(text, width, textFont, textSize);
    for (int i = 0; i < Math.min(lines.size(), maxLines); i++) {
        content.beginText();
        content.setFont(textFont, textSize);
        content.newLineAtOffset(x, y - (i+1)*lineHeight);
        content.showText(lines.get(i));
        content.endText();
    }
    
    // Return Y position for next article
    return y - (maxLines + 1) * lineHeight;
}

// Calculate maximum lines needed for articles in a column
private int calculateMaxLines(String[] articles, float width, PDFont font, float size) throws IOException {
    int maxLines = 0;
    for (String article : articles) {
        int lines = wrapText(article, width, font, size).size();
        if (lines > maxLines) {
            maxLines = lines;
        }
    }
    return maxLines;
}

// Helper methods




private float drawArticle(PDPageContentStream content, String title, String text,
                         float x, float y, float width,
                         PDFont titleFont, PDFont textFont,
                         float titleSize, float textSize, 
                         float lineHeight) throws IOException {
    // Draw title
    content.beginText();
    content.setFont(titleFont, titleSize);
    content.newLineAtOffset(x, y);
    content.showText(title);
    content.endText();
    y -= lineHeight;
    
    // Draw text
    List<String> lines = wrapText(text, width, textFont, textSize);
    for (String line : lines) {
        if (y < 50) break;
        
        content.beginText();
        content.setFont(textFont, textSize);
        content.newLineAtOffset(x, y);
        content.showText(line);
        content.endText();
        y -= lineHeight;
    }
    
    return y - lineHeight/2;
}


// Helper methods remain the same as previous example
// Helper methods
private void drawText(PDPageContentStream content, String text, float x, float y, PDFont font, int size) throws IOException {
    content.beginText();
    content.setFont(font, size);
    content.newLineAtOffset(x, y);
    content.showText(text);
    content.endText();
}



private String getVehicleConditionText(Contrat contrat) {
    StringBuilder sb = new StringBuilder();
    if (contrat.isCarrosserieOk()) sb.append("Carrosserie OK, ");
    if (contrat.isPneusOk()) sb.append("Pneus OK, ");
    if (contrat.isSiegesOk()) sb.append("Sièges OK, ");
    if (contrat.getAutresRemarques() != null) sb.append(contrat.getAutresRemarques());
    return sb.toString();
}

private String getSelectedOptionsText(Contrat contrat) {
    StringBuilder sb = new StringBuilder();
    if (contrat.isRoueSecours()) sb.append("Roue de secours\n");
    if (contrat.isCric()) sb.append("Cric\n");
    if (contrat.isSiegeBebe()) sb.append("Siège bébé\n");
    if (contrat.isCleRoue()) sb.append("Clé de roue\n");
    return sb.toString();
}


private List<String> wrapText(String text, float width, PDFont font, float size) throws IOException {
    List<String> lines = new ArrayList<>();
    
    if (text == null || text.isEmpty()) {
        return lines;
    }

    // Replace newlines with spaces to avoid U+000A error
    String cleanedText = text.replace("\n", " ").replace("\r", " ");
    String[] words = cleanedText.split(" ");
    StringBuilder line = new StringBuilder();

    for (String word : words) {
        if (word.isEmpty()) {
            continue; // Skip empty words from multiple spaces
        }

        String testLine = line.length() > 0 ? line + " " + word : word;
        
        try {
            float testWidth = font.getStringWidth(testLine) / 1000 * size;
            
            if (testWidth <= width) {
                line.append(line.length() > 0 ? " " + word : word);
            } else {
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    // Break very long words into chunks
                    String remainingWord = word;
                    while (!remainingWord.isEmpty()) {
                        int chunkSize = estimateChunkSize(remainingWord, width, font, size);
                        lines.add(remainingWord.substring(0, chunkSize));
                        remainingWord = remainingWord.substring(chunkSize);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // Fallback: Replace problematic characters
            word = sanitizeText(word);
            line.append(line.length() > 0 ? " " + word : word);
        }
    }

    if (line.length() > 0) {
        lines.add(line.toString());
    }

    return lines;
}

private int estimateChunkSize(String word, float maxWidth, PDFont font, float size) throws IOException {
    // Estimate how many characters can fit in the width
    int estimatedLength = (int) (maxWidth * 1000 / (font.getStringWidth("X") / size));
    return Math.min(Math.max(1, estimatedLength), word.length());
}

private String sanitizeText(String text) {
    // Replace control characters and non-ANSI chars
    return text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "?");
}
private int findBreakPosition(String word, int start, float maxWidth, PDFont font, float size) 
    throws IOException {
    int low = start + 1;
    int high = word.length();
    int result = start + 1;
    
    while (low <= high) {
        int mid = (low + high) / 2;
        String sub = word.substring(start, mid);
        float width = font.getStringWidth(sub) / 1000 * size;
        
        if (width <= maxWidth) {
            result = mid;
            low = mid + 1;
        } else {
            high = mid - 1;
        }
    }
    
    // Ensure we make progress even if no characters fit
    return result > start ? result : start + 1;
}

private String formatDate(Date date) {
    if (date == null) return "N/A";
    return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
}
private float drawFlowingConditionArticle(PDPageContentStream content, 
                                        String title, String text,
                                        float startX, float y,
                                        float columnWidth, 
                                        PDFont titleFont, PDFont textFont,
                                        float titleSize, float textSize, 
                                        float lineHeight,
                                        PDPage page) throws IOException {
    
    float currentX = startX;
    float pageHeight = page.getMediaBox().getHeight();
    float bottomMargin = CONDITION_MARGIN;
    
    // Draw article title
    drawText(content, title, currentX, y, titleFont, titleSize);
    y -= lineHeight * 1.5f;
    
    // Split text into paragraphs
    String[] paragraphs = text.split("\n\n");
    
    for (String paragraph : paragraphs) {
        List<String> lines = wrapText(paragraph, columnWidth, textFont, textSize);
        
        for (String line : lines) {
            // Check if we need to switch columns or pages
            if (y < bottomMargin) {
                if (currentX == startX) {
                    // Move to right column
                    currentX = startX + columnWidth + COLUMN_GAP;
                    y = pageHeight - CONDITION_MARGIN - lineHeight*2; // Reset Y with some space
                } else {
                    // Would need to create new page here if implementing multi-page conditions
                    break; // For now just stop drawing
                }
            }
            
            drawText(content, line, currentX, y, textFont, textSize);
            y -= lineHeight;
        }
        
        // Add paragraph spacing
        y -= lineHeight * 0.5f;
    }
    
    return y;
}
private String getArticle1Text() {
    return "Le locataire s'engage à ne pas laisser conduire la voiture par d'autres personnes que lui-même ou celles agrées par la loueur et dont il se porte garant,\n" +
"et à n'utiliser le véhicule que pour ses besoins personnels. Il est interdit de faire la piste, de conduire en état d'ivresse, de participer à toute compétition, qu'elle soit, et d'utiliser le véhicule des fins illicites ou des transports de marchandises, le locataire s'engage à ne pas solliciter directement des documents douaniers il est interdit au locataire de surcharger le véhicule loué entranspor- tant un nombre de passagers supérieur à celui porté sur le contrat sous peine d'être déchu de l'assurance.";
}private String getArticle2Text() {
    return "La voiture est livrée en parfait état de marche et de propreté. Les compteurs et leurs prises sont plombés et les plombs ne pourront être enlevés ou violés sous peine de devoir payer la location sur la base de 500km/jour. La voiture sera rendue dans le même état de propreté. À défaut, le locataire devra acquitter le montant des nettoyages et remise en état. Les 5 pneus sont en bon état, sans coupure, l'usure est normale. En cas de détérioration de l'un d'eux pour une cause autre que l'usure normale, le locataire s'engage à le remplacer immédiatement par un pneu de même dimension et d'usure sensiblement égale ou de payer le montant.";
}private String getArticle3Text() {
    return " L'Essence est à la charge du client le locataire doit vérifier en permanence les niveaux d'huile et d'eau, et vérifier les niveaux de la boite de vitesse et du pont arrière tous les 1000 Kilomètres.Il justifiera de ses travaux par des factures correspondantes (qui lui seront remboursées) sous peine d'avoir à payer une indemnité pour usure anormale. ";
}private String getArticle4Text() {
    return "L'usure mécanique normale est à la charge du loueur. Toutes les réparations provenant soit d'une usure anormale, soit d'une négligence de la part d'un loca- taire ou d'une cause accidentelle, seront à sa charge et exécutées par nos soins Dans le cas ou le véhicule serait immobilisé en dehors de la région, les réparations qu'elle soient dues à l'usure normale ou à une cause accidentelle, ne seront exécutées qu'après accord télégraphique du loueur ou par l'agence régionale de la marque du véhicule. Elles devront faire l'objet d'une facture acquittée et très détaillée, les pièces défectueuses remplacées devront être présentées avec la facture acquitée. En aucun cas et en aucun circonstance, le locataire ne pourra réclamer des dommages et intérêts, soit pour retard de la remise de la voiture ou annulation de la location, soit pour l'immobilisation dans le cas de réparations nécessitées par l'usure normale et effectuée au cours de la location, la responsabilité du loueur ne pourra jamais être invoquée, même en cas d'accident de personnes ou de choses ayant pu résulter de vis ou de défauts de construction ou de réparations antérieurs.";
}/*private String getArticle5Text() {
    return "Le locataire est assuré pour les risques\n" +
"suivants:\n" +
"-Le vol et incendie du véhicule loué, à l'exclusion des vêtements et\n" +
"une\n" +
"- Le locataire s'engage à déclarer au loueur, dans les 48 heures et incendie, même partiel sous peine d'être déchu du bénéfice de immédiatement aux autorités de police, tout accidents, vol ou l'assurance. Sa déclaration devra obligatoirement mentionner les circonstances, la date, le lieu, l'heure, le numéro ou le nom de l'agent le nom et l'adresse des témoins ainsi que le numéro de la voiture de l'adversaire. s'il ya lieu il joindre à cette déclaration tout rapport de police, de gendarmerie ou constat d'huissier, s'il en à été établi, il ne devra en aucun cas discuter la responsabilité ni traiter ou transiger avec des tiers relativement à l'accident, il paiera somme de 200 dhs par jour indemnité de chômage de la voiture pendant toute la durée de location, passé ce délai le loueur décline toute responsabilité pour les accidents que le locataire aurait pu causer et dont il devra faire son affaire personnelle. Enfin il n y'a pas d'assurance pour tout conducteur non muni d'un permis en état de validité ou d'un permis datant de moins de six mois. le loueur décline toute responsabilité pour les accidents aux tiers ou dégât de la voiture que le locataire pourrait causer pendant la période de location si le locataire a délibérément fourni au loueur des informations fausses concernant son identité son adresse ou la validité de son permis de conduire.";
}*/
private String getArticle6Text() {
    return "Les prix de location, ainsi la caution, sont déterminées par les tarifs en vigueur et payable d'avance. la caution ne pourra servir en aucun une prolongation de location, Afin d'éviter toute contestation et pour le cas ou le locataire voudrait conserver la voiture pour un temps supérieur à celui indiqué sur le contrat, devra aprés avoir obtenu l'accord duloueur faire parvenirlemontant de la locationsup- plémentaire 48 heures avant l'expiratio de la location en cours sous peine de s'exposer à des poursuites pour détournement de voiture compte de 0 heure à 24 heures et toute journée commencée est dûé en entier.";
}private String getArticle7Text() {
    return "Le locataire s'interdit formellement d'abandonner le véhicule.\n" +
"En cas d'impossibilité matérielle celle ci sera rapatriée aux frais et par soins du locataire, la location restant dûe jusqu'au retour du véhicule.";
}private String getArticle8Text() {
    return "Le locataire remettra, dés la fin de la location et à la rentrée de la voiture la carte grise et tous les papiers nécessaires à sa circulation faute de quoi, ces pièces étant indispensables à de nouvelles locations. la location continuera à être facturée aux prix initial jusqu'à leur remise à la société. En cas de perte de ses papiers le locataire devra acquitter le montant des frais du duplicata.";
}private String getArticle9Text() {
    return "Le locataire demeure seul responsable des amendes, contravention et procès-verbaux établis contre lui et de toute personnelle.";
}private String getArticle10Text() {
    return "De convention expresse et en cas de contestation quelconque,letribunalrégional d'Agadirseraseul compé- tant, les frais de timbre et d'enregistrement restant à la charge du locataire.";
}
private String getArticle11Text() {
    return "Pendant la durée de la location, le locataire sera responsable de tous délai ou contravention relevés à son encontre.";
}
private String getArticle5Text() {
    return "Le locataire est assuré pour les risques suivants:\n\n" +
           "- Le vol et incendie du véhicule loué, à l'exclusion des vêtements et objets personnels\n" +
           "- Les dommages causés aux tiers\n\n" +
           "Le locataire s'engage à déclarer au loueur, dans les 48 heures, tout accident, vol ou incendie, même partiel, sous peine d'être déchu du bénéfice de l'assurance. Sa déclaration doit mentionner:\n" +
           "- Les circonstances\n- La date, le lieu et l'heure\n- Le numéro et nom de l'agent\n- Les coordonnées des témoins\n\n" +
           "Il devra joindre à cette déclaration tout rapport de police ou constat d'huissier. Il ne devra en aucun cas discuter la responsabilité ni traiter directement avec des tiers.\n\n" +
           "En cas d'immobilisation du véhicule, une indemnité de 200 DH par jour sera due après le délai initial de location. Le loueur décline toute responsabilité pour les accidents causés par un conducteur sans permis valide ou avec un permis de moins de 6 mois.";
}
}
