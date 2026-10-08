# Gestion des salles

TP Java SE : gestion de salles et d'utilisateurs avec JPA et Hibernate, sans EJB ni serveur d'applications.

## Exécution

Prérequis : JDK 11 ou supérieur et Maven 3.6.3 ou supérieur.

```sh
mvn clean verify
mvn compile exec:java
```

Dans un IDE, importer `pom.xml` et lancer `ma.rayan.salles.ApplicationSalles`.

La démonstration crée les salles Atlas (30 places) et Menara (12 places), puis les utilisateurs Rayan, Salma et Yassine. Elle montre les recherches JPQL, passe la capacité de Menara à 18 places, réaffecte Yassine à Atlas et supprime un utilisateur temporaire. Les adresses email utilisées sont fictives.

## Organisation du projet

```text
src/main/java/ma/rayan/salles/
├── ApplicationSalles.java
├── entities/
│   ├── Salle.java
│   └── Utilisateur.java
├── dao/
│   ├── GenericDao.java
│   ├── SalleDao.java
│   └── UtilisateurDao.java
└── service/
    └── GestionSallesService.java
src/main/resources/META-INF/persistence.xml
src/test/java/ma/rayan/salles/GestionSallesServiceTest.java
```

`Salle` possède un nom unique et une capacité. `Utilisateur` possède un nom, un email unique et une association `ManyToOne` facultative vers une salle. Il faut enregistrer la salle avant de lui associer un utilisateur. La suppression d'une salle encore référencée est refusée par la clé étrangère ; il faut d'abord réaffecter ou supprimer ses utilisateurs.

La DAO générique fournit `create`, `find`, `update`, `delete` et `findAll`. Les DAO spécifiques ajoutent les recherches par capacité minimale (borne incluse), nom de salle et fragment de nom d'utilisateur. La recherche par fragment emploie `LIKE`, sensible à la casse, avec `%` et `_` comme caractères génériques.

Le service conserve une seule `EntityManagerFactory` et ouvre un `EntityManager` par opération. Chaque écriture possède sa transaction `RESOURCE_LOCAL`, validée par `commit` ou annulée par `rollback`. Les résultats retournés sont détachés : les modifications se sauvegardent avec les méthodes `modifierSalle` et `modifierUtilisateur`. Les appels successifs au service constituent des transactions distinctes. Le service implémente `AutoCloseable` pour fermer la fabrique avec `try-with-resources`.

## Console H2 — port 8082

```sh
mvn compile exec:java "-Dexec.args=--console"
```

Ouvrir [la console H2](http://localhost:8082) sur le même ordinateur et saisir :

| Champ | Valeur |
| --- | --- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:salles` |
| User Name | `sa` |
| Password | laisser vide |

```sql
SELECT * FROM SALLES ORDER BY ID;
SELECT * FROM UTILISATEURS ORDER BY ID;
SELECT u.NOM, u.EMAIL, s.NOM AS SALLE
FROM UTILISATEURS u
LEFT JOIN SALLES s ON s.ID = u.SALLE_ID
ORDER BY u.ID;
```

Après la démonstration, la base contient deux salles et trois utilisateurs, tous affectés à Atlas. Garder le terminal ouvert pour consulter les données, puis appuyer sur Entrée pour arrêter. Le port est fixé à **8082** : fermer toute autre console H2 sur ce port avant le lancement.

La base est en mémoire. `hibernate.hbm2ddl.auto=update` conserve le schéma pendant l'exécution, mais les données disparaissent à l'arrêt du processus Java.

## Tests

Les tests JUnit 4 utilisent une base H2 distincte par méthode, avec un schéma créé puis supprimé. Les lectures passent par un nouveau contexte de persistance afin de vérifier les données réellement enregistrées.

- Création, lecture, modification et suppression des deux entités.
- Réaffectation d'un utilisateur et relation facultative.
- Recherches par salle, capacité et nom.
- Unicité du nom de salle et de l'email.
- Annulation d'une modification invalide et poursuite après erreur.
- Protection des salles référencées et contraintes de champs obligatoires.

## Dépendances

JPA 2.2, Hibernate 5.6.15.Final, H2 2.1.214 et JUnit 4.13.2. Le projet utilise `hibernate-core`, qui contient le fournisseur JPA, et ajoute H2 au classpath de compilation pour démarrer sa console Web depuis Java.
