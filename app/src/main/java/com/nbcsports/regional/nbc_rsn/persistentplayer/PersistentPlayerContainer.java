package com.nbcsports.regional.nbc_rsn.persistentplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.nbcsports.regional.nbc_rsn.MainContract;
import com.nbcsports.regional.nbc_rsn.MainPresenter;
import com.nbcsports.regional.nbc_rsn.R;

public class PersistentPlayerContainer extends FrameLayout {

    private MainContract.View mainView;
    private int mediumHeight;

    public PersistentPlayerContainer(Context context) {
        super(context);
        init(null, 0);
    }

    public PersistentPlayerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PersistentPlayerContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PersistentPlayerContainer, defStyle, 0);

        mainView = MainPresenter.Injection.provideView(getContext());
    }

//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        ((Activity) getContext()).getWindowManager()
//                .getDefaultDisplay()
//                .getMetrics(displayMetrics);
//
//        int width = displayMetrics.widthPixels;
//        mediumHeight = width * 9 / 16;
//
//        ViewGroup.LayoutParams params = getLayoutParams();
//        params.height = mediumHeight;
//        requestLayout();
//    }

//    @Override
//    protected void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if (newConfig.orientation == ORIENTATION_LANDSCAPE ){
//
//            DisplayMetrics displayMetrics = new DisplayMetrics();
//            ((Activity) getContext()).getWindowManager()
//                    .getDefaultDisplay()
//                    .getMetrics(displayMetrics);
//
//            int height = displayMetrics.heightPixels;
//            int width = displayMetrics.widthPixels;
//
//            ViewGroup.LayoutParams params = getLayoutParams();
//            params.height = height;
//            requestLayout();
//
//            if ( mainView != null ) {
//                mainView.hideFab();
//            }
//
//            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//
//        } else {
//
//            ViewGroup.LayoutParams params = getLayoutParams();
//            params.height = mediumHeight;
//            requestLayout();
//
//            if ( mainView != null ) {
//                mainView.showFab();
//            }
//
//            setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
//
//        }
//    }
}
