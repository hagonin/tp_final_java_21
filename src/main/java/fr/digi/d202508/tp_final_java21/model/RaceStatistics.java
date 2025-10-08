package fr.digi.d202508.tp_final_java21.model;

import java.util.*;

/**
 * Tracks statistics across multiple race rounds.
 * Uses Java Streams API for data analysis.
 */
public class RaceStatistics {

    private final Map<String, AnimalStats> animalStatistics;
    private int totalRounds;

    /**
     * Inner class to hold statistics for a single animal
     */
    public static class AnimalStats {
        private final String name;
        private int wins;
        private int secondPlace;
        private int thirdPlace;
        private double totalDistance;
        private double averageSpeed;
        private final List<Double> speeds;
        private final List<Integer> positions;

        public AnimalStats(String name) {
            this.name = name;
            this.wins = 0;
            this.secondPlace = 0;
            this.thirdPlace = 0;
            this.totalDistance = 0;
            this.averageSpeed = 0;
            this.speeds = new ArrayList<>();
            this.positions = new ArrayList<>();
        }

        public void addRaceResult(int position, double distance, double speed) {
            switch (position) {
                case 1 -> wins++;
                case 2 -> secondPlace++;
                case 3 -> thirdPlace++;
            }
            totalDistance += distance;
            speeds.add(speed);
            positions.add(position);
            averageSpeed = speeds.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }

        public double getWinRate() {
            int totalRaces = speeds.size();
            return totalRaces > 0 ? (double) wins / totalRaces * 100 : 0.0;
        }

        public double getAveragePosition() {
            return positions.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        }

        // Getters
        public String getName() { return name; }
        public int getWins() { return wins; }
        public int getSecondPlace() { return secondPlace; }
        public int getThirdPlace() { return thirdPlace; }
        public double getTotalDistance() { return totalDistance; }
        public double getAverageSpeed() { return averageSpeed; }
        public List<Double> getSpeeds() { return new ArrayList<>(speeds); }
    }

    /**
     * Constructor initializes the statistics tracker
     */
    public RaceStatistics() {
        this.animalStatistics = new HashMap<>();
        this.totalRounds = 0;
    }

    /**
     * Records the results of a race round
     * @param scoreboard sorted list of animals (winner first)
     */
    public void recordRound(List<Animal> scoreboard) {
        totalRounds++;

        for (int i = 0; i < scoreboard.size(); i++) {
            Animal animal = scoreboard.get(i);
            String name = animal.getName();

            animalStatistics.putIfAbsent(name, new AnimalStats(name));
            animalStatistics.get(name).addRaceResult(
                    i + 1,
                    animal.getPosition(),
                    animal.getSpeed()
            );
        }
    }

    /**
     * Returns statistics for a specific animal
     * @param animalName name of the animal
     * @return animal statistics or null if not found
     */
    public AnimalStats getAnimalStats(String animalName) {
        return animalStatistics.get(animalName);
    }

    /**
     * Returns all animal statistics sorted by wins
     * @return sorted list of animal statistics
     */
    public List<AnimalStats> getAllStatsSortedByWins() {
        return animalStatistics.values().stream()
                .sorted(Comparator.comparingInt(AnimalStats::getWins).reversed())
                .toList();
    }

    /**
     * Returns the animal with the best average speed using Streams API
     * @return optional containing the fastest animal stats
     */
    public Optional<AnimalStats> getFastestAnimalAverage() {
        return animalStatistics.values().stream()
                .max(Comparator.comparingDouble(AnimalStats::getAverageSpeed));
    }

    /**
     * Returns the most consistent performer (lowest position variance)
     * @return optional containing the most consistent animal
     */
    public Optional<AnimalStats> getMostConsistentAnimal() {
        return animalStatistics.values().stream()
                .min(Comparator.comparingDouble(stats ->
                        calculateVariance(stats.positions)));
    }

    /**
     * Calculates variance of a list of numbers
     * @param numbers list of numbers
     * @return variance
     */
    private double calculateVariance(List<Integer> numbers) {
        if (numbers.isEmpty()) return 0.0;

        double mean = numbers.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        return numbers.stream()
                .mapToDouble(n -> Math.pow(n - mean, 2))
                .average()
                .orElse(0.0);
    }

    /**
     * Gets total number of rounds played
     * @return total rounds
     */
    public int getTotalRounds() {
        return totalRounds;
    }

    /**
     * Displays comprehensive statistics report
     */
    public void displayStatisticsReport() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║           STATISTIQUES MULTI-MANCHES                     ║");
        System.out.println("╚════════════════════════════════════════════════════════  ╝");
        System.out.println("\nTotal de manches jouées: " + totalRounds);
        System.out.println("\n─────────────────────────────────────────────────────────");
        System.out.println("CLASSEMENT GÉNÉRAL (par victoires):");
        System.out.println("─────────────────────────────────────────────────────────");

        List<AnimalStats> sortedStats = getAllStatsSortedByWins();
        for (int i = 0; i < sortedStats.size(); i++) {
            AnimalStats stats = sortedStats.get(i);
            String medal = switch (i) {
                case 0 -> "🥇";
                case 1 -> "🥈";
                case 2 -> "🥉";
                default -> "  ";
            };

            System.out.printf("%s %-10s | Victoires: %2d | 2e place: %2d | 3e place: %2d%n",
                    medal, stats.getName(), stats.getWins(),
                    stats.getSecondPlace(), stats.getThirdPlace());
        }

        System.out.println("\n─────────────────────────────────────────────────────────");
        System.out.println("PERFORMANCES DÉTAILLÉES:");
        System.out.println("─────────────────────────────────────────────────────────");

        for (AnimalStats stats : sortedStats) {
            System.out.printf("\n%s:%n", stats.getName());
            System.out.printf("  • Taux de victoire: %.1f%%%n", stats.getWinRate());
            System.out.printf("  • Position moyenne: %.2f%n", stats.getAveragePosition());
            System.out.printf("  • Vitesse moyenne: %.2f km/h%n", stats.getAverageSpeed());
            System.out.printf("  • Distance totale: %.2f unités%n", stats.getTotalDistance());
        }

        // Stream API analyses
        System.out.println("\n─────────────────────────────────────────────────────────");
        System.out.println("ANALYSES AVANCÉES (Stream API):");
        System.out.println("─────────────────────────────────────────────────────────");

        getFastestAnimalAverage().ifPresent(stats ->
                System.out.printf("Animal le plus rapide (moyenne): %s (%.2f km/h)%n",
                        stats.getName(), stats.getAverageSpeed()));

        getMostConsistentAnimal().ifPresent(stats ->
                System.out.printf("Animal le plus régulier: %s (position moyenne: %.2f)%n",
                        stats.getName(), stats.getAveragePosition()));

        // Total distance comparison using Streams
        double totalDistanceAllAnimals = animalStatistics.values().stream()
                .mapToDouble(AnimalStats::getTotalDistance)
                .sum();
        System.out.printf("Distance totale parcourue (tous animaux): %.2f unités%n",
                totalDistanceAllAnimals);

        System.out.println("─────────────────────────────────────────────────────────\n");
    }
}