package fr.digi.d202508.tp_final_java21.service;
import fr.digi.d202508.tp_final_java21.display.ConsoleDisplay;
import fr.digi.d202508.tp_final_java21.model.Animal;
import fr.digi.d202508.tp_final_java21.model.RaceStatistics;

import java.util.List;
import java.util.Scanner;

/**
 * Tournament manages multiple rounds of races and tracks overall statistics.
 */
public class Tournament {

    private final RaceStatistics statistics;
    private final ConsoleDisplay display;
    private final Scanner scanner;
    private int currentRound;
    private final int totalRounds;
    private final boolean useReferee;

    /**
     * Constructor for Tournament
     * @param totalRounds number of rounds to play
     * @param useReferee whether to use live referee updates
     */
    public Tournament(int totalRounds, boolean useReferee) {
        this.statistics = new RaceStatistics();
        this.display = new ConsoleDisplay();
        this.scanner = new Scanner(System.in);
        this.currentRound = 0;
        this.totalRounds = totalRounds;
        this.useReferee = useReferee;
    }

    /**
     * Runs the complete tournament
     */
    public void runTournament() {
        displayTournamentIntro();

        for (int round = 1; round <= totalRounds; round++) {
            currentRound = round;

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.printf(   "║              MANCHE %d / %d                            ║%n",
                    round, totalRounds);
            System.out.println(  "╚════════════════════════════════════════════════════════╝");

            // Create and run a single race
            Race race = new Race();
            race.initializeRace();

            if (round > 1) {
                System.out.println("\nAppuyez sur Entrée pour commencer la manche " + round + "...");
                scanner.nextLine();
            }

            race.displayStartingScreen();
            race.startRace();

            // Optional: Start referee thread for this race
            Thread refereeThread = null;
            Referee referee = null;
            if (useReferee) {
                referee = new Referee(race.getRaceTrack(), 2000);
                refereeThread = new Thread(referee, "Thread-Referee");
                refereeThread.start();
            }

            race.monitorRace();

            // Stop referee if running
            if (referee != null) {
                referee.stop();
                if (refereeThread != null && refereeThread.isAlive()) {
                    refereeThread.interrupt();
                }
            }

            race.displayResults();

            // Record results
            List<Animal> scoreboard = race.getRaceTrack().getFinalScoreboard();
            statistics.recordRound(scoreboard);

            // Show interim standings if not last round
            if (round < totalRounds) {
                displayInterimStandings();
            }
        }

        // Display final tournament statistics
        displayFinalTournamentResults();
    }

    /**
     * Displays tournament introduction
     */
    private void displayTournamentIntro() {
        display.clearConsole();
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║              MODE TOURNOI MULTI-MANCHES                ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  Configuration du tournoi:");
        System.out.println("    - Nombre de manches: " + totalRounds);
        System.out.println("    - Participants: Tortue, Lapin, Cheval");
        System.out.println("    - Arbitre en direct: " + (useReferee ? "Oui" : "Non"));
        System.out.println();
        System.out.println("  Le gagnant sera déterminé par le nombre de victoires!");
        System.out.println();
        System.out.println("Appuyez sur Entrée pour commencer le tournoi...");
        scanner.nextLine();
    }

    /**
     * Displays interim standings between rounds
     */
    private void displayInterimStandings() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║              CLASSEMENT PROVISOIRE                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        List<RaceStatistics.AnimalStats> standings = statistics.getAllStatsSortedByWins();
        System.out.println("\nAprès " + currentRound + " manche(s):");

        for (int i = 0; i < standings.size(); i++) {
            RaceStatistics.AnimalStats stats = standings.get(i);
            String medal = switch (i) {
                case 0 -> "🥇";
                case 1 -> "🥈";
                case 2 -> "🥉";
                default -> "  ";
            };

            System.out.printf("%s %-10s - Victoires: %d (Taux: %.1f%%)%n",
                    medal, stats.getName(), stats.getWins(), stats.getWinRate());
        }
    }

    /**
     * Displays final tournament results with comprehensive statistics
     */
    private void displayFinalTournamentResults() {
        display.clearConsole();
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║            RÉSULTATS FINAUX DU TOURNOI                 ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");

        // Display comprehensive statistics
        statistics.displayStatisticsReport();

        // Declare overall tournament winner
        List<RaceStatistics.AnimalStats> finalStandings = statistics.getAllStatsSortedByWins();
        if (!finalStandings.isEmpty()) {
            RaceStatistics.AnimalStats champion = finalStandings.get(0);
            System.out.println("\n🏆 CHAMPION DU TOURNOI: " + champion.getName() + " 🏆");
            System.out.printf("   Victoires: %d sur %d manches (%.1f%% de réussite)%n",
                    champion.getWins(), totalRounds, champion.getWinRate());
        }

        System.out.println("\n\nMerci d'avoir participé au tournoi! ");
    }
}
