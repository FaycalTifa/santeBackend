package com.uab.sante.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder  // Ajoutez cette annotation
@EntityListeners(AuditingEntityListener.class)
public class Assure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_police", unique = true, nullable = false, length = 50)
    private String numeroPolice;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(columnDefinition = "TEXT")
    private String adresse;

    @Column(length = 20)
    private String telephone;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String statut = "ACTIF";

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "assure", cascade = CascadeType.ALL)
    private List<Consultation> consultations = new ArrayList<>();

/*    @OneToMany(mappedBy = "assure", cascade = CascadeType.ALL)
    private List<TauxCouverture> tauxCouvertures = new ArrayList<>();*/
}
