package ma.rayan.salles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import ma.rayan.salles.entities.Salle;
import ma.rayan.salles.entities.Utilisateur;
import ma.rayan.salles.service.GestionSallesService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GestionSallesServiceTest {
    private GestionSallesService service;

    @Before
    public void preparer() {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("javax.persistence.jdbc.url", "jdbc:h2:mem:test_" + UUID.randomUUID());
        configuration.put("hibernate.hbm2ddl.auto", "create-drop");
        configuration.put("hibernate.show_sql", "false");
        service = new GestionSallesService(configuration);
    }

    @After
    public void terminer() {
        if (service != null) { service.fermer(); }
    }

    private Salle salle(String nom, int capacite) {
        Salle salle = new Salle(nom, capacite);
        service.ajouterSalle(salle);
        return salle;
    }

    @Test
    public void creerLireModifierEtSupprimerSalle() {
        Salle salle = salle("Atlas", 30);
        assertNotNull(salle.getId());
        assertEquals("Atlas", service.trouverSalle(salle.getId()).getNom());
        assertEquals(1, service.listerSalles().size());
        salle.setCapacite(40);
        service.modifierSalle(salle);
        assertEquals(40, service.trouverSalle(salle.getId()).getCapacite());
        service.supprimerSalle(salle);
        assertNull(service.trouverSalle(salle.getId()));
        assertTrue(service.listerSalles().isEmpty());
    }

    @Test
    public void creerLireReaffecterEtSupprimerUtilisateur() {
        Salle atlas = salle("Atlas", 30);
        Salle menara = salle("Menara", 12);
        Utilisateur utilisateur = new Utilisateur("Salma", "salma@example.com", atlas);
        service.ajouterUtilisateur(utilisateur);
        assertNotNull(utilisateur.getId());
        assertEquals(atlas.getId(), service.trouverUtilisateur(utilisateur.getId()).getSalle().getId());
        utilisateur.setSalle(menara);
        utilisateur.setNom("Salma Amrani");
        service.modifierUtilisateur(utilisateur);
        Utilisateur retrouve = service.trouverUtilisateur(utilisateur.getId());
        assertEquals("Salma Amrani", retrouve.getNom());
        assertEquals("Menara", retrouve.getSalle().getNom());
        service.supprimerUtilisateur(utilisateur);
        assertNull(service.trouverUtilisateur(utilisateur.getId()));
        assertEquals(2, service.listerSalles().size());
    }

    @Test
    public void filtrerParSalleEtFragmentDuNom() {
        Salle atlas = salle("Atlas", 30);
        Salle menara = salle("Menara", 12);
        service.ajouterUtilisateur(new Utilisateur("Rayan", "rayan@example.com", atlas));
        service.ajouterUtilisateur(new Utilisateur("Salma", "salma@example.com", atlas));
        service.ajouterUtilisateur(new Utilisateur("Yassine", "yassine@example.com", menara));
        assertEquals(2, service.utilisateursParSalle("Atlas").size());
        assertEquals("Yassine", service.utilisateursParSalle("Menara").get(0).getNom());
        assertTrue(service.utilisateursParSalle("Inconnue").isEmpty());
        assertEquals(1, service.utilisateursParNom("ay").size());
        assertEquals("Rayan", service.utilisateursParNom("ay").get(0).getNom());
    }

    @Test
    public void filtrerCapaciteEnIncluantLaLimite() {
        salle("Menara", 12);
        salle("Atlas", 20);
        salle("Toubkal", 40);
        assertEquals(2, service.sallesParCapacite(20).size());
        assertEquals("Atlas", service.sallesParCapacite(20).get(0).getNom());
        assertTrue(service.sallesParCapacite(50).isEmpty());
    }

    @Test
    public void refuserNomSalleDupliqueEtContinuerApresRollback() {
        salle("Atlas", 30);
        assertThrows(RuntimeException.class, () -> salle("Atlas", 12));
        assertEquals(1, service.listerSalles().size());
        assertEquals(30, service.listerSalles().get(0).getCapacite());
        salle("Menara", 12);
        assertEquals(2, service.listerSalles().size());
    }

    @Test
    public void refuserEmailDupliqueLorsDeLaModification() {
        Utilisateur rayan = new Utilisateur("Rayan", "rayan@example.com", null);
        Utilisateur salma = new Utilisateur("Salma", "salma@example.com", null);
        service.ajouterUtilisateur(rayan);
        service.ajouterUtilisateur(salma);
        salma.setEmail(rayan.getEmail());
        assertThrows(RuntimeException.class, () -> service.modifierUtilisateur(salma));
        assertEquals("salma@example.com", service.trouverUtilisateur(salma.getId()).getEmail());
        assertEquals(2, service.listerUtilisateurs().size());
    }

    @Test
    public void protegerSalleReferenceePuisAutoriserSuppression() {
        Salle atlas = salle("Atlas", 30);
        Utilisateur utilisateur = new Utilisateur("Rayan", "rayan@example.com", atlas);
        service.ajouterUtilisateur(utilisateur);
        assertThrows(RuntimeException.class, () -> service.supprimerSalle(atlas));
        assertNotNull(service.trouverSalle(atlas.getId()));
        service.supprimerUtilisateur(utilisateur);
        service.supprimerSalle(atlas);
        assertTrue(service.listerSalles().isEmpty());
    }

    @Test
    public void autoriserUtilisateurSansSalleEtRefuserChampsObligatoiresNuls() {
        Utilisateur utilisateur = new Utilisateur("Visiteur", "visiteur@example.com", null);
        service.ajouterUtilisateur(utilisateur);
        assertNull(service.trouverUtilisateur(utilisateur.getId()).getSalle());
        assertThrows(RuntimeException.class, () -> salle(null, 10));
        assertThrows(RuntimeException.class, () -> service.ajouterUtilisateur(new Utilisateur(null, "test@example.com", null)));
        assertThrows(RuntimeException.class, () -> service.ajouterUtilisateur(new Utilisateur("Test", null, null)));
        assertEquals(1, service.listerUtilisateurs().size());
        assertTrue(service.listerSalles().isEmpty());
    }
}
