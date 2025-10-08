package fr.digi.d202508.tp_final_java21.model;

import fr.digi.d202508.tp_final_java21.service.RaceTrack;

/**
 * Lapin (Rabbit) - Medium speed animal
 * Speed range: 8-10 km/h
 */
public final class Lapin extends Animal {

    private static final double MIN_SPEED = 7.0; // km/h (can be slow)
    private static final double MAX_SPEED = 11.0; // km/h (can be very fast!)
    private static final double STAMINA = 0.5; // Medium-low stamina (50%)

    public Lapin(RaceTrack raceTrack) {
        super("Lapin", raceTrack);
    }

    @Override
    protected double[] getSpeedRange() {
        return new double[]{MIN_SPEED, MAX_SPEED};
    }

    @Override
    public String getDisplayChar() {
        return "🐇";
    }
    @Override
    protected double getStamina() {
        return STAMINA; // Low stamina = more fatigue
    }
}
