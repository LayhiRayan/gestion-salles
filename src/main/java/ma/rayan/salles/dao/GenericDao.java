package ma.rayan.salles.dao;

import java.util.List;
import java.util.function.Supplier;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class GenericDao<T> {
    private final Class<T> type;
    protected final EntityManager em;

    public GenericDao(Class<T> type, EntityManager em) {
        this.type = type;
        this.em = em;
    }

    public void create(T entite) {
        transaction(() -> { em.persist(entite); return null; });
    }

    public T find(Object id) {
        return em.find(type, id);
    }

    public T update(T entite) {
        return transaction(() -> em.merge(entite));
    }

    public void delete(T entite) {
        transaction(() -> {
            em.remove(em.contains(entite) ? entite : em.merge(entite));
            return null;
        });
    }

    public List<T> findAll() {
        String nomEntite = em.getMetamodel().entity(type).getName();
        return em.createQuery("SELECT e FROM " + nomEntite + " e ORDER BY e.id", type).getResultList();
    }

    private <R> R transaction(Supplier<R> operation) {
        EntityTransaction tx = em.getTransaction();
        if (tx.isActive()) {
            throw new IllegalStateException("Le DAO attend un EntityManager sans transaction active.");
        }
        try {
            tx.begin();
            R resultat = operation.get();
            tx.commit();
            return resultat;
        } catch (RuntimeException erreur) {
            try {
                if (tx.isActive()) {
                    tx.rollback();
                }
            } catch (RuntimeException erreurAnnulation) {
                erreur.addSuppressed(erreurAnnulation);
            }
            throw erreur;
        }
    }
}
