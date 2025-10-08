# Animal Race - Multi-Threading Java 21 Application

##  Table des Matières
- [Vue d'ensemble](#vue-densemble)
- [Fonctionnalités](#fonctionnalités)
- [Architecture Technique](#architecture-technique)
- [Choix de Conception](#choix-de-conception)


---

##  Vue d'ensemble

**Animal Race** est une application console Java 21 qui simule une course entre trois animaux (Tortue, Lapin, Cheval) en utilisant le multi-threading. Chaque animal court de manière indépendante dans son propre thread, créant une simulation réaliste et concurrentielle.

### Objectifs Pédagogiques
- Maîtriser le **multi-threading** en Java
- Comprendre la **synchronisation** et les **ressources partagées**
- Utiliser les **sealed classes** (Java 21)
- Manipuler l'**API Stream** pour l'analyse de données
- Gérer les **exceptions** et **interruptions de threads**
- Créer une **interface graphique JavaFX**
- Implémenter un **système de variations dynamiques** pour l'équilibrage

---

##  Fonctionnalités

### Mode Course Simple
-  Course unique entre 3 animaux : tortue, lapin, cheval avec caractéristiques uniques
- Variations de vitesse dynamiques (boost, fatigue)
- Système de stamina influençant la fatigue
-  Affichage en temps réel de la progression
-  Déclaration automatique du gagnant
-  Scoreboard final avec positions

### Mode Tournoi (BONUS)
-  Multiples manches (3-10 configurable)
-  Statistiques cumulatives
-  Classement général par victoires
-  Analyses avancées (Stream API)

### Arbitre en Temps Réel (BONUS)
-  Thread dédié à l'affichage des classements live
-  Mise à jour toutes les 2 secondes
-  Barres de progression visuelles

### Statistiques Avancées (BONUS)
-  Taux de victoire par animal
-  Vitesse moyenne sur toutes les manches
-  Animal le plus régulier (variance minimale)
-  Distance totale parcourue
-  Analyses avec Java Streams API

### Interface Graphique JavaFX (BONUS)
-  Barres de progression animées en temps réel
-  Indicateurs de vitesse visuels (boost , normal , fatigue)
-  Cercles colorés indiquant l'état (vert/bleu/rouge)
-  Graphiques statistiques interactifs
-  Mode console ET mode graphique
---

##  Architecture Technique

### Diagramme de Classes

```
┌─────────────────────────────────────────┐
│          <<sealed abstract>>            │
│              Animal                     │
│           implements Runnable           │
├─────────────────────────────────────────┤
│ - name: String (final)                  │
│ - position: double                      │
│ - baseSpeed: double                     │
│ - currentSpeed: double                  │
│ - finished: boolean                     │
│ - raceTrack: RaceTrack (final)          │
│ - moveCount: int                        │
├─────────────────────────────────────────┤
│ + run(): void                           │
│ # getSpeedRange(): double[]             │
│ # getStamina(): double                  │
│ # getDisplayChar(): char                │
│ - updateCurrentSpeed(): void            │
│ - calculateDistance(): double           │
└─────────────────────────────────────────┘
           △
           │ permits
    ┌──────┴──────────┬─────────┐
    │                 │         │
┌───┴────┐      ┌─────┴───┐  ┌──┴─────┐
│ Tortue │      │  Lapin  │  │ Cheval │
│ 6-8    │      │ 7-11    │  │ 8-10   │
│Stamina │      │Stamina  │  │Stamina │
│  90%   │      │  50%    │  │  70%   │
└────────┘      └─────────┘  └────────┘
```

### Architecture Multi-Threading

```
Main / GUI Application
    │
    ├─► Race Controller
    │      │
    │      ├─► Thread-Tortue (6-8 km/h, 90% stamina)
    │      │      └─► Animal.run() → updateCurrentSpeed()
    │      │
    │      ├─► Thread-Lapin (7-11 km/h, 50% stamina)
    │      │      └─► Animal.run() → updateCurrentSpeed()
    │      │
    │      ├─► Thread-Cheval (8-10 km/h, 70% stamina)
    │      │      └─► Animal.run() → updateCurrentSpeed()
    │      │
    │      └─► Thread-Referee (optionnel)
    │             └─► Referee.run() → Live rankings
    │
    └─► RaceTrack (État Partagé Thread-Safe)
           │
           └─► synchronized(winnerLock) → declareWinner()
```

---

##  Choix de Conception

### 1. Sealed Classes (Java 21)

 Seuls 3 types d'animaux (Tortue, Lapin et Cheval) sont autorisés dans ce système.
 Personne ne peut créer un Lion ou Elephant sans modifier la classe de base

```java
public sealed abstract class Animal 
    implements Runnable 
    permits Tortue, Lapin, Cheval {
    // ...
}
```

---

### 2. Pattern Runnable vs Callable

- Pas besoin de valeur de retour - le gagnant est déclaré via un mécanisme partagé (RaceTrack)
- Simplicité de l'API Thread(Runnable)
- Gestion d'exceptions plus directe

```java
@Override
public void run() {
    try {
        while (!raceTrack.isRaceFinished() && !finished) {
            // Logique de mouvement
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

---

### 3. Synchronisation: synchronized blocks
Un seul animal peut déclarer la victoire

```java
public boolean declareWinner(Animal animal) {
    synchronized (winnerLock) {
        if (!raceFinished) {  // Double-check locking
            raceFinished = true;
            winner = animal;
            return true;
        }
        return false;
    }
}
```

---

### 4. Formule de Calcul de Distance

```java
private double calculateDistance(double speedKmh, double timeSeconds) {
    // km/h → m/s: speed * 1000 / 3600
    double metersPerSecond = speedKmh * 1000.0 / 3600.0;
    return metersPerSecond * timeSeconds * CONVERSION_FACTOR;
}
```

**Logique:**
- 1 unité = 1 mètre
- Vitesse: km/h → m/s → unités/intervalle
- Intervalle: 500ms = 0.5 seconde
- Distance = vitesse × temps

**Exemple:**
- Tortue à 6 km/h = 6000 m/h = 1.67 m/s
- En 0.5s → 0.83 mètres parcourus

---
### 5. Système de Stamina (Endurance)
Chaque animal a une stamina influençant sa vitesse et définit son propre niveau d'endurance (0.0 à 1.0).
Dans la vraie vie, différentes espèces ont des capacités d'endurance différentes. La Tortue lente mais endurante peut battre le lapin rapide mais fatigable.
##### Formule: adjustedFatigueProbability = SPEED_FATIGUE_PROBABILITY * (1 - staminaFactor * 0.5)

- Haute stamina (0.9) → Fatigue divisée par 2
- Basse stamina (0.5) → Fatigue presque au maximum
