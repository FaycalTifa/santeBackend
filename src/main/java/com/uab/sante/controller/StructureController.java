package com.uab.sante.controller;

import com.uab.sante.dto.request.StructureRequestDTO;
import com.uab.sante.dto.response.StructureResponseDTO;
import com.uab.sante.entities.Structure;
import com.uab.sante.service.StructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/structures")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UAB_ADMIN')")
public class StructureController {

    private final StructureService structureService;

    /**
     * Liste de toutes les structures
     */
    @GetMapping
    public ResponseEntity<List<StructureResponseDTO>> getAllStructures() {
        List<Structure> structures = structureService.getAll();
        return ResponseEntity.ok(structures.stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Récupérer une structure par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<StructureResponseDTO> getStructureById(@PathVariable Long id) {
        Structure structure = structureService.getById(id);
        return ResponseEntity.ok(toDTO(structure));
    }

    /**
     * Créer une nouvelle structure
     */
    @PostMapping
    public ResponseEntity<StructureResponseDTO> createStructure(@Valid @RequestBody StructureRequestDTO request) {
        Structure structure = Structure.builder()
                .type(Structure.TypeStructure.valueOf(request.getType()))
                .nom(request.getNom())
                .codeStructure(request.getCodeStructure())
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .agrement(request.getAgrement())
                .compteBancaire(request.getCompteBancaire())
                .actif(true)
                .build();

        Structure saved = structureService.create(structure);
        return ResponseEntity.ok(toDTO(saved));
    }

    /**
     * Modifier une structure
     */
    @PutMapping("/{id}")
    public ResponseEntity<StructureResponseDTO> updateStructure(
            @PathVariable Long id,
            @Valid @RequestBody StructureRequestDTO request) {

        Structure structure = structureService.getById(id);
        structure.setType(Structure.TypeStructure.valueOf(request.getType()));
        structure.setNom(request.getNom());
        structure.setCodeStructure(request.getCodeStructure());
        structure.setAdresse(request.getAdresse());
        structure.setTelephone(request.getTelephone());
        structure.setEmail(request.getEmail());
        structure.setAgrement(request.getAgrement());
        structure.setCompteBancaire(request.getCompteBancaire());

        Structure updated = structureService.update(structure);
        return ResponseEntity.ok(toDTO(updated));
    }

    /**
     * Désactiver une structure
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiverStructure(@PathVariable Long id) {
        structureService.desactiver(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Activer une structure
     */
    @PutMapping("/{id}/activer")
    public ResponseEntity<Void> activerStructure(@PathVariable Long id) {
        structureService.activer(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Types de structures disponibles
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getStructureTypes() {
        List<Map<String, String>> types = List.of(
                Map.of("code", "HOPITAL", "label", "Hôpital"),
                Map.of("code", "CLINIQUE", "label", "Clinique"),
                Map.of("code", "PHARMACIE", "label", "Pharmacie"),
                Map.of("code", "LABORATOIRE", "label", "Laboratoire"),
                Map.of("code", "CABINET_MEDICAL", "label", "Cabinet Médical")
        );
        return ResponseEntity.ok(types);
    }

    private StructureResponseDTO toDTO(Structure structure) {
        Map<String, String> typeLabels = getTypeLabels();

        return StructureResponseDTO.builder()
                .id(structure.getId())
                .type(structure.getType().name())
                .typeLabel(typeLabels.getOrDefault(structure.getType().name(), structure.getType().name()))
                .nom(structure.getNom())
                .codeStructure(structure.getCodeStructure())
                .adresse(structure.getAdresse())
                .telephone(structure.getTelephone())
                .email(structure.getEmail())
                .agrement(structure.getAgrement())
                .compteBancaire(structure.getCompteBancaire())
                .actif(structure.getActif())
                .createdAt(structure.getCreatedAt())
                .build();
    }

    private Map<String, String> getTypeLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("HOPITAL", "Hôpital");
        labels.put("CLINIQUE", "Clinique");
        labels.put("PHARMACIE", "Pharmacie");
        labels.put("LABORATOIRE", "Laboratoire");
        labels.put("CABINET_MEDICAL", "Cabinet Médical");
        return labels;
    }
}
