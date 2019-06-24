package com.nbcsports.regional.nbc_rsn.persistentplayer;

public class PlayerConstants {
    public static final int MINI_PLAYER_ANIMATION_DURATION = 500;
    public static final int MINI_PLAYER_ANIMATION_DURATION_SHORT = 200;

    public enum State {
        SHOWING_NOT_AUTHENTICATED,
        SHOWING_AUTHENTICATED,
        NOT_SHOWING
    }

    public enum Type {
        MEDIUM, LANDSCAPE, MINI, _247
    }

    public enum Save {
        REMEMBER, ONE_TIME_ONLY
    }

    public static class Controller {
        public enum Mode {
            AUTO_HIDE, STICKY
        }
    }

    public static class PlayerEngine {
        public enum Config {
           ALPHA_ANIMATION
        }

        public enum Type {
            EXO, PT
        }
    }

    public static final String BOTTOM_SHEET = "persistent.player.botom.sheet";

}
