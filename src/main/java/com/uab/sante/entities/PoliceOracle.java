package com.uab.sante.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "PoliceOracle") // Remplace par le nom réel de ta table
@Data
public class PoliceOracle {

    @Id
    @Column(name = "NUMEPOLI")
    private String numePoli;  // Numéro de police

    @Column(name = "DATEECHE")
    private LocalDate dateEcheance;  // Date d'échéance

    @Column(name = "RAISSOCI")
    private String raisSoci;  // Nom de l'assuré

    @Column(name = "PRENOM")  // Si tu as un prénom séparé
    private String prenom;

    @Column(name = "ADRESSE")
    private String adresse;

    @Column(name = "TELEPHONE")
    private String telephone;
}
