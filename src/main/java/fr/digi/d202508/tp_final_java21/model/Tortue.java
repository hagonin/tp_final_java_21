package fr.digi.d202508.tp_final_java21.model;

import fr.digi.d202508.tp_final_java21.service.RaceTrack;


public final class Tortue extends Animal {

    private static final double MIN_SPEED = 6.0; // km/h (increased from 5)
    private static final double MAX_SPEED = 8.0; // km/h (increased from 7)
    private static final double STAMINA = 0.9; // Very high stamina (90%)

    public Tortue(RaceTrack raceTrack) {
        super("Tortue", raceTrack);
    }

    @Override
    protected double[] getSpeedRange() {
        return new double[]{MIN_SPEED, MAX_SPEED};
    }

    @Override
    public String getDisplayChar() {
        return "🐢";
    }

    @Override
    protected double getStamina() {
        return STAMINA; // High stamina = less fatigue
    }
}