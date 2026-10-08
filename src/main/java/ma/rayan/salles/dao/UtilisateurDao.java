package ma.rayan.salles.dao;

import java.util.List;
import javax.persistence.EntityManager;
import ma.rayan.salles.entities.Utilisateur;

public class UtilisateurDao extends GenericDao<Utilisateur> {
    public UtilisateurDao(EntityManager em) { super(Utilisateur.class, em); }

    public List<Utilisateur> findBySalleNom(String nomSalle) {
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.salle.nom = :nom ORDER BY u.id", Utilisateur.class)
                .setParameter("nom", nomSalle).getResultList();
    }

    public List<Utilisateur> findByNomContenant(String fragment) {
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.nom LIKE :motif ORDER BY u.id", Utilisateur.class)
                .setParameter("motif", "%" + fragment + "%").getResultList();
    }
}
