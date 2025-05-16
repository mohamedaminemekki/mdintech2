# 🏙️ MdinTech (JavaFX Edition)

**MdinTech** est une application Smart City développée en JavaFX, conçue pour moderniser l'interaction entre les citoyens et les services de la ville. Elle offre une interface desktop intuitive permettant aux utilisateurs d'accéder aux services essentiels comme la santé, le transport, le commerce, les réclamations et les actualités.

---

## 🎯 Fonctionnalités

- Authentification sécurisée avec:
  - Vérification par email
  - Google OAuth2 integration
  - Gestion de mot de passe sécurisée
- Gestion intelligente des utilisateurs et profils
- Module Hôpital:
  - Prise de rendez-vous
  - Agenda médical
  - Notifications par email
- Module Transport:
  - Gestion des trajets
  - Réservations
  - Système de points de fidélité
- Module Market:
  - Paiement sécurisé
  - Génération de factures
  - Gestion des commandes
- Blog avec modération de contenu
- Système de réclamations avec suivi
- Fonctionnalités globales:
  - Interface moderne et responsive
  - Recherche avancée
  - Filtres dynamiques
  - Statistiques en temps réel
  - Notifications système

---

## 💻 Prérequis

- [Java JDK 17+](https://www.oracle.com/java/technologies/downloads/)
- [JavaFX SDK](https://openjfx.io/)
- [Scene Builder](https://gluonhq.com/products/scene-builder/)
- [MySQL ≥ 8.0](https://dev.mysql.com/downloads/mysql/)
- [Maven](https://maven.apache.org/download.cgi) ou [Gradle](https://gradle.org/install/)

---

## 🚀 Installation

```bash
# Cloner le projet
git clone https://github.com/MdinTech/smartcity-desktop.git
cd mdintech

# Si vous utilisez Maven
mvn clean install
mvn javafx:run

# Si vous utilisez Gradle
gradle build
gradle run
```

Configuration:
1. Copiez `config.properties.example` vers `config.properties`
2. Configurez votre base de données et vos clés API
3. Lancez l'application

---

## 🔗 Technologies utilisées

| Technologie                | Description                                          |
|---------------------------|------------------------------------------------------|
| JavaFX 17                 | Framework UI principal                               |
| FXML                      | Définition des interfaces utilisateur                |
| CSS                       | Stylisation moderne des interfaces                   |
| MySQL Connector           | Connexion à la base de données                       |
| JavaMail API             | Envoi d'emails et notifications                      |
| Google OAuth2 Client      | Authentification via Google                          |
| JFoenix                  | Composants Material Design                           |
| Apache PDFBox            | Génération de documents PDF                          |
| Chart.js                 | Visualisation de données statistiques                |
| Hibernate                | Persistence et mapping objet-relationnel             |

---

## 👥 Membres du Projet

| Nom         | Modules & Contributions                                           |
|-------------|------------------------------------------------------------------|
| **Tasnim**  | Module Market: Interface de vente, gestion des produits           |
| **Mariem**  | Module Transport: Réservations, système de points                 |
| **Ines**    | Module Hôpital: Rendez-vous, dossiers médicaux                   |
| **Rahim**   | Module Blog: Interface et modération                             |
| **Amine**   | Module Utilisateur: Authentification, sécurité, profils          |
| **Mohamed** | Module Réclamations: Suivi et traitement                         |
| **Tous**    | UI/UX, tests, documentation                                      |

---

## 😄 Contribution

1. Fork le projet
2. Créez votre branche (`git checkout -b feature/nouvelle-fonctionnalite`)
3. Committez vos changements (`git commit -m 'Ajout d'une nouvelle fonctionnalité'`)
4. Push vers la branche (`git push origin feature/nouvelle-fonctionnalite`)
5. Ouvrez une Pull Request

---

## 📄 Licence

Projet académique réalisé dans le cadre des études à ESPRIT (École Supérieure Privée d'Ingénierie et de Technologies), promotion 2025.  
Usage strictement pédagogique.  
Voir [LICENSE.md](LICENSE.md) pour plus d'informations. 