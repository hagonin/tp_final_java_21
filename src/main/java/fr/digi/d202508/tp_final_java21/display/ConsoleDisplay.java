package fr.digi.d202508.tp_final_java21.display;

import fr.digi.d202508.tp_final_java21.model.Animal;
import fr.digi.d202508.tp_final_java21.service.RaceTrack;

import java.util.List;

/**
 * ConsoleDisplay handles all visual output to the console.
 * Manages race track rendering, clearing screen, and displaying results.
 */
public class ConsoleDisplay {

    private static final int TRACK_WIDTH = 50; // Visual width matching the 50 units

    /**
     * Clears the console screen using ANSI escape codes.
     */
    public void clearConsole() {
        try {
            // ANSI escape code to clear screen and move cursor to top-left
            System.out.print("\033[H\033[2J");
            System.out.flush();

            // Alternative for Windows (if ANSI doesn't work)
            // new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            // If clearing fails, just print newlines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    /**
     * Displays the race starting screen with countdown
     */
    public void drawStartingScreen() {
        clearConsole();
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║          BIENVENUE À LA COURSE D'ANIMAUX!              ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  Participants:");
        System.out.println("    - Tortue 🐢 : Vitesse 5-7 km/h");
        System.out.println("    - Lapin  🐰 : Vitesse 8-10 km/h");
        System.out.println("    - Cheval 🐴 : Vitesse 9-10 km/h");
        System.out.println();
        System.out.println("  Distance à parcourir: 50 unités");
        System.out.println();
        System.out.println("  La course va commencer dans...");

        // Countdown
        for (int i = 3; i > 0; i--) {
            System.out.println("    " + i + "...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("    GO! ");
        System.out.println();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Draws the current state of the race track
     * @param animals list of animals in the race
     */
    public void drawRaceTrack(List<Animal> animals) {
        clearConsole();

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║                  COURSE EN COURS...                   ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();

        // Draw start and finish markers
        System.out.print("START |");
        for (int i = 0; i < TRACK_WIDTH; i++) {
            System.out.print("-");
        }
        System.out.println("| FINISH");
        System.out.println();

        // Draw each animal's position
        for (Animal animal : animals) {
            drawAnimalPosition(animal);
        }

        System.out.println();
        System.out.println("Distance totale: 50 unités");
    }

    /**
     * Draws a single animal's position on the track
     * @param animal the animal to draw
     */
    private void drawAnimalPosition(Animal animal) {
        int visualPosition = (int) ((animal.getPosition() / RaceTrack.FINISH_LINE) * TRACK_WIDTH);

        if (visualPosition > TRACK_WIDTH) {
            visualPosition = TRACK_WIDTH;
        }

        StringBuilder trackLine = new StringBuilder();
        trackLine.append(String.format("%-8s: ", animal.getName()));

        for (int i = 0; i < visualPosition; i++) {
            trackLine.append(" ");
        }

        trackLine.append("[").append(animal.getDisplayChar()).append("]");

        for (int i = visualPosition; i < TRACK_WIDTH; i++) {
            trackLine.append(" ");
        }

        // Show current speed with indicator
        double currentSpeed = animal.getCurrentSpeed();
        double baseSpeed = animal.getSpeed();
        String speedIndicator = "";

        if (currentSpeed > baseSpeed * 1.2) {
            speedIndicator = "🚀"; // Speed boost
        } else if (currentSpeed < baseSpeed * 0.8) {
            speedIndicator = "😴"; // Fatigued
        } else {
            speedIndicator = "️🤖"; // Normal
        }

        trackLine.append(String.format(" %s %.1f/50 (%.1f km/h %s)",
                speedIndicator, animal.getPosition(), currentSpeed,
                currentSpeed > baseSpeed ? "⬆" : currentSpeed < baseSpeed ? "⬇︎️" : ""));

        System.out.println(trackLine);
    }

    /**
     * Displays the final results screen with winner and scoreboard
     * @param winner the winning animal
     * @param scoreboard sorted list of all animals
     */
    public void drawFinalResults(Animal winner, List<Animal> scoreboard) {
        clearConsole();

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║                  COURSE TERMINÉE!                      ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();

        // Winner announcement
        System.out.println("🏆 GAGNANT: " + winner.getName() + " 🏆");
        System.out.println("   Vitesse: " + String.format("%.2f km/h", winner.getSpeed()));
        System.out.println();

        // Final track visualization
        System.out.println("Position finale:");
        System.out.println();
        System.out.print("START |");
        for (int i = 0; i < TRACK_WIDTH; i++) {
            System.out.print("-");
        }
        System.out.println("| FINISH");
        System.out.println();

        for (Animal animal : scoreboard) {
            drawAnimalPosition(animal);
        }

        System.out.println();
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("CLASSEMENT FINAL:");
        System.out.println("─────────────────────────────────────────────────────────");

        // Scoreboard
        int position = 1;
        for (Animal animal : scoreboard) {
            String medal = switch (position) {
                case 1 -> "🥇";
                case 2 -> "🥈";
                case 3 -> "🥉";
                default -> "  ";
            };

            System.out.printf("%s %d. %-10s - Distance: %.2f unités (Vitesse: %.2f km/h)%n",
                    medal, position, animal.getName(), animal.getPosition(), animal.getSpeed());
            position++;
        }

        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println();
        System.out.println("Merci d'avoir participé à la course!");
    }

    /**
     * Displays an error message
     * @param message the error message to display
     */
    public void displayError(String message) {
        System.err.println("ERREUR: " + message);
    }
}