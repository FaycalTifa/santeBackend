package com.uab.sante.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "resultats_examens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class ResultatExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prescription_examen_id", nullable = false)
    private PrescriptionExamen prescriptionExamen;

    @Column(length = 100)
    private String parametre;

    @Column(columnDefinition = "TEXT")
    private String valeur;

    @Column(length = 20)
    private String unite;

    @Column(name = "valeur_normale_min", length = 50)
    private String valeurNormaleMin;

    @Column(name = "valeur_normale_max", length = 50)
    private String valeurNormaleMax;

    private Boolean anormal = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
