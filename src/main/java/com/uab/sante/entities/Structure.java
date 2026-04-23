package com.uab.sante.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "structures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Structure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private TypeStructure type;

    @Column(nullable = false, length = 200)
    private String nom;

    @ManyToOne
    @JoinColumn(name = "structure_parente_id")
    private Structure structureParente;

    @Column(name = "code_structure", unique = true, nullable = false, length = 50)
    private String codeStructure;

    @Column(columnDefinition = "TEXT")
    private String adresse;

    @Column(length = 20)
    private String telephone;

    @Column(length = 100)
    private String email;

    @Column(length = 50)
    private String agrement;

    @Column(name = "compte_bancaire", length = 50)
    private String compteBancaire;

    private Boolean actif = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "structure", cascade = CascadeType.ALL)
    private List<Utilisateur> utilisateurs = new ArrayList<>();

    @OneToMany(mappedBy = "structure")
    private List<Consultation> consultations = new ArrayList<>();

    @OneToMany(mappedBy = "pharmacie")
    private List<PrescriptionMedicament> delivrancesPharmacie = new ArrayList<>();

    @OneToMany(mappedBy = "laboratoire")
    private List<PrescriptionExamen> realisationsLaboratoire = new ArrayList<>();

    public enum TypeStructure {
        HOPITAL,
        CLINIQUE,
        PHARMACIE,
        LABORATOIRE,
        CABINET_MEDICAL
    }
}
