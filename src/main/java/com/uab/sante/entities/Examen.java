package com.uab.sante.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "examens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder  // Ajoutez cette annotation
@EntityListeners(AuditingEntityListener.class)
public class Examen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private CategorieExamen categorie;

    @Column(name = "prix_reference")
    private Double prixReference;

    private Boolean actif = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "examen")
    private List<PrescriptionExamen> prescriptions = new ArrayList<>();

    public enum CategorieExamen {
        ANALYSE, RADIOLOGIE, ECHOGRAPHIE, SCANNER, IRM
    }
}
