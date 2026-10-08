package ma.rayan.salles.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import ma.rayan.salles.dao.SalleDao;
import ma.rayan.salles.dao.UtilisateurDao;
import ma.rayan.salles.entities.Salle;
import ma.rayan.salles.entities.Utilisateur;

public class GestionSallesService implements AutoCloseable {
    private final EntityManagerFactory fabrique;

    public GestionSallesService() {
        this(Collections.emptyMap());
    }

    public GestionSallesService(Map<String, ?> configuration) {
        fabrique = Persistence.createEntityManagerFactory("sallesPU", configuration);
    }

    // Un contexte neuf par opération évite de conserver des entités après un échec.
    private <R> R avecGestionnaire(Function<EntityManager, R> operation) {
        EntityManager em = fabrique.createEntityManager();
        try {
            return operation.apply(em);
        } finally {
            em.close();
        }
    }

    public void ajouterSalle(Salle salle) {
        avecGestionnaire(em -> { new SalleDao(em).create(salle); return null; });
    }

    public Salle trouverSalle(Long id) {
        return avecGestionnaire(em -> new SalleDao(em).find(id));
    }

    public Salle modifierSalle(Salle salle) {
        return avecGestionnaire(em -> new SalleDao(em).update(salle));
    }

    public void supprimerSalle(Salle salle) {
        avecGestionnaire(em -> { new SalleDao(em).delete(salle); return null; });
    }

    public List<Salle> listerSalles() {
        return avecGestionnaire(em -> new SalleDao(em).findAll());
    }

    public List<Salle> sallesParCapacite(int minimum) {
        return avecGestionnaire(em -> new SalleDao(em).findByCapaciteMin(minimum));
    }

    public void ajouterUtilisateur(Utilisateur utilisateur) {
        avecGestionnaire(em -> { new UtilisateurDao(em).create(utilisateur); return null; });
    }

    public Utilisateur trouverUtilisateur(Long id) {
        return avecGestionnaire(em -> new UtilisateurDao(em).find(id));
    }

    public Utilisateur modifierUtilisateur(Utilisateur utilisateur) {
        return avecGestionnaire(em -> new UtilisateurDao(em).update(utilisateur));
    }

    public void supprimerUtilisateur(Utilisateur utilisateur) {
        avecGestionnaire(em -> { new UtilisateurDao(em).delete(utilisateur); return null; });
    }

    public List<Utilisateur> listerUtilisateurs() {
        return avecGestionnaire(em -> new UtilisateurDao(em).findAll());
    }

    public List<Utilisateur> utilisateursParSalle(String nomSalle) {
        return avecGestionnaire(em -> new UtilisateurDao(em).findBySalleNom(nomSalle));
    }

    public List<Utilisateur> utilisateursParNom(String fragment) {
        return avecGestionnaire(em -> new UtilisateurDao(em).findByNomContenant(fragment));
    }

    public void fermer() { close(); }

    @Override
    public void close() {
        if (fabrique.isOpen()) {
            fabrique.close();
        }
    }
}
