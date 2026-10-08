package ma.rayan.salles;

import java.util.Arrays;
import ma.rayan.salles.entities.Salle;
import ma.rayan.salles.entities.Utilisateur;
import ma.rayan.salles.service.GestionSallesService;
import org.h2.tools.Server;

public class ApplicationSalles {
    public static void main(String[] args) throws Exception {
        try (GestionSallesService service = new GestionSallesService()) {
            Salle atlas = new Salle("Atlas", 30);
            Salle menara = new Salle("Menara", 12);
            service.ajouterSalle(atlas);
            service.ajouterSalle(menara);
            service.ajouterUtilisateur(new Utilisateur("Rayan", "rayan@example.com", atlas));
            service.ajouterUtilisateur(new Utilisateur("Salma", "salma@example.com", atlas));
            Utilisateur yassine = new Utilisateur("Yassine", "yassine@example.com", menara);
            service.ajouterUtilisateur(yassine);

            System.out.println("\nSalles enregistrées :");
            service.listerSalles().forEach(System.out::println);
            System.out.println("\nUtilisateurs de la salle Atlas :");
            service.utilisateursParSalle("Atlas").forEach(System.out::println);
            System.out.println("\nSalles d'au moins 20 places :");
            service.sallesParCapacite(20).forEach(System.out::println);
            System.out.println("\nUtilisateurs dont le nom contient 'ay' :");
            service.utilisateursParNom("ay").forEach(System.out::println);

            menara.setCapacite(18);
            service.modifierSalle(menara);
            yassine.setSalle(atlas);
            service.modifierUtilisateur(yassine);
            System.out.println("\nAprès modification :");
            System.out.println(service.trouverSalle(menara.getId()));
            System.out.println(service.trouverUtilisateur(yassine.getId()));

            Utilisateur temporaire = new Utilisateur("Visiteur", "visiteur@example.com", null);
            service.ajouterUtilisateur(temporaire);
            service.supprimerUtilisateur(temporaire);
            System.out.println("\nVisiteur temporaire supprimé : "
                    + (service.trouverUtilisateur(temporaire.getId()) == null));

            if (Arrays.asList(args).contains("--console")) {
                Server console = Server.createWebServer("-webPort", "8082").start();
                try {
                    System.out.println("\nConsole H2 : http://localhost:8082");
                    System.out.println("JDBC : jdbc:h2:mem:salles | Utilisateur : sa | Mot de passe vide");
                    System.out.println("Appuyez sur Entrée pour arrêter.");
                    System.in.read();
                } finally {
                    console.stop();
                }
            }
        }
    }
}
