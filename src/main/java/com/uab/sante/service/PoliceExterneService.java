package com.uab.sante.service;

import com.uab.sante.entities.PoliceOracle;
import com.uab.sante.repository.PoliceOracleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoliceExterneService {

    private final PoliceOracleRepository policeOracleRepository;

    @Qualifier("oracleJdbcTemplate")
    private final JdbcTemplate oracleJdbcTemplate;

    // ==================== RECHERCHE ASSURÉ PRINCIPAL ====================

    /**
     * Récupérer l'assuré principal à partir de NUMEPOLI, CODEINTE, CODERISQ
     */
    public Map<String, Object> getAssurePrincipal(String numPolice, String codeInte, String codeRisq) {
        System.out.println("=== RECHERCHE ASSURÉ PRINCIPAL ===");
        System.out.println("NUMEPOLI: " + numPolice);
        System.out.println("CODEINTE: " + codeInte);
        System.out.println("CODERISQ: " + codeRisq);

        String sql = """
            SELECT DISTINCT 
                r.NUMEPOLI,
                r.CODEINTE,
                r.LIBERISQ,
                r.CATERISQ,
                r.CODERISQ,
                r.DATENAIS,
                NULL as CODEMEMB
            FROM RISQUE r
            WHERE r.CODEINTE = ?
            AND r.NUMEPOLI = ?
            AND r.CODERISQ = ?
        """;

        try {
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, codeInte, numPolice, codeRisq);
            if (results.isEmpty()) {
                System.out.println("❌ Aucun assuré principal trouvé");
                return null;
            }
            Map<String, Object> assurePrincipal = results.get(0);
            assurePrincipal.put("TYPE", "PRINCIPAL");  // ✅ Marquer comme assuré principal
            System.out.println("✅ Assuré principal trouvé");
            return assurePrincipal;
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche de l'assuré principal", e);
        }
    }

    // ==================== RECHERCHE BÉNÉFICIAIRE PAR CODEMEMB ====================

    /**
     * Récupérer un bénéficiaire spécifique par son CODEMEMB
     */
    public Map<String, Object> getBeneficiaireByCodeMemb(String numPolice, String codeInte, String codeMemb) {
        System.out.println("=== RECHERCHE BÉNÉFICIAIRE PAR CODEMEMB ===");
        System.out.println("NUMEPOLI: " + numPolice);
        System.out.println("CODEINTE: " + codeInte);
        System.out.println("CODEMEMB: " + codeMemb);

        String sql = """
            SELECT DISTINCT 
                rf.NUMEPOLI,
                rf.CODEINTE,
                rf.NOM_MEMB as LIBERISQ,
                rf.CODERISQ,
                rf.CODEMEMB,
                rf.DATENAIS
            FROM RISQUE_FAMILLE rf
            WHERE rf.CODEINTE = ?
            AND rf.NUMEPOLI = ?
            AND rf.CODEMEMB = ?
        """;

        try {
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, codeInte, numPolice, codeMemb);
            if (results.isEmpty()) {
                System.out.println("❌ Aucun bénéficiaire trouvé pour CODEMEMB: " + codeMemb);
                return null;
            }
            Map<String, Object> beneficiaire = results.get(0);
            beneficiaire.put("TYPE", "BENEFICIAIRE");  // ✅ Marquer comme bénéficiaire
            System.out.println("✅ Bénéficiaire trouvé: " + beneficiaire.get("LIBERISQ"));
            return beneficiaire;
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche du bénéficiaire", e);
        }
    }

    // ==================== RECHERCHE TOUS LES BÉNÉFICIAIRES ====================

    /**
     * Récupérer tous les bénéficiaires d'une police
     */
    public List<Map<String, Object>> getBeneficiaires(String numPolice, String codeInte, String codeRisq) {
        System.out.println("=== RECHERCHE TOUS LES BÉNÉFICIAIRES ===");
        System.out.println("NUMEPOLI: " + numPolice);
        System.out.println("CODEINTE: " + codeInte);
        System.out.println("CODERISQ: " + codeRisq);

        String sql = """
            SELECT DISTINCT 
                rf.NUMEPOLI,
                rf.CODEINTE,
                rf.NOM_MEMB,
                rf.CODERISQ,
                rf.CODEMEMB,
                rf.DATENAIS
            FROM RISQUE_FAMILLE rf
            WHERE rf.CODEINTE = ?
            AND rf.NUMEPOLI = ?
            AND rf.CODERISQ = ?
            ORDER BY rf.NOM_MEMB
        """;

        try {
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, codeInte, numPolice, codeRisq);

            // ✅ Ajouter le type BENEFICIAIRE pour chaque résultat
            for (Map<String, Object> benef : results) {
                benef.put("TYPE", "BENEFICIAIRE");
            }

            System.out.println("✅ " + results.size() + " bénéficiaires trouvés");
            return results;
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche des bénéficiaires", e);
        }
    }

    // ==================== RECHERCHE POLICE ====================

    /**
     * Rechercher une police par NUMEPOLI et CODEINTE
     */
    public List<Map<String, Object>> searchPolice(String numPolice, String codeInte) {
        System.out.println("=== RECHERCHE POLICE ===");
        System.out.println("NUMEPOLI: " + numPolice);
        System.out.println("CODEINTE: " + codeInte);

        String sql = """
            SELECT DISTINCT 
                p.NUMEPOLI,
                p.CODEINTE,
                p.DATEEFFE,
                p.DATEECHE,
                p.CODEASSU,
                a.RAISSOCI
            FROM POLICE p
            LEFT JOIN ASSURE a ON p.CODEASSU = a.CODEASSU
            WHERE p.CODECATE IN (100, 101, 102, 103, 104)
            AND TO_CHAR(p.NUMEPOLI) LIKE ?
            AND TO_CHAR(p.CODEINTE) LIKE ?
        """;

        String searchNumPattern = "%" + numPolice + "%";
        String searchCodePattern = "%" + codeInte + "%";

        try {
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, searchNumPattern, searchCodePattern);
            System.out.println("✅ " + results.size() + " polices trouvées");
            return results;
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche de la police", e);
        }
    }

    // ==================== RECHERCHE COMPLÈTE ====================

    /**
     * Recherche complète : police + assuré principal + bénéficiaires
     * Si codeMemb est fourni, recherche spécifiquement ce bénéficiaire
     */
    public Map<String, Object> rechercherComplete(String numPolice, String codeInte, String codeRisq, String codeMemb) {
        System.out.println("=== RECHERCHE COMPLÈTE ===");
        System.out.println("NUMEPOLI: " + numPolice);
        System.out.println("CODEINTE: " + codeInte);
        System.out.println("CODERISQ: " + codeRisq);
        System.out.println("CODEMEMB: " + codeMemb);

        Map<String, Object> result = new HashMap<>();

        // 1. Rechercher la police
        List<Map<String, Object>> polices = searchPolice(numPolice, codeInte);
        if (polices.isEmpty()) {
            System.out.println("❌ Police non trouvée");
            return null;
        }
        result.put("police", polices.get(0));

        // 2. Si codeMemb est fourni, rechercher le bénéficiaire spécifique
        if (codeMemb != null && !codeMemb.isEmpty()) {
            Map<String, Object> beneficiaire = getBeneficiaireByCodeMemb(numPolice, codeInte, codeMemb);
            if (beneficiaire != null) {
                result.put("personne", beneficiaire);
                result.put("type", "BENEFICIAIRE");
            } else {
                System.out.println("❌ Bénéficiaire non trouvé pour codeMemb: " + codeMemb);
                return null;
            }
        } else {
            // 3. Sinon, rechercher l'assuré principal
            Map<String, Object> assurePrincipal = getAssurePrincipal(numPolice, codeInte, codeRisq);
            if (assurePrincipal != null) {
                result.put("personne", assurePrincipal);
                result.put("type", "PRINCIPAL");
            }

            // 4. Récupérer tous les bénéficiaires pour la liste
            List<Map<String, Object>> beneficiaires = getBeneficiaires(numPolice, codeInte, codeRisq);
            result.put("beneficiaires", beneficiaires);
        }

        System.out.println("✅ Recherche complète terminée");
        return result;
    }

    // ==================== MÉTHODES EXISTANTES ====================

    public List<Map<String, Object>> getPlafonnementsByPolice(String numPolice, String codeInte) {
        String sql = """
            SELECT DISTINCT
                dgp.CODEINTE,
                dgp.NUMEPOLI,
                dgp.CODECATE,
                dgp.VALEPLAF,
                dgp.CODEPRES,
                p.LIBEPRES
            FROM DETAIL_GROUPE_PLAFOND dgp
            JOIN PRESTATION p ON p.CODEPRES = dgp.CODEPRES
            WHERE dgp.CODECATE IN (100, 101, 102, 103, 104)
            AND dgp.NUMEPOLI = ?
            AND dgp.CODEINTE = ?
            AND dgp.CODEPRES IN ('C01', 'C04', 'C00')
            ORDER BY dgp.CODEPRES
        """;

        try {
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, numPolice, codeInte);
            System.out.println("✅ " + results.size() + " plafonnements trouvés");
            return results;
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération plafonnements: " + e.getMessage());
            return new ArrayList<>();
        }


    }

    public List<Map<String, Object>> getPlafonnementsAll(String numPolice, String codeInte) {
        String sql = """
            SELECT DISTINCT
                dgp.CODEINTE,
                dgp.NUMEPOLI,
                dgp.CODECATE,
                dgp.VALEPLAF,
                dgp.CODEPRES,
                p.LIBEPRES
            FROM DETAIL_GROUPE_PLAFOND dgp
            JOIN PRESTATION p ON p.CODEPRES = dgp.CODEPRES
            WHERE dgp.CODECATE IN (100, 101, 102, 103, 104)
            AND dgp.NUMEPOLI = ?
            AND dgp.CODEINTE = ?
            ORDER BY dgp.CODEPRES
        """;

        try {
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, numPolice, codeInte);
            System.out.println("✅ " + results.size() + " plafonnements trouvés");
            return results;
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération plafonnements: " + e.getMessage());
            return new ArrayList<>();
        }


    }

    public Map<String, Object> getPlafonnementByCodePres(String numPolice, String codeInte, String codePres) {
        String sql = """
            SELECT DISTINCT
                dgp.CODEINTE,
                dgp.NUMEPOLI,
                dgp.CODECATE,
                dgp.VALEPLAF,
                dgp.CODEPRES,
                p.LIBEPRES
            FROM DETAIL_GROUPE_PLAFOND dgp
            JOIN PRESTATION p ON p.CODEPRES = dgp.CODEPRES
            WHERE dgp.CODECATE IN (100, 101, 102, 103, 104)
            AND dgp.NUMEPOLI = ?
            AND dgp.CODEINTE = ?
            AND dgp.CODEPRES = ?
        """;

        try {
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, numPolice, codeInte, codePres);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return null;
        }
    }

    public List<Map<String, Object>> getCodeRisqList(String numPolice, String codeInte) {
        String sql = """
            SELECT DISTINCT 
                r.CODERISQ,
                r.LIBERISQ
            FROM RISQUE r
            WHERE r.NUMEPOLI = ?
            AND r.CODEINTE = ?
            ORDER BY r.CODERISQ
        """;

        try {
            return oracleJdbcTemplate.queryForList(sql, numPolice, codeInte);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== METHODES JPA ====================

    public boolean existePolice(String numPolice) {
        return policeOracleRepository.findByNumePoli(numPolice).isPresent();
    }

    public Optional<PoliceOracle> getPoliceByNumero(String numPolice) {
        return policeOracleRepository.findByNumePoli(numPolice);
    }

    public List<PoliceOracle> getPolicesNonEchues() {
        return policeOracleRepository.findPolicesNonEchues(LocalDate.now());
    }

    public List<PoliceOracle> searchByNomAssure(String nom) {
        return policeOracleRepository.findByRaisSociContainingIgnoreCase(nom);
    }
}
