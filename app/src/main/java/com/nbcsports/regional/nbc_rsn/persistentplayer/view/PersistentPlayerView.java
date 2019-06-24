package com.nbcsports.regional.nbc_rsn.persistentplayer.view;

import android.animation.LayoutTransition;
import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.Injection;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationPresenter;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerBottomSheet;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PlayWhenReadyListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PlayerEngine;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnCloseClickListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnControlViewTouchListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnKebabClickListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnPauseClickListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnPlayClickListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnPlayerViewTouchListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.intent.OnScrubTimebarListener;
import com.nbcsports.regional.nbc_rsn.persistentplayer.switchscreen.SwitchScreenView;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.progressbar.PersistentPlayerProgressBarManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.view.temppass.CountDownContainer;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import lombok.Getter;
import timber.log.Timber;

import static com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants.Controller.Mode.AUTO_HIDE;

/*
* Custom view for persistent_player_view.xml. Contains the player view for ExoPlayer and
* PrimeTime
*/
public class PersistentPlayerView extends ConstraintLayout implements PlayWhenReadyListener {

    @BindView(R.id.exo_player_view) @Getter
    protected PlayerView exoPlayerView;

    @BindView(R.id.primetime_player_view) @Getter
    protected FrameLayout primetimePlayerView;

    @BindView(R.id.temp_pass_countdown_container) @Getter
    protected CountDownContainer tempPassCountdownContainer;

    @BindView(R.id.sign_in_to_watch_overlay_background) @Getter
    protected ImageView signInToWatchOverlayBackground;

    @BindView(R.id.temp_pass_expired_container) @Getter
    ConstraintLayout tempPassExpiredContainer;

    protected View playerControlView;

    @Getter
    protected ImageButton play;

    @Getter
    protected ImageButton pause;

    @Getter
    protected View kebabClickArea;

    @Getter
    protected ImageView mvpdTopBarLogo;

    @Getter
    protected ImageButton closeButton;

    protected DefaultTimeBar timeBar;

    protected TextView exoPosition;

    protected TextView exoDuration;

    protected FrameLayout playPauseContainer;

    @Getter
    protected TextView adsLabel;

    @BindView(R.id.persistent_player_highlight_backward) @Getter
    protected ImageButton highlightBackward;

    @BindView(R.id.persistent_player_highlight_forward) @Getter
    protected ImageButton highlightForward;

    @BindView(R.id.persistent_player_title) @Getter
    protected TextView titleView;

    @BindView(R.id.video_drawer) @Getter
    SwitchScreenView switchScreenView;

    @BindView(R.id.standalone_play_button) @Getter
    ImageButton standalonePlayButton;

    @BindView(R.id.persistent_player_progress) @Getter
    PersistentPlayerProgressBar progressBar;

    @BindView(R.id.sign_in_to_watch_close) @Getter
    ImageButton signInToWatchClose;

    @BindView(R.id.preview_has_ended)
    TextView previewHasEndedTextView;

    @BindView(R.id.continue_watching)
    TextView continueWatchingTextView;

    @Getter
    private boolean controlsInitialized;

    private FrameLayout currentPlayerView;

    @Getter
    private PersistentPlayer lastKnownPersistentPlayer;

    @Getter
    private LiveTimeBarControls lastKnownLiveControls;

    @Getter
    private PersistentPlayerBottomSheet lastKnownBottomSheet;

    private StreamAuthenticationContract.Presenter streamAuthenticationPresenter;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    DisposableObserver<Long> timebarObservable;
    private boolean timebarDurationSet;
    private OnScrubTimebarListener timeBarListener = new OnScrubTimebarListener(this);
    @Getter
    private OnCloseClickListener closeClickListener;
    private Disposable threeSecondRule;

    @Getter
    private DisposableObserver<Long> progressBarDisposableObserver;

    public PersistentPlayerView(Context context) {
        super(context);
        init();
    }

    public PersistentPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PersistentPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.persistent_player_view, this);
        ButterKnife.bind(this);

        // Let see if the slow animation will make a come back.
        // getExoPlayerView().setLayoutTransition(getExoPlayerTransistion());
        getTempPassCountdownContainer().setLayoutTransition(getExoPlayerTransistion());

        // Hide controllers when view is first created
        exoPlayerView.findViewById(R.id.exo_controller).setVisibility(GONE);
        primetimePlayerView.findViewById(R.id.exo_controller).setVisibility(GONE);

        streamAuthenticationPresenter = Injection.provideStreamAuthentication((StreamAuthenticationContract.View) getContext());
    }


    public void initControls(PersistentPlayer persistentPlayer,
                             LiveTimeBarControls liveControls,
                             PersistentPlayerBottomSheet bottomSheet){

        if (persistentPlayer.getPlayerEngine() == null){
            return;
        }
        MediaSource mediaSource = persistentPlayer.getMediaSource();
        controlsInitialized = true;

        if (persistentPlayer.getPlayerEngine().getType() == PlayerConstants.PlayerEngine.Type.PT){

            exoPlayerView.setVisibility(GONE);
            primetimePlayerView.setVisibility(VISIBLE);
            currentPlayerView = primetimePlayerView;

        } else if (persistentPlayer.getPlayerEngine().getType() == PlayerConstants.PlayerEngine.Type.EXO) {

            exoPlayerView.setVisibility(VISIBLE);
            primetimePlayerView.setVisibility(GONE);
            currentPlayerView = exoPlayerView;
        }

        playerControlView = currentPlayerView.findViewById(R.id.exo_controller);
        play = currentPlayerView.findViewById(R.id.exo_play);
        pause = currentPlayerView.findViewById(R.id.exo_pause);
        kebabClickArea = currentPlayerView.findViewById(R.id.persistent_player_kebab_button_click_area);
        mvpdTopBarLogo = currentPlayerView.findViewById(R.id.mvpd_bar_logo_landscape);
        closeButton = currentPlayerView.findViewById(R.id.persistent_player_close);
        timeBar = currentPlayerView.findViewById(R.id.exo_progress);
        exoPosition = currentPlayerView.findViewById(R.id.exo_position);
        exoDuration = currentPlayerView.findViewById(R.id.exo_duration);
        playPauseContainer = currentPlayerView.findViewById(R.id.play_pause_container);
        adsLabel = currentPlayerView.findViewById(R.id.ads_label);

        // Initial Play/Pause state
        onSet(persistentPlayer.isPlayWhenReady());

        // Listen to play/pause change and update UI
        persistentPlayer.setPlayWhenReadyListener(this);

        // determine to live or non-live UI
        showNonLiveControlUI( ! mediaSource.getLive());
        liveControls.show(mediaSource.getLive());

        // start time bar
        startTimebar();

        // standalone play affecting pause button, let's handle this scenario
        if (standalonePlayButton.getVisibility() == VISIBLE) {
            getPause().setVisibility(GONE);
        }

        getPlay().setOnTouchListener(new OnPlayClickListener(persistentPlayer, this, liveControls));
        getPause().setOnTouchListener(new OnPauseClickListener(persistentPlayer, this, liveControls));
        getPlayerControlView().setOnTouchListener(new OnControlViewTouchListener(this, streamAuthenticationPresenter));
        setOnTouchListener(new OnPlayerViewTouchListener(this, streamAuthenticationPresenter, mediaSource));
        kebabClickArea.setOnClickListener(new OnKebabClickListener(bottomSheet, getContext()));
        closeClickListener = new OnCloseClickListener(persistentPlayer);
        closeButton.setOnClickListener(closeClickListener);
        signInToWatchClose.setOnClickListener(new OnCloseClickListener(persistentPlayer));
        timeBar.addListener(timeBarListener);
        timeBarListener.setPersistentPlayer(persistentPlayer);

        lastKnownPersistentPlayer = persistentPlayer;
        lastKnownLiveControls = liveControls;
        lastKnownBottomSheet = bottomSheet;
    }

    public void showNonLiveControlUI(boolean show){
        if (timeBar == null){ return; }
        if (exoPosition == null){ return; }
        if (exoDuration == null){ return; }

        if (show){
            timeBar.setVisibility(VISIBLE);
            exoPosition.setVisibility(VISIBLE);
            exoDuration.setVisibility(VISIBLE);
        } else {
            timeBar.setVisibility(INVISIBLE);
            exoPosition.setVisibility(INVISIBLE);
            exoDuration.setVisibility(INVISIBLE);
        }
    }

    public void showControllers(boolean show, PlayerConstants.Controller.Mode mode) {
        if (lastKnownPersistentPlayer != null){
            lastKnownPersistentPlayer.log(this, "showControllers(%s)", show);
        }
        if (getPlayerControlView() != null) {
            getPlayerControlView().setVisibility(show ? VISIBLE : GONE);
            if (mode == AUTO_HIDE){
                enforceThreeSecondRule();
            }
        }
    }

    public void showControllers(boolean show){
       showControllers(show, AUTO_HIDE);
    }


    public void showTeamPassCountdown(){
        if (getPlayerControlView() instanceof PersistentPlayerControlView) {
            ((PersistentPlayerControlView) getPlayerControlView()).setVisibilityListener(visibility -> getTempPassCountdownContainer().setVisibility(visibility));
        } else if (getPlayerControlView() instanceof PlayerControlView) {
            ((PlayerControlView) getPlayerControlView()).setVisibilityListener(visibility -> getTempPassCountdownContainer().setVisibility(visibility));
        }
    }

    public void enforceThreeSecondRule() {
        if (threeSecondRule != null) {
            compositeDisposable.remove(threeSecondRule);
        }
        threeSecondRule = Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (getPlayerControlView() != null) {
                        getPlayerControlView().setVisibility(GONE);
                    }
                    // Checking if chromecast is connected
                    // 1. If so, keep temppass showing if available
                    // 2. Otherwise, hide temppass as default
                    if (lastKnownPersistentPlayer != null
                            && lastKnownPersistentPlayer.getChromecastHelper() != null
                            && lastKnownPersistentPlayer.getChromecastHelper().isStateConnected()){
                        return;
                    }
                    getTempPassCountdownContainer().setVisibility(GONE);
                });
        compositeDisposable.add(threeSecondRule);
    }

    public void initLocalization() {
        if (LocalizationManager.isInitialized() && previewHasEndedTextView != null && continueWatchingTextView != null){
            previewHasEndedTextView.setText(LocalizationManager.VideoPlayer.PreviewEndedTitle);
            continueWatchingTextView.setText(LocalizationManager.VideoPlayer.PreviewEndedDescription);
        }
    }

    private LayoutTransition getExoPlayerTransistion() {
        LayoutTransition lt = new LayoutTransition();
        lt.setDuration(LayoutTransition.DISAPPEARING, 2500);
        return lt;
    }

    public View getPlayerControlView() {
        return playerControlView;
    }

    @Override
    public void onSet(boolean playing) {
        if (playing){
            play.setVisibility(GONE);
            pause.setVisibility(VISIBLE);
        } else {
            play.setVisibility(VISIBLE);
            pause.setVisibility(GONE);
        }
    }

    public void syncState() {
        if (lastKnownPersistentPlayer == null){ return; }
        if (lastKnownLiveControls == null){ return; }
        if (lastKnownBottomSheet == null){ return; }
        initControls(lastKnownPersistentPlayer, lastKnownLiveControls, lastKnownBottomSheet);
    }

    /**
     * Display the MVPD logo
     * @param streamAuthenticationPresenter
     */
    public void initMvpdLogo(StreamAuthenticationContract.Presenter streamAuthenticationPresenter) {
        if (streamAuthenticationPresenter.getLastKnownAuth() != null
                && !TextUtils.isEmpty(streamAuthenticationPresenter.getLastKnownAuth().getLandScapeLogoUrl())) {
            displayHeader(streamAuthenticationPresenter.getLastKnownAuth());

        } else {
            checkAuthAndShowMvpdHeader();
        }
    }
    public void displayHeader(Auth auth) {
        getMvpdTopBarLogo().setVisibility(View.GONE);
        if (auth == null) {
            return;
        }
        if (auth.getLandScapeLogoUrl() != null && !auth.getLandScapeLogoUrl().isEmpty() && !auth.isTempPass(streamAuthenticationPresenter.getConfig())) {
            Picasso.get()
                    .load(auth.getLandScapeLogoUrl())
                    .into(getMvpdTopBarLogo(), new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            getMvpdTopBarLogo().setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                            Timber.d("Error when loading the image");
                        }
                    });
        }
    }

    public void checkAuthAndShowMvpdHeader() {
        if (streamAuthenticationPresenter == null) {
            return;
        }
        streamAuthenticationPresenter.checkAuthNStatus(
                PreferenceUtils.INSTANCE.getString(StreamAuthenticationPresenter.LAST_SUCCESSFUL_MVPD, ""),
                new DisposableObserver<Auth>() {
                    @Override
                    public void onNext(Auth auth) {
                        if (auth.getAuthNToken() != null && !auth.isTempPass(streamAuthenticationPresenter.getConfig())) {
                            displayHeader(auth);
                        } else {
                            getMvpdTopBarLogo().setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpdTopBarLogo().setVisibility(View.GONE);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void startTimebar() {
        if (timebarObservable != null){
            compositeDisposable.remove(timebarObservable);
        }
        timebarDurationSet = false;
        timebarObservable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .skipWhile(aLong -> lastKnownPersistentPlayer == null)
                .skipWhile(aLong -> lastKnownPersistentPlayer.getPlayerEngine() == null)
                .skipWhile(aLong -> !lastKnownPersistentPlayer.isPlaying())
                .flatMap(setupTimebar())
                .flatMap(updateTimebarPosition())
                .flatMap(updateTimebarDuration())
                .flatMap(stopWhenFinish())
                .flatMap(stopIfLiveDetected())
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        compositeDisposable.add(timebarObservable);
    }

    private Function<Long, Observable<Long>> setupTimebar() {
        return aLong -> {
            PlayerEngine.Interface pe = lastKnownPersistentPlayer.getPlayerEngine();
            if (!timebarDurationSet) {
                timeBar.setDuration(pe.getDuration());
                timebarDurationSet = true;
            }
            return Observable.just(aLong);
        };
    }

    private Function<Long, Observable<Long>> updateTimebarDuration() {
        return aLong -> {
            PlayerEngine.Interface pe = lastKnownPersistentPlayer.getPlayerEngine();
            exoDuration.setText(milliToMinuteSeconds(pe.getDuration()));
            return Observable.just(aLong);
        };
    }

    private Function<Long, Observable<Long>> updateTimebarPosition() {
        return aLong -> {
            PlayerEngine.Interface pe = lastKnownPersistentPlayer.getPlayerEngine();
            //  lastKnownPersistentPlayer.log(this, "timebar ticking timebarTimeBeforeSeek: %s, currentPosition: %s",
            //          timeBarListener.getSeekToPosition(),
            //          lastKnownPersistentPlayer.getPlayerEngine().getCurrentPosition());
            exoPosition.setText(milliToMinuteSeconds(pe.getCurrentPosition()));
            timeBar.setPosition(pe.getCurrentPosition());
            return Observable.just(aLong);
        };
    }

    private Function<Long, Observable<Long>> stopWhenFinish() {
        return aLong -> {
            PlayerEngine.Interface pe = lastKnownPersistentPlayer.getPlayerEngine();
            if (pe.isFinished()){
                stopTimebar();
                exoPosition.setText(milliToMinuteSeconds(pe.getCurrentPosition()));
                showControllers(true);
                standalonePlayButton.setVisibility(VISIBLE);
                getPlay().setVisibility(GONE);
                getPause().setVisibility(GONE);
                timeBar.setDuration(0l);
                timebarDurationSet = false;
            }
            return Observable.just(aLong);
        };
    }

    private Function<Long, Observable<Long>> stopIfLiveDetected() {
        return aLong -> {
            MediaSource ms = lastKnownPersistentPlayer.getMediaSource();
            if (ms.getLive()){
                stopTimebar();
            }
            return Observable.just(aLong);
        };
    }

    public void stopTimebar() {
        compositeDisposable.clear();
    }

    /**
     * This method will re-apply time bar listener to time bar
     *
     * This method is used when the medium is detached from window
     * and attached again to the window
     *
     * @param persistentPlayer
     */
    public void resetTimeBarListener(PersistentPlayer persistentPlayer) {
        if (timeBar != null && timeBarListener != null
                && persistentPlayer != null){
            timeBar.removeListener(timeBarListener);
            timeBar.addListener(timeBarListener);
            timeBarListener.setPersistentPlayer(persistentPlayer);
        }
    }

    private String milliToMinuteSeconds(long milliseconds){

        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopTimebar();
        if (timeBar != null)
            timeBar.removeListener(timeBarListener);
    }

    public void resetPause() {
        if (getPause() != null)
            getPause().setVisibility(View.VISIBLE);
    }

    public void updateTimebar(long position) {
        exoPosition.setText(milliToMinuteSeconds(position));
    }

    public void showProgressBar(boolean show){
        if (getProgressBar() == null){ return; }
        if (show){
            if (playPauseContainer != null) playPauseContainer.setVisibility(GONE);
            progressBar.setVisibility(VISIBLE);
        } else {
            if (playPauseContainer != null) playPauseContainer.setVisibility(VISIBLE);
            progressBar.setVisibility(GONE);
        }
    }

    public void showAdUI(boolean show) {
        if (lastKnownLiveControls != null){
            lastKnownLiveControls.show(!show);
        }
        if (adsLabel != null){
            adsLabel.setVisibility(show? VISIBLE : GONE);
        }
    }

    /**
     * This is used to sync the state between 2 player view. Here are states getting synced so far:
     * - adsLabel
     * @param source
     * @param target
     */
    public static void syncState(PersistentPlayerView source, PersistentPlayerView target) {

        if (source == null){ return; }
        if (target == null){ return; }

        if (source.getLastKnownPersistentPlayer() != null
                && source.getLastKnownPersistentPlayer().isAdPlaying()
                && source.getLastKnownPersistentPlayer().isLive()){
            target.showAdUI(true);
        } else if (source.getLastKnownPersistentPlayer() != null
                && !source.getLastKnownPersistentPlayer().isAdPlaying()
                && source.getLastKnownPersistentPlayer().isLive()){
            target.showAdUI(false);
        }

        // This part is used for landscape transit to medium
        if (target != null && target.lastKnownPersistentPlayer != null){
            // Sync play/pause button state
            target.onSet(target.lastKnownPersistentPlayer.isPlayWhenReady());
            // Listen to play/pause change and update UI
            target.lastKnownPersistentPlayer.setPlayWhenReadyListener(target);
        }

    }

    public void showTempPassExpired() {
        ConstraintLayout tempPassExpiredContainer = getTempPassExpiredContainer();
        tempPassExpiredContainer.setVisibility(View.VISIBLE);
        initLocalization();
    }

    public void hideTempPassExpired() {
        ConstraintLayout tempPassExpiredContainer = getTempPassExpiredContainer();
        tempPassExpiredContainer.setVisibility(View.GONE);
    }

    public void showTempPassGeoBlocked() {
        ConstraintLayout tempPassExpiredContainer = getTempPassExpiredContainer();
        tempPassExpiredContainer.setVisibility(View.VISIBLE);
        if (LocalizationManager.isInitialized() && previewHasEndedTextView != null && continueWatchingTextView != null){
            previewHasEndedTextView.setText(LocalizationManager.VideoPlayer.PreviewUnavailableTitle);
            continueWatchingTextView.setText(LocalizationManager.VideoPlayer.PreviewUnavailableDescription);
        }
    }

    public boolean isTempPassExpiredShowing(){
        return getTempPassExpiredContainer().getVisibility() == VISIBLE;
    }

    /**
     * This method is used to start the progress bar observer
     * and add it to progress bar manager for management
     *
     * The observer is going to check the player engine status,
     * and base on the status, it change to visibility of the
     * progress bar
     *
     * @param persistentPlayer
     * @param ppcView
     */
    public void startProgressBarObserver(PersistentPlayer persistentPlayer,
                                         PersistentPlayerContract.View ppcView) {
        if (progressBar != null){
            if (progressBarDisposableObserver == null || progressBarDisposableObserver.isDisposed()){
                progressBarDisposableObserver = progressBar.getProgressBarDO(persistentPlayer, ppcView);
            }
            PersistentPlayerProgressBarManager.addProgressBarObserver(progressBarDisposableObserver,
                    ppcView.hashCode());
        }
    }

    /**
     * This method is used to stop the progress bar observer
     * and remove it from progress bar manager
     *
     * @param ppcView
     */
    public void stopProgressBarObserver(PersistentPlayerContract.View ppcView) {
        if (progressBarDisposableObserver != null){
            PersistentPlayerProgressBarManager.removeProgressBarObserver(progressBarDisposableObserver,
                    ppcView.hashCode());
        }
    }

    public void trackControlsVisibility(final boolean playVisible, final boolean pauseVisible, final boolean progressBarVisible) {
        if (play != null) {
            play.setVisibility(playVisible ? View.VISIBLE : View.GONE);
        }
        if (pause != null) {
            pause.setVisibility(pauseVisible ? View.VISIBLE : View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(progressBarVisible ? View.VISIBLE : View.GONE);
        }
    }

}
