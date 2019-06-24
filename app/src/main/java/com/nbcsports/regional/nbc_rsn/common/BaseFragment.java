package com.nbcsports.regional.nbc_rsn.common;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.fabigation.FabMenuInterface;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseFragment extends Fragment {

    public static final String TAG = "BaseFragment";
    private Unbinder unbinder;
    protected CompositeDisposable compositeDisposable;
    protected FabMenuInterface menuInterface;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            menuInterface = (FabMenuInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement FabMenuInterface.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayout(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
    }

    @Override
    public void onDestroyView() {
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
            compositeDisposable.dispose();
        }
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();

    }



    @LayoutRes
    public abstract int getLayout();

    /**
     * This listener will be triggered when the Localization Manager is initialized
     * If Fragments are created after Localization Manager is initialized,
     * then this listener will be useless
     */
    public void onLocalizationManagerInitialized() {}

    protected PageInfo getPageInfo() {
        return null;
    }
}
