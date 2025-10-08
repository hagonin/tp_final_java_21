package fr.digi.d202508.tp_final_java21.service;

import fr.digi.d202508.tp_final_java21.model.Animal;

import java.util.Comparator;
import java.util.List;

/**
 * Referee thread that monitors and displays live rankings during the race.
 * Runs independently and updates the console with current positions periodically.
 */
public class Referee implements Runnable {

    private final RaceTrack raceTrack;
    private final long updateIntervalMs;
    private volatile boolean running;

    public Referee(RaceTrack raceTrack, long updateIntervalMs) {
        this.raceTrack = raceTrack;
        this.updateIntervalMs = updateIntervalMs;
        this.running = true;
    }

    @Override
    public void run() {
        try {
            // Wait a bit before first update
            Thread.sleep(1000);

            while (running && !raceTrack.isRaceFinished()) {
                displayLiveRankings();
                Thread.sleep(updateIntervalMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Displays current rankings without clearing the main race display
     */
    private void displayLiveRankings() {
        List<Animal> currentRankings = raceTrack.getParticipants().stream()
                .sorted(Comparator.comparingDouble(Animal::getPosition).reversed())
                .toList();

        System.out.println("\n📊 CLASSEMENT EN TEMPS RÉEL:");
        int rank = 1;
        for (Animal animal : currentRankings) {
            String progressBar = createProgressBar(animal);
            System.out.printf("   %d. %-8s %s %.1f/50 unités%n",
                    rank++, animal.getName(), progressBar, animal.getPosition());
        }
        System.out.println();
    }

    /**
     * Creates a visual progress bar for an animal
     * @param animal the animal to create bar for
     * @return progress bar string
     */
    private String createProgressBar(Animal animal) {
        int totalBars = 20;
        int filledBars = (int) ((animal.getPosition() / RaceTrack.FINISH_LINE) * totalBars);

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("]");
        return bar.toString();
    }

    /**
     * Stops the referee thread
     */
    public void stop() {
        running = false;
    }
}