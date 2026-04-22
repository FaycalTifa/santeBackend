// entities/PrescriptionExamen.java
package com.uab.sante.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescriptions_examens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_bulletin", unique = true, length = 50)
    private String numeroBulletin;

    @ManyToOne
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @ManyToOne
    @JoinColumn(name = "examen_id")
    private Examen examen;

    @Column(name = "examen_nom", nullable = false, length = 200)
    private String examenNom;

    @Column(name = "code_acte", length = 20)
    private String codeActe;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "date_prescription")
    private LocalDate datePrescription;

    @Column(name = "prix_total")
    private Double prixTotal;

    @Column(name = "taux_couverture")
    private Double tauxCouverture;

    @Column(name = "montant_pris_en_charge")
    private Double montantPrisEnCharge;

    @Column(name = "montant_ticket_moderateur")
    private Double montantTicketModerateur;

    @Column(name = "montant_paye_patient")
    private Double montantPayePatient;

    private Boolean paye = false;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    private Boolean realise = false;

    @ManyToOne
    @JoinColumn(name = "laboratoire_id")
    private Structure laboratoire;

    @ManyToOne
    @JoinColumn(name = "biologiste_id")
    private Utilisateur biologiste;

    @Column(name = "date_realisation")
    private LocalDate dateRealisation;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "prescriptionExamen", cascade = CascadeType.ALL)
    private List<ResultatExamen> resultats = new ArrayList<>();

    @OneToMany(mappedBy = "prescriptionExamen", cascade = CascadeType.ALL)
    private List<Interpretation> interpretations = new ArrayList<>();

    @OneToMany(mappedBy = "prescriptionExamen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ResultatExamen> resultatsList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "medecin_interpretation_id")
    private Utilisateur medecinInterpretation;

    @Column(columnDefinition = "TEXT")
    private String interpretation;

    @Column(name = "date_interpretation")
    private LocalDate dateInterpretation;

    // ✅ AJOUTER CES CHAMPS POUR LA VALIDATION UAB
    @Column(name = "validation_uab")
    private String validationUab = "EN_ATTENTE";

    @Column(name = "validation_uab_date")
    private LocalDateTime validationUabDate;

    @Column(name = "statut_validation", length = 30)
    private String statutValidation = "EN_ATTENTE"; // EN_ATTENTE, VALIDE, REJETE

    @ManyToOne
    @JoinColumn(name = "validation_uab_par")
    private Utilisateur validationUabPar;

    @Column(name = "motif_rejet", columnDefinition = "TEXT")
    private String motifRejet;
}
