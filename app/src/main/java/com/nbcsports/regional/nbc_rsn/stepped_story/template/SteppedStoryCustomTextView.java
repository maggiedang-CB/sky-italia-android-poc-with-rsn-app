package com.nbcsports.regional.nbc_rsn.stepped_story.template;

import android.content.Context;

import androidx.appcompat.widget.AppCompatTextView;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Fix TextView with clickable span (except hyperlinks) blocking onTouch ACTION_DOWN of RecyclerView
 * Please refer to "https://stackoverflow.com/a/14093635"
 */
public class SteppedStoryCustomTextView extends AppCompatTextView {

    public boolean dontConsumeNonUrlClicks = true;
    public boolean linkHit;

    public SteppedStoryCustomTextView(Context context) {
        super(context);
    }

    public SteppedStoryCustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SteppedStoryCustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        linkHit = false;
        boolean res = super.onTouchEvent(event);
        if (dontConsumeNonUrlClicks){
            return linkHit;
        }
        return res;
    }

    public static class LocalLinkMovementMethod extends LinkMovementMethod {
        private static LocalLinkMovementMethod sInstance;

        public static LocalLinkMovementMethod getInstance() {
            if (sInstance == null){
                sInstance = new LocalLinkMovementMethod();
            }
            return sInstance;
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                x -= widget.getTotalPaddingEnd();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(
                        off, off, ClickableSpan.class);

                if (link.length != 0){
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    }
                    if (widget instanceof SteppedStoryCustomTextView){
                        ((SteppedStoryCustomTextView)widget).linkHit = true;
                    }
                    return true;
                } else {
                    Touch.onTouchEvent(widget, buffer, event);
                    return false;
                }
            }
            return Touch.onTouchEvent(widget, buffer, event);
        }
    }

}
