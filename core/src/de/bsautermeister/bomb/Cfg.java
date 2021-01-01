package de.bsautermeister.bomb;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Logger;

public interface Cfg {

    int LOG_LEVEL = Logger.DEBUG;
    boolean DEBUG_MODE = false;
    String SAVE_GAME_FILE = "save.bin";

    interface World {
        float GRAVITY = 9.81f;
        float PPM = 5f;
        float WIDTH_PPM = Ground.FRAGMENT_SIZE_PPM * Ground.FRAGMENTS_NUM_COLS;
        float VIEWPORT_HEIGHT_PPM = 41f / PPM;
        float VIEWPORT_WIDTH_PPM = 64f / PPM;
    }

    interface Ui {
        float WIDTH = 1280;
        float HEIGHT = 720;
    }

    interface Window {
        int WIDTH = 1280;
        int HEIGHT = 720;
    }

    interface Ground {
        float FRAGMENT_SIZE_PPM = 5f / World.PPM;
        int FRAGMENTS_NUM_COLS = 20;
        int FRAGMENTS_NUM_COMPLETE_ROWS = 6;
    }

    interface Player {
        float RADIUS_PPM = 1.25f / World.PPM;
        float SELF_HEALING_PER_SECOND = 0.015f;
        Vector2 START_POSITION = new Vector2(World.VIEWPORT_WIDTH_PPM, 5f / World.PPM);
    }

    interface Colors {
        Color DARK_RED = new Color(0.7f, 0f, 0f, 1f);
    }
}
