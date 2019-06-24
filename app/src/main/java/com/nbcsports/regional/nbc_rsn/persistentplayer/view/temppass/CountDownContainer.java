package com.nbcsports.regional.nbc_rsn.persistentplayer.view.temppass;

import android.content.Context;
import android.content.res.Configuration;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class CountDownContainer extends ConstraintLayout {

    @BindView(R.id.temp_pass_time_remaining)
    TextView tempPassTimeRemaining;

    @BindView(R.id.temp_pass_preview)
    TextView tempPassPreview;


    public CountDownContainer(Context context) {
        super(context);
        init();
    }

    public CountDownContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CountDownContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.temppass_countdown_container, this);
        ButterKnife.bind(this);
    }

    public DisposableObserver<Long> start(Auth auth, CountDownContainer.Listener listener ){

        if (LocalizationManager.isInitialized()){
            tempPassPreview.setText("| "+LocalizationManager.VideoPlayer.PreviewRemaining);
        }

        setVisibility(VISIBLE);
        tempPassPreview.setVisibility(VISIBLE);

        // remaining time = expiry time - current time
        String expiryString = auth.getAuthZToken().getExpires();
        long expiry = Long.parseLong(expiryString);
        long currentTime = Calendar.getInstance().getTimeInMillis();

        long remaining = expiry - currentTime;
        // debugging - make the remaining expiry time only a few seconds instead of 30 minutes gotten from server.
        // e.g. 30 or 10 seconds (30000 or 10000)
        // long remaining = 10000;

        Timber.d("temp pass countdown: %s = %s - %s", remaining, expiry, currentTime);
        long remainingSeconds = remaining / 1000;
        DisposableObserver<Long> tempPassCountDown = Observable.interval(0, 1, TimeUnit.SECONDS)
                .takeWhile(val -> val < remainingSeconds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        long remain = remaining - aLong * 1000;
                        //Timber.d("temp pass countdown onNext: %s, remaining: %s", aLong, remain);
                        tempPassUpdateClock(remain);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                        if (listener != null){
                            listener.onComplete();
                        }

                    }
                });

        return tempPassCountDown;
    }

    private void tempPassUpdateClock(long milliUntilFinished){

        tempPassTimeRemaining.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliUntilFinished),
                TimeUnit.MILLISECONDS.toSeconds(milliUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliUntilFinished))
        ));
    }

    public interface Listener {
        void onComplete();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == ORIENTATION_LANDSCAPE ) {
            int textSize = (int) getResources().getDimension(R.dimen.temp_pass_text_size_1_land);
            tempPassPreview.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        } else {
            int textSize = (int) getResources().getDimension(R.dimen.temp_pass_text_size_1);
            tempPassPreview.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }

    }
}
