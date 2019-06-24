/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nbcsports.regional.nbc_rsn.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.transition.Fade;
import android.view.Window;
import android.view.WindowManager;

import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This provides methods to help Activities load their UI.
 */
public class ActivityUtils {

    /**
     * The {@code fragment} is added to the container view with id {@code frameId}. The operation is
     * performed by the {@code fragmentManager}.
     */
    public static void addThenHideFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                                     @NonNull Fragment fragment, int frameId) {
        if (fragmentManager == null || fragment == null || fragment.isAdded()) {
            return;
        }

        fragmentManager.beginTransaction()
                .add(frameId, fragment)
                .hide(fragment)
                .commit();
    }

    public static void showFragment(FragmentManager fragmentManager, Fragment fragment) {

        if (fragmentManager == null || fragment == null) {
            return;
        }

        fragmentManager.beginTransaction()
                .show(fragment)
                .commitAllowingStateLoss();
    }

    public static void hideFragment(FragmentManager fragmentManager, Fragment fragment) {

        if (fragmentManager == null || fragment == null) {
            return;
        }

        fragmentManager.beginTransaction()
                .hide(fragment)
                .commitAllowingStateLoss();
    }

    public static Fragment hideThenShowFragment(@NonNull FragmentManager fragmentManager,
                                                @NonNull Fragment fragmentToHide,
                                                @NonNull Fragment fragmentToShow,
                                                int enterAnimation,
                                                int exitAnimation) {
        checkNotNull(fragmentManager);
        checkNotNull(fragmentToHide);
        checkNotNull(fragmentToShow);
        fragmentManager.beginTransaction()
                .setCustomAnimations(enterAnimation, exitAnimation)
                .hide(fragmentToHide)
                .show(fragmentToShow)
                .commitAllowingStateLoss();

        return fragmentToShow;
    }

    public static Fragment hideThenShowFragment(@NonNull FragmentManager fragmentManager,
                                                @NonNull Fragment fragmentToHide,
                                                @NonNull Fragment fragmentToShow) {
        checkNotNull(fragmentManager);
        checkNotNull(fragmentToHide);
        checkNotNull(fragmentToShow);
        fragmentManager.beginTransaction()
                .hide(fragmentToHide)
                .show(fragmentToShow)
                .commitAllowingStateLoss();

        return fragmentToShow;
    }

    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int frameId) {

        if (fragmentManager == null || fragment == null || fragment.isAdded()) {
            return;
        }

        fragmentManager.beginTransaction()
                .add(frameId, fragment)
                .commit();
    }

    public static void removeFragmentToActivity(@NonNull FragmentManager fragmentManager, Fragment fragment) {

        if (fragmentManager == null || fragment == null) {
            return;
        }
        fragmentManager.beginTransaction()
                .remove(fragment)
                .commitAllowingStateLoss();
    }

    public static void showAndAddFragmentToBackStack(String rootTag, @NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {

        if (fragmentManager == null || fragment == null) {
            return;
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(rootTag);
        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
    }

    public static void showAndAddFragmentToBackStackWithFade(
            String rootTag, @NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {

        if (fragmentManager == null || fragment == null) {
            return;
        }

        Fade fade = new Fade();
        fade.setDuration(300);
        fragment.setEnterTransition(fade);
        fragment.setExitTransition(fade);
        fragmentManager.beginTransaction()
                .addToBackStack(rootTag)
                .show(fragment)
                .commit();
    }

    public static void showAndAddFragmentWithCustomAnimation(@NonNull FragmentManager fragmentManager,
                                                             @NonNull Fragment fragment, int frameId,
                                                             int enterAnimId, int exitAnimId) {

        if (fragmentManager == null || fragment == null || fragment.isAdded()) {
            return;
        }

        fragmentManager.beginTransaction()
                .setCustomAnimations(enterAnimId, exitAnimId)
                .add(frameId, fragment)
                .show(fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public static void removeFragmentAndPopBackStack(@NonNull FragmentManager fragmentManager,
                                                     @NonNull Fragment fragment,
                                                     int enterAnim, int exitAnim) {

        if (fragmentManager == null || fragment == null) {
            return;
        }

        fragmentManager.beginTransaction()
                .remove(fragment)
                .setCustomAnimations(enterAnim, exitAnim)
                .commit();
        fragmentManager.popBackStack();
    }

    public static void popToLastStack(@NonNull FragmentManager fragmentManager) {
        if (fragmentManager == null) return;
        fragmentManager.popBackStackImmediate();
    }

    public static void popToRootStack(@NonNull FragmentManager fragmentManager) {
        if (fragmentManager == null) return;
        fragmentManager.popBackStack("ROOT_FRAGMENT", 0);
    }

    public static int getStatusBarColor(Activity activity) {
        Window window = activity.getWindow();

        return window.getStatusBarColor();
    }

    public static void setStatusBarColor(Activity activity, int color) {

        NotificationsManagerKt notificationsManager = NotificationsManagerKt.INSTANCE;

        if (notificationsManager.isNotificationShowing()) {
            notificationsManager.updateReturnStatusBarColor(color);
            return;

        } else if (NavigationManager.getInstance().isSplashScreenActive()) { // ignore if splash screen is playing
            return;
        }

        Window window = activity.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(color);
    }

    public static void vibrate(@NonNull Context context, long duration) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(duration);
        }
    }
}
