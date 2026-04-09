// service/UtilisateurService.java
package com.uab.sante.service;

import com.uab.sante.entities.Role;  // ✅ Importer la bonne classe Role
import com.uab.sante.entities.Structure;
import com.uab.sante.entities.Utilisateur;
import com.uab.sante.repository.RoleRepository;
import com.uab.sante.repository.StructureRepository;
import com.uab.sante.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final StructureRepository structureRepository;
    private final RoleRepository roleRepository;

    public List<Utilisateur> getAll() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur getById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    public Structure getStructureById(Long id) {
        return structureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));
    }

    public List<Role> getAllRoles() {
        return roleRepository.findByActifTrue();
    }

    @Transactional
    public Utilisateur create(Utilisateur utilisateur, List<String> roleCodes) {
        if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Associer les rôles
        List<Role> roles = roleCodes.stream()
                .map(code -> roleRepository.findByCode(code)
                        .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + code)))
                .collect(Collectors.toList());
        utilisateur.setRoles(roles);

        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public Utilisateur update(Utilisateur utilisateur, List<String> roleCodes) {
        // Associer les rôles
        List<Role> roles = roleCodes.stream()
                .map(code -> roleRepository.findByCode(code)
                        .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + code)))
                .collect(Collectors.toList());
        utilisateur.setRoles(roles);

        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void desactiver(Long id) {
        Utilisateur utilisateur = getById(id);
        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void activer(Long id) {
        Utilisateur utilisateur = getById(id);
        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);
    }

    public Utilisateur findByEmailOrThrow(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));
    }

    // Ajoutez cette méthode dans UtilisateurService si nécessaire
    public List<Utilisateur> getUtilisateursByRole(String roleCode) {
        return utilisateurRepository.findByRoleCodeAndActifTrue(roleCode);
    }

    public List<Utilisateur> getUtilisateursByRoles(List<String> roleCodes) {
        return utilisateurRepository.findByRoleCodesAndActifTrue(roleCodes);
    }

    public List<Utilisateur> getUtilisateursByStructureAndRole(Long structureId, String roleCode) {
        return utilisateurRepository.findByStructureIdAndRoleCode(structureId, roleCode);
    }
}
