package com.nbcsports.regional.nbc_rsn.persistentplayer;

public class Injection {
    public static PersistentPlayer providePlayer(PersistentPlayerContract.Main.View view) {
        try {
            PersistentPlayerContract.Main.View mainActivityView = view;
            return mainActivityView.getPersistentPlayer();
        } catch (ClassCastException castException) {
            return null;
        }
    }
}
