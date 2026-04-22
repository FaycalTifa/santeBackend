package com.uab.sante.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "taux_couverture")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder  // Ajoutez cette annotation
@EntityListeners(AuditingEntityListener.class)
public class TauxCouverture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ajoutez un champ code si vous voulez rechercher par code
    @Column(unique = true, length = 50)
    private String code;


    @Column(name = "taux_pourcentage", nullable = false)
    private Double tauxPourcentage;

    @Column(length = 100)
    private String libelle;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}
