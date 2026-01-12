🌐 [English](README.md) | [Deutsch](README.de.md) | **Français**

---

# CAGE Companion

Une application Android simple pour suivre l'âge de la canule (CAGE) avec intégration Nightscout.

## Fonctionnalités

- **Affichage CAGE**: Affiche l'âge actuel de la canule au format jours:heures:minutes
- **Code couleur**: Vert (OK), Jaune (Avertissement), Rouge (Critique) selon des seuils configurables
- **Intégration Nightscout**: Lit le CAGE depuis Nightscout et télécharge les nouveaux traitements Site Change
- **Widget écran d'accueil**: Widget compact affichant le CAGE avec code couleur
- **Notification persistante**: Statut toujours visible dans la barre de notifications avec couleur
- **Mises à jour automatiques**: L'application, le widget et la notification se mettent à jour chaque minute
- **Multilingue**: Allemand, Anglais et Français

## Captures d'écran

| Écran principal | Notification |
|-----------------|--------------|
| Grand affichage circulaire CAGE avec couleur | Notification persistante avec statut CAGE |

## Installation

1. Télécharger et installer l'APK depuis [Releases](../../releases)
2. Ouvrir les Paramètres et configurer l'URL Nightscout et le Secret API
3. Définir les seuils d'avertissement (par défaut: Jaune à 2 jours, Rouge à 2,5 jours)
4. L'application commencera automatiquement à suivre votre CAGE

## Utilisation

### Écran principal
- Grand affichage circulaire montrant le CAGE actuel avec code couleur
- **Icône Actualiser**: Récupérer manuellement le dernier CAGE depuis Nightscout
- **Icône Paramètres**: Ouvrir les paramètres
- **Bouton Canule changée**: Enregistrer un nouveau Site Change dans Nightscout

### Widget
- Ajouter le "Widget CAGE" à votre écran d'accueil
- Affiche le CAGE avec fond coloré
- Se met à jour automatiquement chaque minute
- Appuyer pour ouvrir l'application

### Notification
- La notification persistante affiche le CAGE actuel
- Colorée selon vos seuils
- Se met à jour automatiquement chaque minute

## Configuration

### Paramètres Nightscout
- **URL**: L'URL de votre instance Nightscout (ex: `https://votre-nightscout.herokuapp.com`)
- **Secret API**: Votre secret API ou token Nightscout pour l'accès lecture/écriture

### Paramètres des seuils
- **Jaune (Avertissement)**: Âge auquel l'affichage devient jaune (par défaut: 2 jours)
- **Rouge (Critique)**: Âge auquel l'affichage devient rouge (par défaut: 2 jours 12 heures)

## Prérequis

- Android 8.0 (API 26) ou supérieur
- Instance Nightscout avec accès API

## Compilation depuis les sources

```bash
git clone https://github.com/yourusername/CageCompanion.git
cd CageCompanion
./gradlew assembleDebug
```

## Stack technique

- Kotlin
- Jetpack Compose
- Client HTTP Ktor
- DataStore Preferences
- Widgets Glance
- Foreground Service

## Confidentialité

- Toutes les données sont stockées localement sur votre appareil
- Communique uniquement avec votre instance Nightscout personnelle
- Pas d'analytics ni de tracking

## Licence

Licence MIT - voir [LICENSE](LICENSE)

## Avertissement

Cette application n'est pas affiliée à la Nightscout Foundation ni à aucun fabricant d'appareils pour diabétiques. C'est une application compagnon indépendante pour la gestion personnelle du diabète. Consultez toujours votre professionnel de santé pour les décisions médicales.

---

Créé avec soin pour la communauté diabétique.
