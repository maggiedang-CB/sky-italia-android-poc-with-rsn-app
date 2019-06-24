package com.nbcsports.regional.nbc_rsn.debug_options;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nbcsports.regional.nbc_rsn.BuildConfig;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;
import com.nbcsports.regional.nbc_rsn.common.Constants;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;

import io.reactivex.observers.DisposableObserver;

public class DebugPresenter implements DebugContract.Presenter {

    private DebugContract.View debugView;
    private boolean originalDataBarState;
    private boolean originalDataMenuState;
    private String originalConfigurationState;

    private String devConfigUrl = "http://appstream-stage.nbcsports.com/apps/RSN/configuration-android.json";
    private String prodConfigUrl = "http://stream.nbcsports.com/data/mobile/apps/RSN/configuration-android.json";


    public DebugPresenter(DebugContract.View view) {
        debugView = view;
        debugView.setSettingsPresenter(this);
        obtainCurrentValue();
    }

    public static final String CONFIGURATION_STATE = "configuration_state";
    public static final String DATA_MENU_ENABLE = "data_menu_enable";
    public static final String CONFIGURATION_URL = "configuration_url";
    public static final String IS_PROD_STATE = "is_prod_flag";

    private void obtainCurrentValue() {
        originalDataBarState = PreferenceUtils.INSTANCE.getBoolean(Constants.PREF_KEY_DATABAR_ENABLED, true);
        originalDataMenuState = PreferenceUtils.INSTANCE.getBoolean(DATA_MENU_ENABLE, false);
        originalConfigurationState = PreferenceUtils.INSTANCE.getString(CONFIGURATION_STATE, BuildConfig.FLAVOR);
    }

    @Override
    public void setUpDataBarDefaultValue() {
        debugView.setUpDataBarDefaultValue(originalDataBarState);
    }

    @Override
    public void setUpDataMenuDefaultValue() {
        debugView.setUpDataMenuDefaultValue(originalDataMenuState);
    }

    @Override
    public void setUpConfigurationUrlDefaultValue() {
        debugView.setUpConfigurationStateDefaultValue(originalConfigurationState);
    }

    @Override
    public void setUpConfigurationStateDefaultValue() {
        String configUrl = PreferenceUtils.INSTANCE.getString(CONFIGURATION_URL, BuildConfig.CONFIG_URL);
        debugView.setUpConfigurationUrlDefaultValue(configUrl);
    }

    @Override
    public void displayStateChooser(Activity activity) {
        if (activity != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            View stateChooser = activity.getLayoutInflater().inflate(R.layout.settings_debug_config_state_chooser, null);
            final RadioButton prodRadio = stateChooser.findViewById(R.id.radio_prod);
            final RadioButton devRadio = stateChooser.findViewById(R.id.radio_dev);
            final RadioButton customRadio = stateChooser.findViewById(R.id.radio_custom);
            final Button submitButton = stateChooser.findViewById(R.id.debug_submit_state);
            final RadioGroup radioGroup = stateChooser.findViewById(R.id.debug_states);
            final EditText customUrlField = stateChooser.findViewById(R.id.debug_custom_url);
            String currentState = PreferenceUtils.INSTANCE.getString(CONFIGURATION_STATE, BuildConfig.FLAVOR);

            //Reset radio position
            prodRadio.setChecked(currentState.equalsIgnoreCase(prodRadio.getText().toString()));
            devRadio.setChecked(currentState.equalsIgnoreCase(devRadio.getText().toString()));
            if(currentState.equalsIgnoreCase(customRadio.getText().toString())) {
                customRadio.setChecked(currentState.equalsIgnoreCase(customRadio.getText().toString()));
                customUrlField.setVisibility(View.VISIBLE);
            }

            builder.setView(stateChooser);
            AlertDialog dialog = builder.create();

            radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> customUrlField.setVisibility((i == R.id.radio_custom) ? View.VISIBLE : View.GONE));

            submitButton.setOnClickListener(view -> {
                if (prodRadio.isChecked()) {
                    setConfigurationState(prodRadio.getText().toString(), prodConfigUrl, true);
                } else if (devRadio.isChecked()) {
                    setConfigurationState(devRadio.getText().toString(), devConfigUrl, false);
                } else if (customRadio.isChecked()) {
                    setConfigurationState(customRadio.getText().toString(), customUrlField.getText().toString(), false);
                }
                String state = PreferenceUtils.INSTANCE.getString(CONFIGURATION_STATE, BuildConfig.FLAVOR);
                String url = PreferenceUtils.INSTANCE.getString(CONFIGURATION_URL, BuildConfig.CONFIG_URL);
                debugView.setUpConfigurationUrlDefaultValue(url);
                debugView.setUpConfigurationStateDefaultValue(state);
                dialog.hide();
            });
            dialog.show();
        }
    }

    public void setConfigurationState(String state, String url, boolean isProd) {
        PreferenceUtils.INSTANCE.setString(CONFIGURATION_STATE, state);
        PreferenceUtils.INSTANCE.setString(CONFIGURATION_URL, url);
        PreferenceUtils.INSTANCE.setBoolean(IS_PROD_STATE, isProd);
    }

    @Override
    public void notShowingDataBar() {
        PreferenceUtils.INSTANCE.setBoolean(Constants.PREF_KEY_DATABAR_ENABLED, false);
    }

    @Override
    public void showingDataBar() {
        PreferenceUtils.INSTANCE.setBoolean(Constants.PREF_KEY_DATABAR_ENABLED, true);
    }

    @Override
    public void notShowingDataMenu() {
        PreferenceUtils.INSTANCE.setBoolean(DATA_MENU_ENABLE, false);
    }

    @Override
    public void showingDataMenu() {
        PreferenceUtils.INSTANCE.setBoolean(DATA_MENU_ENABLE, true);
    }

    @Override
    public void resetTempPass(StreamAuthenticationContract.Presenter authPresenter) {
        authPresenter.resetTempPass(authPresenter.getAuthorizedRequestorId(), new DisposableObserver<Auth>() {
            @Override
            public void onNext(Auth auth) {
                NotificationsManagerKt.INSTANCE.showAuthMessage("Temp Pass reset successfully");
            }

            @Override
            public void onError(Throwable e) {
                NotificationsManagerKt.INSTANCE.showAuthMessage("Temp Pass reset failed");
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void copyUADeviceId(TextView textView, MainActivity mainActivity) {
        try {
            String copyText = textView.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) mainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("channelId", copyText);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(mainActivity, "Copied channel id", Toast.LENGTH_SHORT).show();

        } catch (NullPointerException e) {
            Toast.makeText(mainActivity, "Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean isRestartingAfterExit() {
        boolean currentDataBarState = PreferenceUtils.INSTANCE.getBoolean(Constants.PREF_KEY_DATABAR_ENABLED, true);
        String currentConfigurationState = PreferenceUtils.INSTANCE.getString(CONFIGURATION_STATE, BuildConfig.FLAVOR);
        return (currentDataBarState != originalDataBarState || !currentConfigurationState.equalsIgnoreCase(originalConfigurationState));
    }

    @Override
    public void openRestartDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View restartView = activity.getLayoutInflater().inflate(R.layout.settings_debug_restart, null);
        final Button cancelButton = restartView.findViewById(R.id.debug_cancel_button);
        final Button restartButton = restartView.findViewById(R.id.debug_restart_button);
        builder.setView(restartView);
        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(view -> dialog.hide());
        //API quote the finishAndRemoveTask() method as: Finishes all activities in this task and removes it from the recent tasks list.
        //Many people suggested it is better than finish() and System.exit(0), did not find any problem when testing
        restartButton.setOnClickListener(view -> activity.finishAndRemoveTask());

        dialog.show();
    }
}
