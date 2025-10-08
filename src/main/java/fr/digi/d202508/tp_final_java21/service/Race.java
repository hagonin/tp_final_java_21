package fr.digi.d202508.tp_final_java21.service;
import fr.digi.d202508.tp_final_java21.display.ConsoleDisplay;
import fr.digi.d202508.tp_final_java21.model.Animal;
import fr.digi.d202508.tp_final_java21.model.Cheval;
import fr.digi.d202508.tp_final_java21.model.Lapin;
import fr.digi.d202508.tp_final_java21.model.Tortue;

import java.util.ArrayList;
import java.util.List;

/**
 * Race class manages the overall race execution.
 * Coordinates animals, threads, and display.
 */
public class Race {

    private final RaceTrack raceTrack;
    private final List<Thread> animalThreads;
    private final ConsoleDisplay display;

    /**
     * Constructor initializes the race components
     */
    public Race() {
        this.raceTrack = new RaceTrack();
        this.animalThreads = new ArrayList<>();
        this.display = new ConsoleDisplay();
    }

    /**
     * Initializes the race by creating all animals and their threads
     */
    public void initializeRace() {
        // Create the three animals
        Animal tortue = new Tortue(raceTrack);
        Animal lapin = new Lapin(raceTrack);
        Animal cheval = new Cheval(raceTrack);

        // Add animals to race track
        raceTrack.addParticipant(tortue);
        raceTrack.addParticipant(lapin);
        raceTrack.addParticipant(cheval);

        // Create threads for each animal
        animalThreads.add(new Thread(tortue, "Thread-Tortue"));
        animalThreads.add(new Thread(lapin, "Thread-Lapin"));
        animalThreads.add(new Thread(cheval, "Thread-Cheval"));
    }

    public void displayStartingScreen() {
        display.drawStartingScreen();
    }

    public void startRace() {
        System.out.println("Départ de la course!");
        System.out.println();

        // Start all threads at the same time
        for (Thread thread : animalThreads) {
            thread.start();
        }
    }

    /**
     * Monitors the race until a winner is found.
     * Main thread waits and checks race status periodically.
     */
    public void monitorRace() {
        try {
            // Poll race status every 100ms
            while (!raceTrack.isRaceFinished()) {
                Thread.sleep(100);
            }

            // Race finished - stop all animal threads
            stopAllAnimals();

            // Small delay to ensure all threads have stopped
            Thread.sleep(200);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            display.displayError("La surveillance de la course a été interrompue");
        }
    }

    private void stopAllAnimals() {
        for (Thread thread : animalThreads) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }

        // Wait for all threads to finish
        for (Thread thread : animalThreads) {
            try {
                thread.join(1000); // Wait up to 1 second for each thread
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void displayResults() {
        Animal winner = raceTrack.getWinner();
        List<Animal> scoreboard = raceTrack.getFinalScoreboard();

        if (winner != null) {
            display.drawFinalResults(winner, scoreboard);
        } else {
            display.displayError("Aucun gagnant n'a été déclaré");
        }
    }

    /**
     * Gets the race track (useful for tournament mode)
     * @return the race track
     */
    public RaceTrack getRaceTrack() {
        return raceTrack;
    }
    /**
     * Runs the complete race from start to finish.
     * This is a convenience method that calls all race phases in order.
     */
    public void run() {
        try {
            initializeRace();
            displayStartingScreen();
            startRace();
            monitorRace();
            displayResults();
        } catch (Exception e) {
            display.displayError("Une erreur s'est produite pendant la course: " + e.getMessage());
            e.printStackTrace();
        }
    }
}