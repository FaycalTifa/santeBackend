package com.uab.sante.controller;

import com.uab.sante.dto.response.AssureResponseDTO;
import com.uab.sante.entities.Assure;
import com.uab.sante.service.AssureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/assures")
@RequiredArgsConstructor
public class AssureController {

    private final AssureService assureService;

    /**
     * Rechercher un assuré par numéro de téléphone
     */
    @GetMapping("/telephone/{telephone}")
    @PreAuthorize("hasAnyRole('CAISSIER_HOPITAL', 'UAB_ADMIN')")
    public ResponseEntity<Assure> rechercherParTelephone(@PathVariable String telephone) {
        Optional<Assure> assure = assureService.findByTelephone(telephone);
        return ResponseEntity.ok(assure.orElse(null));
    }

    /**
     * Rechercher un assuré par numéro de police
     */
    @GetMapping("/police/{numeroPolice}")
    @PreAuthorize("hasAnyRole('CAISSIER_HOPITAL', 'UAB_ADMIN')")
    public ResponseEntity<AssureResponseDTO> rechercherParPolice(@PathVariable String numeroPolice) {
        System.out.println("=== RECHERCHE ASSURÉ PAR POLICE ===");
        System.out.println("Numéro police: " + numeroPolice);

        Optional<Assure> assureOpt = assureService.findByNumeroPolice(numeroPolice);

        if (assureOpt.isPresent()) {
            Assure assure = assureOpt.get();
            System.out.println("✅ Assuré trouvé: " + assure.getPrenom() + " " + assure.getNom());

            // Convertir en DTO sans les relations
            AssureResponseDTO response = AssureResponseDTO.builder()
                    .id(assure.getId())
                    .numeroPolice(assure.getNumeroPolice())
                    .nom(assure.getNom())
                    .prenom(assure.getPrenom())
                    .telephone(assure.getTelephone())
                    .email(assure.getEmail())
                    .adresse(assure.getAdresse())
                    .dateNaissance(assure.getDateNaissance())
                    .statut(assure.getStatut())
                    .build();

            return ResponseEntity.ok(response);
        }

        System.out.println("❌ Aucun assuré trouvé pour la police: " + numeroPolice);
        return ResponseEntity.ok(null);
    }
}
