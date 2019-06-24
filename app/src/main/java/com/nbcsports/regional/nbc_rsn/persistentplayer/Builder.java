package com.nbcsports.regional.nbc_rsn.persistentplayer;

import androidx.fragment.app.FragmentManager;

import com.nbcsports.regional.nbc_rsn.MainContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Landscape;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Mini;
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils;

import com.nbcsports.regional.nbc_rsn.chromecast.Injection;

/**
 * Created by pengzhiquah on 2018-07-05.
 */
public class Builder {

    private FragmentManager supportFragmentManager;
    private int landscapeFragmentID;
    private int miniFragmentID;
    private PersistentPlayerContract.Main.View persistentPlayerContractView;
    private MainContract.View mainContractView;

    public Builder() {
    }

    public Builder setPersistentPlayerContract(PersistentPlayerContract.Main.View mainView) {
        this.persistentPlayerContractView = mainView;
        return this;
    }

    public Builder setMainActivityContract(MainContract.View mainContractView) {
        this.mainContractView = mainContractView;
        return this;
    }

    public Builder setFragmentManager(FragmentManager supportFragmentManager) {
        this.supportFragmentManager = supportFragmentManager;
        return this;
    }

    public Builder setLandscapeFragmentID(int viewResID) {
        landscapeFragmentID = viewResID;
        return this;
    }

    public Builder setMiniFragmentID(int viewResID) {
        miniFragmentID = viewResID;
        return this;
    }

    public PersistentPlayer create() {
        PersistentPlayerContract.View miniFragment = initMiniLayout();
        PersistentPlayerContract.View landscapeFragment = initLandscapeLayout();

        // new PersistentPlayer is a presenter between mainactivity and persistentplayerfragment,
        PersistentPlayer persistentPlayer = new PersistentPlayer(persistentPlayerContractView, mainContractView,
                landscapeFragment, landscapeFragmentID,
                miniFragment, miniFragmentID);

        // new PersistentPlayerPresenter is a presenter just for persistentPlayerFragment
        // for now only landscape fragment is using the presenter
        new PersistentPlayerPresenter(persistentPlayer, landscapeFragment);

        // set up chromecast helper for persistent player singleton
        if (persistentPlayer != null){
            persistentPlayer.setChromecastHelper(
                    Injection.provideChromecastHelper(persistentPlayerContractView, persistentPlayer));
        }

        return persistentPlayer;
    }

    private Mini initMiniLayout() {

        Mini mini = (Mini) supportFragmentManager
                .findFragmentById(miniFragmentID);

        if (mini == null) {

            mini = Mini.newInstance();

            // Haha, this is why onHidden is called!
            ActivityUtils.addThenHideFragmentToActivity(supportFragmentManager,
                    mini, miniFragmentID);
        }

        return mini;
    }

    private Landscape initLandscapeLayout() {

        Landscape landscape = (Landscape) supportFragmentManager
                .findFragmentById(landscapeFragmentID);

        if (landscape == null) {

            landscape = Landscape.newInstance();

            ActivityUtils.addThenHideFragmentToActivity(supportFragmentManager,
                    landscape, landscapeFragmentID);
        }

        return landscape;
    }
}
