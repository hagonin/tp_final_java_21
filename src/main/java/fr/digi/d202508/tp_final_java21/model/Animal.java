package fr.digi.d202508.tp_final_java21.model;

import fr.digi.d202508.tp_final_java21.service.RaceTrack;

import java.util.Random;

public sealed abstract class Animal implements Runnable permits Tortue, Lapin, Cheval {

    private final String name;
    private double position; // Current position in units (0 to 50)
    private double baseSpeed; // Base speed in km/h
    private double currentSpeed; // Current speed (can vary)
    private boolean finished;
    private final RaceTrack raceTrack;
    private int moveCount; // Track number of moves for fatigue calculation

    // Random generator for speed variation
    private static final Random random = new Random();

    // Conversion factor: 1 unit = 1 meter
    private static final double CONVERSION_FACTOR = 1.0;

    // Speed variation constants
    private static final double SPEED_BOOST_PROBABILITY = 0.15; // 15% chance of boost
    private static final double SPEED_FATIGUE_PROBABILITY = 0.20; // 20% chance of slowdown
    private static final double BOOST_MULTIPLIER = 1.5; // 50% speed increase
    private static final double FATIGUE_MULTIPLIER = 0.7; // 30% speed decrease

    protected Animal(String name, RaceTrack raceTrack) {
        this.name = name;
        this.position = 0.0;
        this.finished = false;
        this.raceTrack = raceTrack;
        this.moveCount = 0;

        // Generate random speed within the animal's speed range
        double[] speedRange = getSpeedRange();
        this.baseSpeed = speedRange[0] + (speedRange[1] - speedRange[0]) * random.nextDouble();
        this.currentSpeed = baseSpeed;
    }

    /**
     * Abstract method to define speed range for each animal type
     * @return array with [minSpeed, maxSpeed] in km/h
     */
    protected abstract double[] getSpeedRange();

    /**
     * Abstract method to get the character representation for display
     * @return character or emoji representing the animal
     */
    public abstract String getDisplayChar();

    /**
     * Abstract method to get stamina characteristics
     * Higher stamina = less affected by fatigue
     * @return stamina value (0.0 to 1.0)
     */
    protected abstract double getStamina();

    /**
     * Calculates current speed with random variations
     * Simulates bursts of energy and moments of fatigue
     */
    private void updateCurrentSpeed() {
        double speedMultiplier = 1.0;
        double rand = random.nextDouble();

        // Apply stamina factor (better stamina = less fatigue effect)
        double staminaFactor = getStamina();
        double adjustedFatigueProbability = SPEED_FATIGUE_PROBABILITY * (1 - staminaFactor * 0.5);

        if (rand < SPEED_BOOST_PROBABILITY) {
            // Speed boost! (burst of energy)
            speedMultiplier = BOOST_MULTIPLIER;
        } else if (rand < SPEED_BOOST_PROBABILITY + adjustedFatigueProbability) {
            // Fatigue/slowdown
            speedMultiplier = FATIGUE_MULTIPLIER;
        }
        // else: normal speed
        currentSpeed = baseSpeed * speedMultiplier;
    }

    /**
     * Calculates distance moved based on speed and time interval
     * Formula: distance = speed * time
     * Speed is in km/h, converted to units/second
     *
     * @param speedKmh speed in kilometers per hour
     * @param timeSeconds time interval in seconds
     * @return distance moved in units
     */
    private double calculateDistance(double speedKmh, double timeSeconds) {
        // Convert km/h to m/s: speed * 1000 / 3600
        // Then multiply by time to get distance
        double metersPerSecond = speedKmh * 1000.0 / 3600.0;
        return metersPerSecond * timeSeconds * CONVERSION_FACTOR;
    }

    /**
     * Main run method for the thread.
     * Animal moves at regular intervals until race finishes or it reaches the finish line.
     */
    @Override
    public void run() {
        try {
            while (!raceTrack.isRaceFinished() && !finished) {
                // Update speed with random variation
                updateCurrentSpeed();

                // Calculate distance moved in 500ms interval
                double distanceMoved = calculateDistance(currentSpeed, RaceTrack.SLEEP_INTERVAL_MS / 1000.0);
                position += distanceMoved;
                moveCount++;

                // Check if animal reached finish line
                if (position >= RaceTrack.FINISH_LINE) {
                    position = RaceTrack.FINISH_LINE;
                    finished = true;

                    // Attempt to declare victory
                    if (raceTrack.declareWinner(this)) {
                        // This animal won!
                    }
                }

                // Sleep for the specified interval
                Thread.sleep(RaceTrack.SLEEP_INTERVAL_MS);
            }
        } catch (InterruptedException e) {
            // Thread was interrupted (race ended by another winner)
            Thread.currentThread().interrupt();
        }
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getPosition() {
        return position;
    }

    public double getSpeed() {
        return baseSpeed;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public String toString() {
        return String.format("%s (%.2f km/h)", name, baseSpeed);
    }
}