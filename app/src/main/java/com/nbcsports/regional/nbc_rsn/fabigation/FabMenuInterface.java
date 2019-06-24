package com.nbcsports.regional.nbc_rsn.fabigation;

import com.nbcsports.regional.nbc_rsn.common.Team;

/**
 * Created by justin on 2018-03-23.
 */

public interface FabMenuInterface {
    void enterMenu();

    void exitMenu(FabMenuAdapter.FabCardType cardType);

    void tapFab(boolean isTapFab);

    void flingLeft();

    void flingRight();

    void switchFabLogo(Team team);

    void setSlideIndicatorVisibleWithAnimation(boolean show);
}
