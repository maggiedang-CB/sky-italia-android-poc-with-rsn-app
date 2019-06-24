package com.nbcsports.regional.nbc_rsn.chromecast;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.utils.NBCSystemUtils;

import static com.nbcsports.regional.nbc_rsn.authentication.Injection.provideStreamAuthentication;

public class Injection {
        public static IChromecastHelper provideChromecastHelper(PersistentPlayerContract.Main.View view, PersistentPlayer persistentPlayer) {
            if (!(view instanceof MainActivity)) throw new RuntimeException("MainActivity must implement PersistentPlayerContract.Main.View");
            MainActivity mainActivity = (MainActivity) view;
            if (NBCSystemUtils.INSTANCE.getPLAY_SERVICES_AVAILABLE()) {
                StreamAuthenticationContract.Presenter streamAuthentication = provideStreamAuthentication(mainActivity);
                return new ChromecastHelper(mainActivity, persistentPlayer, streamAuthentication);
            } else {
                return new NoChromecastHelper();
            }
        }
}

