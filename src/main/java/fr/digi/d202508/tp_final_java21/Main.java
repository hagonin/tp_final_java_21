package fr.digi.d202508.tp_final_java21;

import fr.digi.d202508.tp_final_java21.service.Race;
import fr.digi.d202508.tp_final_java21.service.Tournament;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import java.util.Scanner;


@SpringBootApplication
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        displayWelcome();

        try {
            boolean running = true;

            while (running) {
                int choice = displayMenu();

                switch (choice) {
                    case 1 -> runSingleRace();
                    case 2 -> runTournament();
                    case 3 -> running = false;
                    default -> System.out.println("Choix invalide, veuillez réessayer.");
                }
            }

            System.out.println("\n Merci d'avoir joué! À bientôt!");

        } catch (Exception e) {
            System.err.println("Une erreur fatale s'est produite:");
            e.printStackTrace();
            System.exit(1);
        } finally {
            scanner.close();
        }
    }

    /**
     * Displays welcome message
     */
    private static void displayWelcome() {
        clearConsole();
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║                                                        ║");
        System.out.println("║            COURSE D'ANIMAUX - JAVA 21                  ║");
        System.out.println("║                                                        ║");
        System.out.println("║              Application Multi-Threading               ║");
        System.out.println("║                                                        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * Displays main menu and gets user choice
     * @return user's menu choice
     */
    private static int displayMenu() {
        System.out.println("\n═══════════════ MENU PRINCIPAL ═══════════════");
        System.out.println("  1. 🏃 Course simple (1 manche)");
        System.out.println("  2. 🏆 Mode tournoi (plusieurs manches)");
        System.out.println("  3. 🚪 Quitter");
        System.out.println("══════════════════════════════════════════════");
        System.out.print("\nVotre choix (1-3): ");

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Runs a single race
     */
    private static void runSingleRace() {
        System.out.println("\n Lancement d'une course simple...\n");

        Race race = new Race();
        race.run();

        System.out.println("\nAppuyez sur Entrée pour revenir au menu...");
        scanner.nextLine();
        clearConsole();
        displayWelcome();
    }

    /**
     * Runs a tournament with multiple rounds
     */
    private static void runTournament() {
        System.out.println("\n🏆 Configuration du tournoi\n");

        // Ask for number of rounds
        int rounds = getTournamentRounds();

        // Ask if user wants referee
        boolean useReferee = askForReferee();

        // Run tournament
        Tournament tournament = new Tournament(rounds, useReferee);
        tournament.runTournament();

        System.out.println("\nAppuyez sur Entrée pour revenir au menu...");
        scanner.nextLine();
        clearConsole();
        displayWelcome();
    }

    /**
     * Gets number of tournament rounds from user
     * @return number of rounds
     */
    private static int getTournamentRounds() {
        while (true) {
            System.out.print("Combien de manches voulez-vous jouer? (3-10): ");
            try {
                int rounds = Integer.parseInt(scanner.nextLine().trim());
                if (rounds >= 3 && rounds <= 10) {
                    return rounds;
                }
                System.out.println("Veuillez entrer un nombre entre 3 et 10.");
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre valide.");
            }
        }
    }

    /**
     * Asks user if they want live referee updates
     * @return true if user wants referee, false otherwise
     */
    private static boolean askForReferee() {
        System.out.print("Voulez-vous un arbitre avec classement en temps réel? (o/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("o") || response.equals("oui") || response.equals("y") || response.equals("yes");
    }

    /**
     * Clears the console
     */
    private static void clearConsole() {
        try {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        } catch (Exception e) {
            // If clearing fails, just print newlines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }}
}
