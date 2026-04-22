package com.uab.sante.controller;

import com.uab.sante.entities.PoliceOracle;
import com.uab.sante.service.PoliceExterneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/polices-externes")
@RequiredArgsConstructor
public class PoliceExterneController {

    private final PoliceExterneService policeExterneService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend Oracle fonctionne !");
    }

    /**
     * Recherche complète : police + assuré principal + bénéficiaires
     */
    @GetMapping("/recherche-complete")
    public ResponseEntity<Map<String, Object>> rechercherComplete(
            @RequestParam(name = "numPolice", required = true) String numPolice,
            @RequestParam(name = "codeInte", required = true) String codeInte,
            @RequestParam(name = "codeRisq", required = true) String codeRisq,
            @RequestParam(name = "codeMemb", required = false) String codeMemb) {  // ✅ codeMemb est optionnel

        System.out.println("=== ENDPOINT RECHERCHE-COMPLETE ===");
        System.out.println("numPolice: " + numPolice);
        System.out.println("codeInte: " + codeInte);
        System.out.println("codeRisq: " + codeRisq);
        System.out.println("codeMemb: " + codeMemb);

        // ✅ Appel avec 4 paramètres (codeMemb peut être null)
        Map<String, Object> result = policeExterneService.rechercherComplete(numPolice, codeInte, codeRisq, codeMemb);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Récupérer l'assuré principal
     */
    @GetMapping("/assure-principal")
    public ResponseEntity<Map<String, Object>> getAssurePrincipal(
            @RequestParam String numPolice,
            @RequestParam String codeInte,
            @RequestParam String codeRisq) {

        Map<String, Object> result = policeExterneService.getAssurePrincipal(numPolice, codeInte, codeRisq);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Récupérer les bénéficiaires
     */
    @GetMapping("/beneficiaires")
    public ResponseEntity<List<Map<String, Object>>> getBeneficiaires(
            @RequestParam String numPolice,
            @RequestParam String codeInte,
            @RequestParam String codeRisq) {

        return ResponseEntity.ok(policeExterneService.getBeneficiaires(numPolice, codeInte, codeRisq));
    }

    // controller/PoliceExterneController.java - Ajouter

    @GetMapping("/{numPolice}/{codeInte}/plafonnements")
    public ResponseEntity<List<Map<String, Object>>> getPlafonnements(
            @PathVariable String numPolice,
            @PathVariable String codeInte) {
        return ResponseEntity.ok(policeExterneService.getPlafonnementsByPolice(numPolice, codeInte));
    }

    /**
     * Récupérer TOUS les plafonnements d'une police (sans filtre CODEPRES)
     * Pour la caisse - Visualisation complète
     */
    @GetMapping("/{numPolice}/{codeInte}/plafonnements-all")
    @PreAuthorize("hasAnyRole('CAISSIER_HOPITAL', 'UAB_ADMIN', 'CAISSIER_LABORATOIRE', 'ADMIN_STRUCTURE')")
    public ResponseEntity<List<Map<String, Object>>> getPlafonnementsAll(
            @PathVariable String numPolice,
            @PathVariable String codeInte) {

        System.out.println("=== GET PLAFONNEMENTS ALL ===");
        System.out.println("numPolice: " + numPolice);
        System.out.println("codeInte: " + codeInte);

        List<Map<String, Object>> plafonnements = policeExterneService.getPlafonnementsAll(numPolice, codeInte);

        if (plafonnements == null || plafonnements.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(plafonnements);
    }

    @GetMapping("/{numPolice}/{codeInte}/plafonnement/{codePres}")
    public ResponseEntity<Map<String, Object>> getPlafonnementByCodePres(
            @PathVariable String numPolice,
            @PathVariable String codeInte,
            @PathVariable String codePres) {
        Map<String, Object> result = policeExterneService.getPlafonnementByCodePres(numPolice, codeInte, codePres);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    /**
     * Récupérer la liste des CODERISQ disponibles
     */
    @GetMapping("/code-risq-list")
    public ResponseEntity<List<Map<String, Object>>> getCodeRisqList(
            @RequestParam String numPolice,
            @RequestParam String codeInte) {

        return ResponseEntity.ok(policeExterneService.getCodeRisqList(numPolice, codeInte));
    }

    /**
     * Rechercher une police
     */
    @GetMapping("/search-police")
    public ResponseEntity<List<Map<String, Object>>> searchPolice(
            @RequestParam String numPolice,
            @RequestParam String codeInte) {

        return ResponseEntity.ok(policeExterneService.searchPolice(numPolice, codeInte));
    }
}
