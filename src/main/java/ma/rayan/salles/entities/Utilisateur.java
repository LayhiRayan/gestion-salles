package ma.rayan.salles.entities;

import javax.persistence.*;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "salle_id")
    private Salle salle;

    public Utilisateur() {
    }

    public Utilisateur(String nom, String email, Salle salle) {
        this.nom = nom;
        this.email = email;
        this.salle = salle;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }

    @Override
    public String toString() {
        return "Utilisateur{id=" + id + ", nom='" + nom + "', email='" + email
                + "', salle=" + (salle == null ? "aucune" : salle.getNom()) + "}";
    }
}
