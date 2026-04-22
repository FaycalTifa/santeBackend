package com.uab.sante.dto;

import com.uab.sante.entities.*;
import com.uab.sante.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final StructureRepository structureRepository;
    private final MedicamentRepository medicamentRepository;
    private final ExamenRepository examenRepository;
    private final AssureRepository assureRepository;
    private final TauxCouvertureRepository tauxCouvertureRepository;
    private final RoleRepository roleRepository;  // ✅ Ajouter RoleRepository
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("=== DÉMARRAGE DE L'INITIALISATION DES DONNÉES ===");

        // Initialiser les rôles (NOUVEAU)
        initRoles();

        // Initialiser les structures
        initStructures();

        // Initialiser les médicaments
        initMedicaments();

        // Initialiser les examens
        initExamens();

        // Initialiser les assurés
        initAssures();

        // Initialiser les taux de couverture
        initTauxCouverture();

        // Initialiser les utilisateurs (admin et autres)
        initUtilisateurs();

        System.out.println("=== FIN DE L'INITIALISATION DES DONNÉES ===");
    }

    // ✅ NOUVELLE MÉTHODE : Initialisation des rôles
    private void initRoles() {
        if (roleRepository.count() == 0) {
            System.out.println("Création des rôles...");

            Role[] roles = {
                    Role.builder().code("ADMIN_STRUCTURE").nom("Admin Structure").description("Administrateur de structure").actif(true).build(),
                    Role.builder().code("CAISSIER_HOPITAL").nom("Caissier Hôpital").description("Gère les paiements à l'hôpital").actif(true).build(),
                    Role.builder().code("MEDECIN").nom("Médecin").description("Prescrit des médicaments et examens").actif(true).build(),
                    Role.builder().code("CAISSIER_PHARMACIE").nom("Caissier Pharmacie").description("Gère les paiements à la pharmacie").actif(true).build(),
                    Role.builder().code("PHARMACIEN").nom("Pharmacien").description("Délivre les médicaments").actif(true).build(),
                    Role.builder().code("CAISSIER_LABORATOIRE").nom("Caissier Laboratoire").description("Gère les paiements au laboratoire").actif(true).build(),
                    Role.builder().code("BIOLOGISTE").nom("Biologiste").description("Réalise les examens").actif(true).build(),
                    Role.builder().code("UAB_ADMIN").nom("Admin UAB").description("Administrateur de la plateforme").actif(true).build()
            };

            for (Role role : roles) {
                roleRepository.save(role);
            }

            System.out.println("✓ " + roleRepository.count() + " rôles créés");
        } else {
            System.out.println("ℹ Rôles déjà existants: " + roleRepository.count());
        }
    }

    private void initStructures() {
        if (structureRepository.count() == 0) {
            System.out.println("Création des structures...");

            Structure hopital = Structure.builder()
                    .type(Structure.TypeStructure.HOPITAL)
                    .nom("Clinique Sainte Marie")
                    .codeStructure("CSM001")
                    .adresse("Abidjan - Cocody, Boulevard Latrille")
                    .telephone("27 20 20 20 20")
                    .email("contact@csm.ci")
                    .agrement("AGR-HOP-001")
                    .compteBancaire("CI00123456789")
                    .actif(true)
                    .build();

            Structure pharmacie = Structure.builder()
                    .type(Structure.TypeStructure.PHARMACIE)
                    .nom("Pharmacie du Centre")
                    .codeStructure("PDC001")
                    .adresse("Abidjan - Plateau, Avenue Chardy")
                    .telephone("27 21 21 21 21")
                    .email("contact@pharmacie.ci")
                    .agrement("AGR-PHA-001")
                    .compteBancaire("CI00234567890")
                    .actif(true)
                    .build();

            Structure laboratoire = Structure.builder()
                    .type(Structure.TypeStructure.LABORATOIRE)
                    .nom("Laboratoire Biomédical")
                    .codeStructure("LAB001")
                    .adresse("Abidjan - Marcory, Rue des Jardins")
                    .telephone("27 22 22 22 22")
                    .email("contact@labo.ci")
                    .agrement("AGR-LAB-001")
                    .compteBancaire("CI00345678901")
                    .actif(true)
                    .build();

            Structure cabinetMedical = Structure.builder()
                    .type(Structure.TypeStructure.CABINET_MEDICAL)
                    .nom("Cabinet Médical Docteur Koné")
                    .codeStructure("CMK001")
                    .adresse("Abidjan - Cocody, Rue des Hôpitaux")
                    .telephone("27 23 23 23 23")
                    .email("contact@cabinetkone.ci")
                    .agrement("AGR-CAB-001")
                    .compteBancaire("CI00456789012")
                    .actif(true)
                    .build();

            structureRepository.save(hopital);
            structureRepository.save(pharmacie);
            structureRepository.save(laboratoire);
            structureRepository.save(cabinetMedical);

            System.out.println("✓ " + structureRepository.count() + " structures créées");
        } else {
            System.out.println("ℹ Structures déjà existantes: " + structureRepository.count());
        }
    }

    private void initMedicaments() {
        if (medicamentRepository.count() == 0) {
            System.out.println("Création des médicaments...");

            Medicament[] medicaments = {
                    createMedicament("AMOX500", "Amoxicilline", "500mg", "Comprimé", 250.0),
                    createMedicament("AMOX250", "Amoxicilline", "250mg", "Gélule", 200.0),
                    createMedicament("PARA1000", "Paracétamol", "1000mg", "Comprimé", 200.0),
                    createMedicament("PARA500", "Paracétamol", "500mg", "Comprimé", 150.0),
                    createMedicament("IBU400", "Ibuprofène", "400mg", "Comprimé", 300.0),
                    createMedicament("IBU200", "Ibuprofène", "200mg", "Comprimé", 200.0),
                    createMedicament("AZI500", "Azithromycine", "500mg", "Comprimé", 450.0),
                    createMedicament("AZI250", "Azithromycine", "250mg", "Comprimé", 300.0),
                    createMedicament("DOLI500", "Doliprane", "500mg", "Comprimé", 150.0),
                    createMedicament("VENTOLINE", "Ventoline", "100mcg", "Aérosol", 1200.0),
                    createMedicament("SPASFON", "Spasfon", "80mg", "Comprimé", 180.0),
                    createMedicament("EFFERALGAN", "Efferalgan", "500mg", "Comprimé effervescent", 200.0),
                    createMedicament("ASPIRINE", "Aspirine", "500mg", "Comprimé", 100.0),
                    createMedicament("DEXIUM", "Dexium", "30mg", "Gélule", 800.0),
                    createMedicament("INEXIUM", "Inexium", "20mg", "Gélule", 750.0)
            };

            for (Medicament m : medicaments) {
                medicamentRepository.save(m);
            }

            System.out.println("✓ " + medicamentRepository.count() + " médicaments créés");
        } else {
            System.out.println("ℹ Médicaments déjà existants: " + medicamentRepository.count());
        }
    }

    private Medicament createMedicament(String code, String nom, String dosage, String forme, Double prixReference) {
        return Medicament.builder()
                .code(code)
                .nom(nom)
                .dosage(dosage)
                .forme(forme)
                .prixReference(prixReference)
                .actif(true)
                .build();
    }

    private void initExamens() {
        if (examenRepository.count() == 0) {
            System.out.println("Création des examens...");

            Examen[] examens = {
                    createExamen("B30", "Bilan sanguin complet (NFS, CRP, Glycémie)", Examen.CategorieExamen.ANALYSE, 8500.0),
                    createExamen("B15", "Numération formule sanguine (NFS)", Examen.CategorieExamen.ANALYSE, 4500.0),
                    createExamen("B20", "Bilan hépatique", Examen.CategorieExamen.ANALYSE, 5500.0),
                    createExamen("B25", "Bilan lipidique", Examen.CategorieExamen.ANALYSE, 6000.0),
                    createExamen("B35", "Bilan rénal", Examen.CategorieExamen.ANALYSE, 5000.0),
                    createExamen("B40", "Bilan thyroïdien", Examen.CategorieExamen.ANALYSE, 7000.0),
                    createExamen("R30", "Radiographie thorax", Examen.CategorieExamen.RADIOLOGIE, 12000.0),
                    createExamen("R20", "Radiographie des membres", Examen.CategorieExamen.RADIOLOGIE, 10000.0),
                    createExamen("R25", "Radiographie du rachis", Examen.CategorieExamen.RADIOLOGIE, 11000.0),
                    createExamen("E30", "Échographie abdominale", Examen.CategorieExamen.ECHOGRAPHIE, 15000.0),
                    createExamen("E20", "Échographie pelvienne", Examen.CategorieExamen.ECHOGRAPHIE, 12000.0),
                    createExamen("E25", "Échographie obstétricale", Examen.CategorieExamen.ECHOGRAPHIE, 13000.0),
                    createExamen("SCAN", "Scanner cérébral", Examen.CategorieExamen.SCANNER, 45000.0),
                    createExamen("SCAN2", "Scanner thoracique", Examen.CategorieExamen.SCANNER, 50000.0),
                    createExamen("IRM", "IRM lombaire", Examen.CategorieExamen.IRM, 80000.0)
            };

            for (Examen e : examens) {
                examenRepository.save(e);
            }

            System.out.println("✓ " + examenRepository.count() + " examens créés");
        } else {
            System.out.println("ℹ Examens déjà existants: " + examenRepository.count());
        }
    }

    private Examen createExamen(String code, String nom, Examen.CategorieExamen categorie, Double prixReference) {
        return Examen.builder()
                .code(code)
                .nom(nom)
                .categorie(categorie)
                .prixReference(prixReference)
                .actif(true)
                .build();
    }

    private void initAssures() {
        if (assureRepository.count() == 0) {
            System.out.println("Création des assurés...");

            Assure[] assures = {
                    createAssure("0056787", "Diarra", "Ousmane", LocalDate.of(1985, 5, 12), "70 12 34 56", "ousmane.diarra@email.ci"),
                    createAssure("0056712", "Traoré", "Aminata", LocalDate.of(1990, 3, 25), "71 23 45 67", "aminata.traore@email.ci"),
                    createAssure("0056690", "Coulibaly", "Mamadou", LocalDate.of(1978, 11, 8), "72 34 56 78", "mamadou.coulibaly@email.ci"),
                    createAssure("0056701", "Koné", "Fatoumata", LocalDate.of(1995, 7, 15), "73 45 67 89", "fatoumata.kone@email.ci"),
                    createAssure("0056723", "Bamba", "Ibrahim", LocalDate.of(1982, 9, 22), "74 56 78 90", "ibrahim.bamba@email.ci")
            };

            for (Assure a : assures) {
                assureRepository.save(a);
            }

            System.out.println("✓ " + assureRepository.count() + " assurés créés");
        } else {
            System.out.println("ℹ Assurés déjà existants: " + assureRepository.count());
        }
    }

    private Assure createAssure(String numeroPolice, String nom, String prenom, LocalDate dateNaissance, String telephone, String email) {
        return Assure.builder()
                .numeroPolice(numeroPolice)
                .nom(nom)
                .prenom(prenom)
                .dateNaissance(dateNaissance)
                .telephone(telephone)
                .email(email)
                .statut("ACTIF")
                .build();
    }

    private void initTauxCouverture() {
        if (tauxCouvertureRepository.count() == 0) {
            System.out.println("Création des taux de couverture...");

            Assure diarra = assureRepository.findByNumeroPolice("0056787").orElse(null);
            Assure traore = assureRepository.findByNumeroPolice("0056712").orElse(null);
            Assure coulibaly = assureRepository.findByNumeroPolice("0056690").orElse(null);
            Assure kone = assureRepository.findByNumeroPolice("0056701").orElse(null);
            Assure bamba = assureRepository.findByNumeroPolice("0056723").orElse(null);

            LocalDate today = LocalDate.now();

            if (diarra != null) {
                createTauxCouverture(diarra, 80.0, "STANDARD", today, null);
            }
            if (traore != null) {
                createTauxCouverture(traore, 90.0, "PREMIUM", today, null);
            }
            if (coulibaly != null) {
                createTauxCouverture(coulibaly, 70.0, "ECONOMIQUE", today, null);
            }
            if (kone != null) {
                createTauxCouverture(kone, 85.0, "FAMILLE", today, null);
            }
            if (bamba != null) {
                createTauxCouverture(bamba, 95.0, "VIP", today, null);
            }

            System.out.println("✓ " + tauxCouvertureRepository.count() + " taux de couverture créés");
        } else {
            System.out.println("ℹ Taux de couverture déjà existants: " + tauxCouvertureRepository.count());
        }
    }

    private void createTauxCouverture(Assure assure, Double taux, String typeContrat, LocalDate dateDebut, LocalDate dateFin) {

        TauxCouverture tauxCouverture = TauxCouverture.builder()
                .tauxPourcentage(taux)
                .libelle("Couverture " + typeContrat + " - " + taux + "%")
                .code("TAUX_" + typeContrat + "_" + taux.intValue())
                .build();

        tauxCouvertureRepository.save(tauxCouverture);
    }

    // ✅ MÉTHODE MODIFIÉE : Utilisation de rôles multiples
    private void initUtilisateurs() {
        System.out.println("Vérification des utilisateurs...");

        // Récupérer les structures
        Structure hopital = structureRepository.findByCodeStructure("CSM001").orElse(null);
        Structure pharmacie = structureRepository.findByCodeStructure("PDC001").orElse(null);
        Structure laboratoire = structureRepository.findByCodeStructure("LAB001").orElse(null);
        Structure cabinet = structureRepository.findByCodeStructure("CMK001").orElse(null);

        // Récupérer les rôles
        Role roleUABAdmin = roleRepository.findByCode("UAB_ADMIN").orElse(null);
        Role roleMedecin = roleRepository.findByCode("MEDECIN").orElse(null);
        Role roleCaissierHopital = roleRepository.findByCode("CAISSIER_HOPITAL").orElse(null);
        Role rolePharmacien = roleRepository.findByCode("PHARMACIEN").orElse(null);
        Role roleCaissierPharmacie = roleRepository.findByCode("CAISSIER_PHARMACIE").orElse(null);
        Role roleBiologiste = roleRepository.findByCode("BIOLOGISTE").orElse(null);
        Role roleCaissierLaboratoire = roleRepository.findByCode("CAISSIER_LABORATOIRE").orElse(null);
        Role roleAdminStructure = roleRepository.findByCode("ADMIN_STRUCTURE").orElse(null);

        // Créer l'admin UAB
        if (roleUABAdmin != null && utilisateurRepository.findByEmail("admin@uab.ci").isEmpty()) {
            System.out.println("Création de l'administrateur UAB...");
            Utilisateur admin = Utilisateur.builder()
                    .structure(null)
                    .nom("Admin")
                    .prenom("UAB")
                    .email("admin@uab.ci")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(List.of(roleUABAdmin))
                    .telephone("0102030405")
                    .actif(true)
                    .build();
            utilisateurRepository.save(admin);
            System.out.println("✓ Administrateur UAB créé (email: admin@uab.ci, mot de passe: admin123)");
        }

        // Créer le médecin
        if (hopital != null && roleMedecin != null && utilisateurRepository.findByEmail("dr.kone@csm.ci").isEmpty()) {
            System.out.println("Création du médecin...");
            Utilisateur medecin = Utilisateur.builder()
                    .structure(hopital)
                    .nom("Koné")
                    .prenom("Mamadou")
                    .email("dr.kone@csm.ci")
                    .password(passwordEncoder.encode("password123"))
                    .roles(List.of(roleMedecin))
                    .telephone("0101010101")
                    .actif(true)
                    .build();
            utilisateurRepository.save(medecin);
            System.out.println("✓ Médecin créé (email: dr.kone@csm.ci, mot de passe: password123)");
        }

        // Créer le caissier hôpital
        if (hopital != null && roleCaissierHopital != null && utilisateurRepository.findByEmail("caisse@csm.ci").isEmpty()) {
            System.out.println("Création du caissier hôpital...");
            Utilisateur caissierHopital = Utilisateur.builder()
                    .structure(hopital)
                    .nom("Traoré")
                    .prenom("Fatou")
                    .email("caisse@csm.ci")
                    .password(passwordEncoder.encode("password123"))
                    .roles(List.of(roleCaissierHopital))
                    .telephone("0101010102")
                    .actif(true)
                    .build();
            utilisateurRepository.save(caissierHopital);
            System.out.println("✓ Caissier hôpital créé (email: caisse@csm.ci, mot de passe: password123)");
        }

        // Créer le pharmacien
        if (pharmacie != null && rolePharmacien != null && utilisateurRepository.findByEmail("pharmacien@pharmacie.ci").isEmpty()) {
            System.out.println("Création du pharmacien...");
            Utilisateur pharmacien = Utilisateur.builder()
                    .structure(pharmacie)
                    .nom("Diallo")
                    .prenom("Oumar")
                    .email("pharmacien@pharmacie.ci")
                    .password(passwordEncoder.encode("password123"))
                    .roles(List.of(rolePharmacien))
                    .telephone("0101010103")
                    .actif(true)
                    .build();
            utilisateurRepository.save(pharmacien);
            System.out.println("✓ Pharmacien créé (email: pharmacien@pharmacie.ci, mot de passe: password123)");
        }

        // Créer le caissier pharmacie
        if (pharmacie != null && roleCaissierPharmacie != null && utilisateurRepository.findByEmail("caisse@pharmacie.ci").isEmpty()) {
            System.out.println("Création du caissier pharmacie...");
            Utilisateur caissierPharmacie = Utilisateur.builder()
                    .structure(pharmacie)
                    .nom("Coulibaly")
                    .prenom("Aminata")
                    .email("caisse@pharmacie.ci")
                    .password(passwordEncoder.encode("password123"))
                    .roles(List.of(roleCaissierPharmacie))
                    .telephone("0101010104")
                    .actif(true)
                    .build();
            utilisateurRepository.save(caissierPharmacie);
            System.out.println("✓ Caissier pharmacie créé (email: caisse@pharmacie.ci, mot de passe: password123)");
        }

        // Créer le biologiste
        if (laboratoire != null && roleBiologiste != null && utilisateurRepository.findByEmail("biologiste@labo.ci").isEmpty()) {
            System.out.println("Création du biologiste...");
            Utilisateur biologiste = Utilisateur.builder()
                    .structure(laboratoire)
                    .nom("Camara")
                    .prenom("Ibrahim")
                    .email("biologiste@labo.ci")
                    .password(passwordEncoder.encode("password123"))
                    .roles(List.of(roleBiologiste))
                    .telephone("0101010105")
                    .actif(true)
                    .build();
            utilisateurRepository.save(biologiste);
            System.out.println("✓ Biologiste créé (email: biologiste@labo.ci, mot de passe: password123)");
        }

        // Créer le caissier laboratoire
        if (laboratoire != null && roleCaissierLaboratoire != null && utilisateurRepository.findByEmail("caisse@labo.ci").isEmpty()) {
            System.out.println("Création du caissier laboratoire...");
            Utilisateur caissierLaboratoire = Utilisateur.builder()
                    .structure(laboratoire)
                    .nom("Bamba")
                    .prenom("Mariam")
                    .email("caisse@labo.ci")
                    .password(passwordEncoder.encode("password123"))
                    .roles(List.of(roleCaissierLaboratoire))
                    .telephone("0101010106")
                    .actif(true)
                    .build();
            utilisateurRepository.save(caissierLaboratoire);
            System.out.println("✓ Caissier laboratoire créé (email: caisse@labo.ci, mot de passe: password123)");
        }

        // Créer le secrétaire (Admin Structure)
        if (cabinet != null && roleAdminStructure != null && utilisateurRepository.findByEmail("secretaire@cabinet.ci").isEmpty()) {
            System.out.println("Création de la secrétaire...");
            Utilisateur secretaire = Utilisateur.builder()
                    .structure(cabinet)
                    .nom("Ouattara")
                    .prenom("Aicha")
                    .email("secretaire@cabinet.ci")
                    .password(passwordEncoder.encode("password123"))
                    .roles(List.of(roleAdminStructure))
                    .telephone("0101010107")
                    .actif(true)
                    .build();
            utilisateurRepository.save(secretaire);
            System.out.println("✓ Secrétaire créé (email: secretaire@cabinet.ci, mot de passe: password123)");
        }

        System.out.println("=== RÉCAPITULATIF DES UTILISATEURS ===");
        System.out.println("📧 admin@uab.ci / admin123 → UAB_ADMIN");
        System.out.println("📧 dr.kone@csm.ci / password123 → MEDECIN");
        System.out.println("📧 caisse@csm.ci / password123 → CAISSIER_HOPITAL");
        System.out.println("📧 pharmacien@pharmacie.ci / password123 → PHARMACIEN");
        System.out.println("📧 caisse@pharmacie.ci / password123 → CAISSIER_PHARMACIE");
        System.out.println("📧 biologiste@labo.ci / password123 → BIOLOGISTE");
        System.out.println("📧 caisse@labo.ci / password123 → CAISSIER_LABORATOIRE");
        System.out.println("📧 secretaire@cabinet.ci / password123 → ADMIN_STRUCTURE");
        System.out.println("Total utilisateurs: " + utilisateurRepository.count());
    }
}
