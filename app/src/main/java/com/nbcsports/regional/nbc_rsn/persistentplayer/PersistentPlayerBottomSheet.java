package com.nbcsports.regional.nbc_rsn.persistentplayer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.chromecast.ChromecastDeviceDiscovery;
import com.nbcsports.regional.nbc_rsn.chromecast.ChromecastDiscoveryListener;
import com.nbcsports.regional.nbc_rsn.chromecast.ChromecastListener;
import com.nbcsports.regional.nbc_rsn.chromecast.IChromecastHelper;
import com.nbcsports.regional.nbc_rsn.chromecast.PersistentPlayerBottomSheetChromecastMixin;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

public class PersistentPlayerBottomSheet extends BottomSheetDialogFragment implements PersistentPlayerBottomSheetChromecastMixin,
                                                                                      ChromecastDiscoveryListener {

    View contentView;
    private DialogInterface.OnDismissListener dismissListener;
    private Unbinder unbinder;
    private PersistentPlayer persistentPlayer;
    NativeShareUtils.ShareInfo shareInfo;

    @BindView(R.id.kebab_share_text)
    TextView shareText;

    @BindView(R.id.mute_text)
    TextView muteText;

    @BindView(R.id.mute_icon)
    ImageView muteIcon;

    @BindView(R.id.closed_caption_text)
    TextView closedCaptionText;

    @BindView(R.id.closed_caption_icon)
    ImageView closedCaptionIcon;

    @BindView(R.id.chromecast)
    View chromecastView;

    private ChromecastDeviceDiscovery mMediaRouterCallback;


    @Override
    public ChromecastListener getChromecastListener() {
        IChromecastHelper chromecastHelper = Objects.requireNonNull(Injection.providePlayer((PersistentPlayerContract.Main.View) getActivity()))
                .getChromecastHelper();
        return chromecastHelper != null? chromecastHelper.getChromecastListener() : null;
    }

    @Override
    public ChromecastDeviceDiscovery getChromecastDeviceDiscovery() {
        return mMediaRouterCallback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setOnShowListener(dialog -> {
            BottomSheetDialog d1 = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = (FrameLayout) d1.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior behaviour = BottomSheetBehavior.from(bottomSheet);
            behaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss();
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });
            behaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            behaviour.setPeekHeight(0);
        });

        return d;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        contentView = View.inflate(getContext(), R.layout.persistent_player_bottom_sheet, null);
        dialog.setContentView(contentView);
        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        unbinder = ButterKnife.bind(this, contentView);

        persistentPlayer = Injection.providePlayer((PersistentPlayerContract.Main.View) getActivity());

        // mute
        muteText.setText(persistentPlayer.isMuted() ? LocalizationManager.KebabMenu.Unmute : LocalizationManager.KebabMenu.Mute);
        muteIcon.setImageResource(persistentPlayer.isMuted() ? R.drawable.ic_unmute : R.drawable.ic_mute);

        // share
        shareText.setText(LocalizationManager.KebabMenu.Share);

        // closed caption
        closedCaptionText.setText(persistentPlayer.isClosedCaptionEnabled()
                ? LocalizationManager.KebabMenu.CCOff : LocalizationManager.KebabMenu.CCOn);
        closedCaptionIcon.setImageResource(persistentPlayer.isClosedCaptionEnabled() ? R.drawable.ic_cc_on : R.drawable.ic_cc_off);

        // only show the chromecast icon if a chromecast is detected in the network
        // setup Chromecast Icon
        setupChromecastIcon(dialog.findViewById(R.id.chromecast_icon), dialog.findViewById(R.id.chromecast));

        if (isChromecastEnabled()) {
            mMediaRouterCallback = new ChromecastDeviceDiscovery(getActivity(), this);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (isChromecastEnabled()){
            removeSessionManagerListener();
        }
        addSessionManagerListenerAndDeviceDiscovery();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeDeviceDiscovery();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (dismissListener != null) {
            dismissListener.onDismiss(getDialog());
        }
    }

    @OnClick(R.id.cancel)
    public void cancelButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.mute)
    public void muteButtonClicked() {
        persistentPlayer.mute(!persistentPlayer.isMuted());
        dismiss();
    }

    @OnClick(R.id.kebab_share)
    public void shareButtonClicked() {
        if(shareInfo == null) {
            Timber.d("Share info not initialized");
            return;
        }
        NativeShareUtils.share(shareInfo);
        //Deleted the dismiss function since it disturb the keyboard behaviour when returning from another app
    }

    @OnClick(R.id.closed_caption)
    public void closedCaptionClicked() {
        final boolean isCcCurrentlyEnabled = persistentPlayer.isClosedCaptionEnabled();

        persistentPlayer.enableClosedCaption(!isCcCurrentlyEnabled);

        if (isChromecastEnabled() &&
                persistentPlayer.getChromecastHelper() != null &&
                persistentPlayer.getChromecastHelper().isStateConnected()) {
            persistentPlayer.getChromecastHelper().setClosedCaptions(!isCcCurrentlyEnabled);
        }

        dismiss();
    }

    public void setOnDismiss(DialogInterface.OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    @Override
    public void dismiss() {
        if (getActivity()==null || getActivity().isFinishing() || getActivity().isChangingConfigurations()) return;

        super.dismiss();
        if (dismissListener != null) {
            dismissListener.onDismiss(getDialog());
        }
    }

    public void updateShareInfo(NativeShareUtils.ShareInfo shareInfo) {
        this.shareInfo = shareInfo;
    }

    @Override
    public void onAtLeastOneDeviceAvailable() {
        showChromecastButton();
    }

    @Override
    public void onNoDevicesAvailable() {
        hideChromecastButton();
    }

    public void showChromecastButton() {
        if (persistentPlayer.getChromecastHelper().canShowCastButton()){
            chromecastView.setVisibility(View.VISIBLE);
        }
    }

    public void hideChromecastButton() {
        chromecastView.setVisibility(View.GONE);
    }
}
