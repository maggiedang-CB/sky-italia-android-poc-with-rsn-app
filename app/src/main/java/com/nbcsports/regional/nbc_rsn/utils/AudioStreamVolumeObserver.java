package com.nbcsports.regional.nbc_rsn.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import androidx.annotation.NonNull;

public class AudioStreamVolumeObserver {

    public static void convertToPercentage(int volume) {
    }

    public interface OnAudioStreamVolumeChangedListener {
        void onAudioStreamVolumeChanged(int audioStreamType, float volume);
    }

    private static class AudioStreamVolumeContentObserver extends ContentObserver {

        private final AudioManager mAudioManager;
        private final int mAudioStreamType;
        private final OnAudioStreamVolumeChangedListener mListener;
        private int mLastVolume;

        public AudioStreamVolumeContentObserver(@NonNull Handler handler,
                                                @NonNull AudioManager audioManager,
                                                int audioStreamType,
                                                @NonNull OnAudioStreamVolumeChangedListener listener) {
            super(handler);

            mAudioManager = audioManager;
            mAudioStreamType = audioStreamType;
            mListener = listener;
            mLastVolume = mAudioManager.getStreamVolume(mAudioStreamType);
        }

        @Override
        public void onChange(boolean selfChange) {
            int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (currentVolume != mLastVolume) {
                mLastVolume = currentVolume;
                float volumePercentage = currentVolume / (float) maxVolume * 100;
                mListener.onAudioStreamVolumeChanged(mAudioStreamType, volumePercentage);
            }
        }
    }

    private final Context mContext;
    private AudioStreamVolumeContentObserver mAudioStreamVolumeContentObserver;

    public AudioStreamVolumeObserver(@NonNull Context context) {
        mContext = context;
    }

    public void start(int audioStreamType, @NonNull OnAudioStreamVolumeChangedListener listener) {
        stop();

        Handler handler = new Handler();
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        mAudioStreamVolumeContentObserver = new AudioStreamVolumeContentObserver(handler, audioManager, audioStreamType, listener);

        mContext
                .getContentResolver()
                .registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mAudioStreamVolumeContentObserver);
    }

    public void stop() {
        if (mAudioStreamVolumeContentObserver == null) {
            return;
        }

        mContext
                .getContentResolver()
                .unregisterContentObserver(mAudioStreamVolumeContentObserver);
        mAudioStreamVolumeContentObserver = null;
    }
}
