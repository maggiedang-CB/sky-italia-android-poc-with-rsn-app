package com.nbcsports.regional.nbc_rsn.debug_options;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.view.View;

import com.jakewharton.rxbinding2.view.RxView;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsActionableView;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsDescriptionView;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsToggleButton;
import com.urbanairship.UAirship;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import lombok.Getter;

public class DebugFragment extends BaseFragment implements DebugContract.View {

    @BindView(R.id.debug_copy_ua_id)
    SettingsActionableView uaChannelId;

    @BindView(R.id.debug_state_container)
    ConstraintLayout stateContainer;

    @BindView(R.id.debug_current_config_state)
    SettingsDescriptionView currentConfigState;

    @BindView(R.id.debug_configuration_url)
    SettingsDescriptionView configUrl;

    @BindView(R.id.debug_ua_id)
    SettingsDescriptionView uaID;

    @BindView(R.id.debug_data_bar_option)
    SettingsActionableView dataBarOption;

    @BindView(R.id.debug_data_menu_option)
    SettingsActionableView dataMenuOption;

    @Getter
    private DebugContract.Presenter presenter;

    private StreamAuthenticationContract.Presenter authPresenter;

    @Override
    public int getLayout() {
        return R.layout.fragment_debug_option;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        //this.trackingPageName = "DebugFragment";
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new DebugPresenter(this);
        initialFields();
        compositeDisposable.addAll(
                RxView.clicks(stateContainer)
                        .debounce(500L, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(v -> {
                            presenter.displayStateChooser(getActivity());
                        })
        );
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden) {
            String channelIdText = UAirship.shared().getPushManager().getChannelId();
            if (channelIdText != null) {
                uaID.setDescription(channelIdText);
            }
        }
    }

    @Override
    public void setUpDataBarDefaultValue(boolean defaultValue) {
        dataBarOption.getToggleButton().setToggleDefault(defaultValue);
        if (defaultValue) {
            dataBarOption.getTextView().setText("Showing Data Bar");
        } else {
            dataBarOption.getTextView().setText("Not Showing Data Bar");
        }
    }

    @Override
    public void setUpDataMenuDefaultValue(boolean defaultValue) {
        dataMenuOption.getToggleButton().setToggleDefault(defaultValue);
        if (defaultValue) {
            dataMenuOption.getTextView().setText("Showing Data Menu");
        } else {
            dataMenuOption.getTextView().setText("Not Showing Data Menu");
        }
    }

    @Override
    public void setUpConfigurationUrlDefaultValue(String configUrl) {
        this.configUrl.setDescription(configUrl);
    }

    @Override
    public void setUpConfigurationStateDefaultValue(String configState) {
        currentConfigState.setDescription(configState);
    }

    private void initialFields() {
        presenter.setUpDataBarDefaultValue();
        presenter.setUpDataMenuDefaultValue();
        presenter.setUpConfigurationStateDefaultValue();
        presenter.setUpConfigurationUrlDefaultValue();
    }

    @OnClick(R.id.debug_data_bar_option)
    public void toggleDataBarOption() {
        SettingsToggleButton toggleButton = dataBarOption.getToggleButton();

        if (toggleButton.isToggledOn()) {
            toggleButton.animateToggleOff();
            dataBarOption.getTextView().setText("Not Showing Data Bar");
            presenter.notShowingDataBar();
        } else {
            toggleButton.animateToggleOn();
            dataBarOption.getTextView().setText("Showing Data Bar");
            presenter.showingDataBar();
        }
    }

    @OnClick(R.id.debug_data_menu_option)
    public void toggleDataMenuOption() {
        SettingsToggleButton toggleButton = dataMenuOption.getToggleButton();

        if (toggleButton.isToggledOn()) {
            toggleButton.animateToggleOff();
            dataMenuOption.getTextView().setText("Not Showing Data Menu");
            presenter.notShowingDataMenu();
        } else {
            toggleButton.animateToggleOn();
            dataMenuOption.getTextView().setText("Showing Data Menu");
            presenter.showingDataMenu();
        }
    }

    @Override
    public void updateAuthPresenter(StreamAuthenticationContract.Presenter authPresenter) {
        this.authPresenter = authPresenter;
    }

    @OnClick(R.id.debug_reset_temp_pass)
    public void resetTempPass() {
        presenter.resetTempPass(authPresenter);
    }

    @OnLongClick(R.id.debug_ua_container)
    public boolean copyUAId() {
        presenter.copyUADeviceId(uaID.getDebugDescription(), (MainActivity) getActivity());
        return false;
    }

    @Override
    public void setSettingsPresenter(DebugContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public PageInfo getPageInfo() {
        return new PageInfo(false, "", "", "", "", "", "", "", "", "", "");
    }
}
