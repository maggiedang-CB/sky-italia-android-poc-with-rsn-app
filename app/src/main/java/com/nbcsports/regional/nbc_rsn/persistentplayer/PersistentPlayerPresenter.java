package com.nbcsports.regional.nbc_rsn.persistentplayer;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.nbcsports.regional.nbc_rsn.common.Highlight;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.common.TimelineMarker;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

class PersistentPlayerPresenter implements PersistentPlayerContract.Presenter {

    private final PersistentPlayer persistentPlayer;
    private final PersistentPlayerContract.View persistentPlayerView;
    private Gson gson = new Gson();
    private long programStartTime;
    private Highlight highlight;

    public PersistentPlayerPresenter(PersistentPlayer persistentPlayer, PersistentPlayerContract.View persistentPlayerView) {

        this.persistentPlayer = checkNotNull(persistentPlayer);
        this.persistentPlayerView = checkNotNull(persistentPlayerView);

        persistentPlayerView.setMainPresenter(this);
    }

    @Override
    public void play(String url) {

    }

    // Highlights / Chapter Markers
    @Override
    public void getHighlightData(MediaSource mediaSource) {

        if ( ! TextUtils.isEmpty(mediaSource.getId())) {
            return;
        }

        getTimelineMarkers(mediaSource.getId())
           .subscribeOn( Schedulers.io())
           .observeOn( AndroidSchedulers.mainThread() )
           .subscribe( new Observer<Highlight>() {

               @Override
               public void onSubscribe( Disposable d ) {

               }

               @Override
               public void onNext( Highlight highlight ) {
                   Timber.d("getHighlightData onNext()");
                   PersistentPlayerPresenter.this.highlight = highlight;
                   persistentPlayerView.showHighlightMarker(highlight);
               }

               @Override
               public void onError( Throwable e ) {

               }

               @Override
               public void onComplete() {

               }
           } );

    }

    @Override
    public void setProgramStartTime( long startTimeUs ) {
        programStartTime = startTimeUs;
    }

    @Override
    public long getProgramStartTime() {
        return programStartTime;
    }

    @Override
    public List<TimelineMarker> getTimelineMarkers() {
        if (highlight!=null){
            return highlight.getTimelineMarkers();
        }
        return null;
    }

    private Observable<Highlight> getTimelineMarkers( String pid ){

        final OkHttpClient client = new OkHttpClient();
        String urlLiteral = "http://stream.nbcsports.com/data/lowttl/timeline_markers_[pid].json";
        String url = urlLiteral.replace( "[pid]", pid );

        final Request request = new Request.Builder()
            .url(url)
            .get()
            .build();

        return Observable.create( emitter -> {
            try {
                Response response = client.newCall(request).execute();
                Highlight highlight = gson.fromJson( response.body().charStream(), Highlight.class );
                emitter.onNext(highlight);
                emitter.onComplete();
            } catch (IOException e) {
                Timber.e(e);
                emitter.onError(e);
            }
        } );
    }
}
