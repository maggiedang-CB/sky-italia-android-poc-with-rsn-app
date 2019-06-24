package com.nbcsports.regional.nbc_rsn.fabigation;

import android.content.Context;
import android.graphics.Canvas;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.View;

public class CloneView extends CardView {
    private View originalCard;

    public CloneView(Context context) {
        super(context);
    }

    public CloneView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CloneView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public View getOriginalCard() {
        return originalCard;
    }

    public void setSource(View source) {
        originalCard = source;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (originalCard != null) {
            //Position the card at the center of the page
            canvas.translate((canvas.getWidth() - originalCard.getWidth()) / 2,(canvas.getHeight() - originalCard.getHeight()) / 2);
            originalCard.draw(canvas);
        }
    }
}
