package com.nbcsports.regional.nbc_rsn.persistentplayer.intent;

import androidx.core.view.GestureDetectorCompat;
import android.view.MotionEvent;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Mini;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.mini.MiniPlayerCloseListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.mini.MiniPlayerGestureListener;
import com.nbcsports.regional.nbc_rsn.utils.DataMenuUtils;

public class OnMiniDragListener implements View.OnTouchListener {

    private final GestureDetectorCompat gestureDetector;
    private final MiniPlayerGestureListener gestureListener;

    public OnMiniDragListener(Mini mini, MiniPlayerCloseListener closeListener ) {
        gestureListener = new MiniPlayerGestureListener(mini, closeListener);
        gestureDetector = new GestureDetectorCompat(mini.getContext(), gestureListener);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (DataMenuUtils.INSTANCE.getDATA_MENU_IS_OPENED()){
            return false;
        }
        gestureListener.setView(view);
        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP){
           gestureListener.onUp(event);
        }
        return true;
    }

    public void allowAlphaAnimation(boolean alphaAnimation) {
        gestureListener.allowAlphaAnimation = alphaAnimation;
    }
}
