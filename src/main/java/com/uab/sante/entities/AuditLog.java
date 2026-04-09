package com.uab.sante.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(length = 100)
    private String action;

    @Column(length = 50)
    private String entite;

    @Column(name = "entite_id")
    private Long entiteId;

    // CHANGEMENT: JsonNode → String
    @Column(name = "anciennes_valeurs", columnDefinition = "TEXT")
    private String anciennesValeurs;

    @Column(name = "nouvelles_valeurs", columnDefinition = "TEXT")
    private String nouvellesValeurs;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
