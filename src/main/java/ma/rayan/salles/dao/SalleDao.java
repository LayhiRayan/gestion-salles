package ma.rayan.salles.dao;

import java.util.List;
import javax.persistence.EntityManager;
import ma.rayan.salles.entities.Salle;

public class SalleDao extends GenericDao<Salle> {
    public SalleDao(EntityManager em) { super(Salle.class, em); }

    public List<Salle> findByCapaciteMin(int minimum) {
        return em.createQuery("SELECT s FROM Salle s WHERE s.capacite >= :minimum ORDER BY s.id", Salle.class)
                .setParameter("minimum", minimum).getResultList();
    }
}
