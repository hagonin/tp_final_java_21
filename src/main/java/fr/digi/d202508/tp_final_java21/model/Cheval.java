package fr.digi.d202508.tp_final_java21.model;

import fr.digi.d202508.tp_final_java21.service.RaceTrack;


/**
 * Cheval (Horse) - The fastest animal in the race
 * Speed range: 9-10 km/h
 */
public final class Cheval extends Animal {

    private static final double MIN_SPEED = 8.0; // km/h (reduced from 9)
    private static final double MAX_SPEED = 10.0; // km/h (same)
    private static final double STAMINA = 0.7; // Good stamina (70%)

    public Cheval(RaceTrack raceTrack) {
        super("Cheval", raceTrack);
    }

    @Override
    protected double[] getSpeedRange() {
        return new double[]{MIN_SPEED, MAX_SPEED};
    }

    @Override
    public String getDisplayChar() {
        return "🐎";
    }

    @Override
    protected double getStamina() {
        return STAMINA; // Good stamina
    }
}
