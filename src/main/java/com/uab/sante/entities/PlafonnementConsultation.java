// entities/PlafonnementConsultation.java
package com.uab.sante.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "plafonnement_consultation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlafonnementConsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codeinte", nullable = false, length = 50)
    private String codeInte;

    @Column(name = "type_consultation", nullable = false, length = 50)
    private String typeConsultation;

    @Column(name = "montant_plafond", nullable = false)
    private Double montantPlafond;

    @Column(name = "taux_remboursement", nullable = false)
    private Double tauxRemboursement;

    @Column(name = "actif")
    private Boolean actif = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
