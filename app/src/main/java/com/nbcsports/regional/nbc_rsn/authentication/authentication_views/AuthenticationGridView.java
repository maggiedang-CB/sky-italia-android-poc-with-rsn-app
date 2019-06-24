package com.nbcsports.regional.nbc_rsn.authentication.authentication_views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class AuthenticationGridView extends GridView {

    public AuthenticationGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AuthenticationGridView(Context context) {
        super(context);
    }

    public AuthenticationGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
