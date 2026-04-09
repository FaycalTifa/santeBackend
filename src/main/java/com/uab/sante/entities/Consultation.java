package com.uab.sante.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consultations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_feuille", unique = true, length = 50)
    private String numeroFeuille;

    @ManyToOne
    @JoinColumn(name = "assure_id", nullable = false)
    private Assure assure;

    @Column(name = "date_consultation", nullable = false)
    private LocalDate dateConsultation;

    @Column(name = "prix_consultation", nullable = false)
    private Double prixConsultation;

    @Column(name = "prix_actes")
    private Double prixActes;

    @Column(name = "montant_total_hospitalier", nullable = false)
    private Double montantTotalHospitalier;

    @Column(name = "taux_couverture")
    private Double tauxCouverture;

    @Column(name = "montant_pris_en_charge")
    private Double montantPrisEnCharge;

    @Column(name = "montant_ticket_moderateur")
    private Double montantTicketModerateur;

    @Column(name = "montant_paye_patient")
    private Double montantPayePatient;

    @ManyToOne
    @JoinColumn(name = "structure_id")
    private Structure structure;

    @ManyToOne
    @JoinColumn(name = "medecin_id")
    private Utilisateur medecin;

    @Column(name = "nature_maladie", length = 255)
    private String natureMaladie;

    @Column(columnDefinition = "TEXT")
    private String diagnostic;

    @Column(name = "actes_medicaux", columnDefinition = "TEXT")
    private String actesMedicaux;

    @Column(name = "prescriptions_validees")
    private Boolean prescriptionsValidees = false;

    @Column(name = "date_prescription")
    private LocalDate datePrescription;

    @Column(length = 30)
    private String statut = "PAYEE_CAISSE";

    @Column(name = "validation_uab")
    private Boolean validationUab = false;

    @Column(name = "validation_uab_date")
    private LocalDateTime validationUabDate;

    @ManyToOne
    @JoinColumn(name = "validation_uab_par")
    private Utilisateur validationUabPar;

    @Column(name = "remboursement_effectue")
    private Boolean remboursementEffectue = false;

    @Column(name = "date_remboursement")
    private LocalDate dateRemboursement;

    @Column(name = "date_transmission")
    private LocalDate dateTransmission;

    @Column(columnDefinition = "TEXT")
    private String remarques;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL)
    private List<PrescriptionMedicament> prescriptionsMedicaments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL)
    private List<PrescriptionExamen> prescriptionsExamens = new ArrayList<>();
}
