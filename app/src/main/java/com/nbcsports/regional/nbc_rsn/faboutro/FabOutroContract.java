package com.nbcsports.regional.nbc_rsn.faboutro;

public interface FabOutroContract {

    interface View {

        void setFabOutroPresenter(Presenter presenter);

        void requestAllViews();

        void onFabOutroShownAlready();

    }

    interface Presenter {

        void setUpAppropriateViews(android.view.View... views);

        void setUpBackgroundAnimationsAndPlay();

        void clearAllViewsAnimations();

        void setUpTextViewWithLocalizedText(android.view.View... views);

    }

}
