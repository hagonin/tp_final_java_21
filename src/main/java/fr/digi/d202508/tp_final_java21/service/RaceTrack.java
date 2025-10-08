package fr.digi.d202508.tp_final_java21.service;

import fr.digi.d202508.tp_final_java21.model.Animal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * RaceTrack manages the shared state of the race.
 * Handles winner declaration and race status using thread-safe mechanisms.
 */
public class RaceTrack {

    // Constants
    public static final int FINISH_LINE = 50; // Distance in units
    public static final int SLEEP_INTERVAL_MS = 500; // Sleep time between moves

    // Shared state (volatile for visibility across threads)
    private volatile boolean raceFinished = false;
    private volatile Animal winner = null;

    // List of all participants
    private final List<Animal> participants;

    // Lock object for synchronization
    private final Object winnerLock = new Object();

    public RaceTrack() {
        this.participants = new ArrayList<>();
    }

    public void addParticipant(Animal animal) {
        participants.add(animal);
    }

    public List<Animal> getParticipants() {
        return new ArrayList<>(participants);
    }

    /**
     * Thread-safe method to declare the winner.
     * Only the first animal to call this method will be declared winner.
     * Uses synchronized block to prevent race conditions.
     *
     * @param animal the animal attempting to declare victory
     * @return true if this animal won, false if another animal already won
     */
    public boolean declareWinner(Animal animal) {
        synchronized (winnerLock) {
            if (!raceFinished) {
                raceFinished = true;
                winner = animal;
                return true;
            }
            return false;
        }
    }

    public boolean isRaceFinished() {
        return raceFinished;
    }

    public Animal getWinner() {
        return winner;
    }

    public List<Animal> getFinalScoreboard() {
        List<Animal> scoreboard = new ArrayList<>(participants);
        // Sort by position in descending order
        scoreboard.sort(Comparator.comparingDouble(Animal::getPosition).reversed());
        return scoreboard;
    }

    public int getFinishLine() {
        return FINISH_LINE;
    }
}
