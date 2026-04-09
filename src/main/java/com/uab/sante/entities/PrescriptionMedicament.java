package com.uab.sante.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions_medicaments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PrescriptionMedicament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_ordonnance", unique = true, length = 50)
    private String numeroOrdonnance;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    // Saisi par le MEDECIN
    @ManyToOne
    @JoinColumn(name = "medicament_id")
    private Medicament medicament;

    @Column(name = "medicament_nom", nullable = false, length = 200)
    private String medicamentNom;

    @Column(name = "medicament_dosage", length = 50)
    private String medicamentDosage;

    @Column(name = "medicament_forme", length = 50)
    private String medicamentForme;

    @Column(name = "quantite_prescitee", nullable = false)
    private Integer quantitePrescitee;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "date_prescription")
    private LocalDate datePrescription;

    // Saisi par la PHARMACIE
    @Column(name = "prix_unitaire")
    private Double prixUnitaire;

    @Column(name = "prix_total")
    private Double prixTotal;

    @Column(name = "quantite_delivree")
    private Integer quantiteDelivree = 0;

    private Boolean delivre = false;

    @ManyToOne
    @JoinColumn(name = "pharmacie_id")
    private Structure pharmacie;

    @ManyToOne
    @JoinColumn(name = "pharmacien_id")
    private Utilisateur pharmacien;

    @Column(name = "date_delivrance")
    private LocalDate dateDelivrance;

    // Gestion ticket modérateur
    @Column(name = "taux_couverture")
    private Double tauxCouverture;

    @Column(name = "montant_pris_en_charge")
    private Double montantPrisEnCharge;

    @Column(name = "montant_ticket_moderateur")
    private Double montantTicketModerateur;

    @Column(name = "montant_paye_patient")
    private Double montantPayePatient;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
