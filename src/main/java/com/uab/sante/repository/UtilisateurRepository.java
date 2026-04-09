package com.uab.sante.repository;

import com.uab.sante.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    // ✅ MODIFIER - Rechercher par structure
    List<Utilisateur> findByStructureIdAndActifTrue(Long structureId);

    // ✅ MODIFIER - Rechercher par rôle (via la table des rôles)
    @Query("SELECT u FROM Utilisateur u JOIN u.roles r WHERE r.code = :roleCode AND u.actif = true")
    List<Utilisateur> findByRoleCodeAndActifTrue(@Param("roleCode") String roleCode);

    // ✅ MODIFIER - Rechercher par plusieurs rôles
    @Query("SELECT u FROM Utilisateur u JOIN u.roles r WHERE r.code IN :roleCodes AND u.actif = true")
    List<Utilisateur> findByRoleCodesAndActifTrue(@Param("roleCodes") List<String> roleCodes);

    // ✅ NOUVEAU - Rechercher par nom ou prénom
    @Query("SELECT u FROM Utilisateur u WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Utilisateur> searchByNomOrPrenom(@Param("search") String search);

    // ✅ NOUVEAU - Rechercher les utilisateurs actifs
    List<Utilisateur> findByActifTrue();

    // ✅ NOUVEAU - Rechercher par structure et rôle
    @Query("SELECT u FROM Utilisateur u JOIN u.roles r WHERE u.structure.id = :structureId AND r.code = :roleCode AND u.actif = true")
    List<Utilisateur> findByStructureIdAndRoleCode(@Param("structureId") Long structureId, @Param("roleCode") String roleCode);
}
