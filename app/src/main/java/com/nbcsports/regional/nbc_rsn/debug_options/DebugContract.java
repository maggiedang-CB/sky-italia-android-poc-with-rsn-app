package com.nbcsports.regional.nbc_rsn.debug_options;

import android.app.Activity;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.authentication.StreamAuthenticationContract;

public interface DebugContract {

    interface View {

        void updateAuthPresenter(StreamAuthenticationContract.Presenter authPresenter);

        void setSettingsPresenter(Presenter presenter);

        void setUpDataBarDefaultValue(boolean defaultValue);

        void setUpDataMenuDefaultValue(boolean defaultValue);

        void setUpConfigurationUrlDefaultValue(String configUrl);

        void setUpConfigurationStateDefaultValue(String configState);

        void resetTempPass();
    }

    interface Presenter {

        void setUpDataBarDefaultValue();

        void setUpDataMenuDefaultValue();

        void setUpConfigurationUrlDefaultValue();

        void setUpConfigurationStateDefaultValue();

        void displayStateChooser(Activity activity);

        void notShowingDataBar();

        void showingDataBar();

        void notShowingDataMenu();

        void showingDataMenu();

        void resetTempPass(StreamAuthenticationContract.Presenter authPresenter);

        void copyUADeviceId(TextView textView, MainActivity mainActivity);

        boolean isRestartingAfterExit();

        void openRestartDialog(Activity activity);
    }

}