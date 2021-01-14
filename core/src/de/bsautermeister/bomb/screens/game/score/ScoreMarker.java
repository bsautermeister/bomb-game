package de.bsautermeister.bomb.screens.game.score;

public class ScoreMarker {
    private static final float TOTAL_EXPIRY_TIME = 0.5f;

    private final int score;
    private final String label;
    private float ttl;

    public ScoreMarker(int score, String label) {
        this.score = score;
        this.label = label;
        this.ttl = TOTAL_EXPIRY_TIME;
    }

    public void update(float delta, int currentScore) {
        if (currentScore >= score) {
            ttl -= delta;
        }
    }

    public int getScore() {
        return score;
    }

    public String getLabel() {
        return label;
    }

    public float inverseProgress() {
        return Math.max(0f, ttl / TOTAL_EXPIRY_TIME);
    }

    public boolean isExpired() {
        return ttl <= 0f;
    }
}