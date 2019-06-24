package com.nbcsports.regional.nbc_rsn.authentication;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.RsnApplication;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.AuthInfo;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelper;
import com.nbcsports.regional.nbc_rsn.analytics.kochava.KochavaAnalytic;
import com.nbcsports.regional.nbc_rsn.analytics.kochava.KochavaContract;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.common.MvpdProviderUrl;
import com.nbcsports.regional.nbc_rsn.common.MvpdRequestor;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.common.TeamManager;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Landscape;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;
import com.nbcsports.regional.nbc_rsn.utils.KeyboardUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class StreamAuthenticationFragment extends BaseFragment {

    public static final String MEDIA_SOURCE = "media.source";
    public static final String IS_247 = "is247";

    @BindView(R.id.provider_logo_grid)
    GridView providerLogoGrid;

    @BindView(R.id.provider_name_recycler_view)
    RecyclerView providerNameRecyclerView;

    @BindView(R.id.provider_search_field)
    EditText providerSearchField;

    @BindView(R.id.authentication_scroll_view)
    NestedScrollView scrollView;

    @BindView(R.id.exit_button)
    ImageView exitButton;

    @BindView(R.id.above_search)
    LinearLayout aboveSearch;

    @BindView(R.id.loading_screen)
    RelativeLayout loadingScreen;

    @BindView(R.id.fragment_stream_authentication)
    FrameLayout authentication;

    private ProviderNameAdapter providerNameAdapter;
    private String providerJsonUrl;
    private Gson gson = new Gson();
    private Type providerListType = new TypeToken<List<MvpdProviderUrl>>() {
    }.getType();
    private Type requestorListType = new TypeToken<List<MvpdRequestor>>() {
    }.getType();
    private ArrayList<String> mvpdPremium = new ArrayList<>();
    private boolean isClickable = true;
    private boolean isComeFrom247Player = false;
    private final int ITEM_REQUIRED_TO_FULFILL_THE_PAGE = 10;

    View.OnTouchListener searchFieldListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.getId() == providerSearchField.getId()) {

                providerNameRecyclerView.setNestedScrollingEnabled(false);
                providerSearchField.requestFocus();
                providerSearchField.setCursorVisible(true);
                scrollView.scrollTo(0, aboveSearch.getBottom());
            } else {
                providerSearchField.clearFocus();
                providerSearchField.setCursorVisible(false);
                providerNameRecyclerView.setNestedScrollingEnabled(true);
                KeyboardUtils.hideKeyboard(getActivity());
            }
            return false;
        }
    };

    private ArrayList<ProviderInfo> providerInfoList = new ArrayList<>();
    private ProviderInfo[] topProviderInfoList;

    private StreamAuthenticationContract.Presenter presenter;
    private MediaSource mediaSource;
    private PersistentPlayer persistentPlayer;
    private ProviderInfo selectedProvider;
    private KochavaContract.Presenter kochava;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        persistentPlayer = com.nbcsports.regional.nbc_rsn.persistentplayer.Injection.providePlayer((PersistentPlayerContract.Main.View) getActivity());
        presenter = Injection.provideStreamAuthentication((StreamAuthenticationContract.View) getActivity());
        kochava = KochavaAnalytic.Injection.provideKochava((KochavaContract.View) getActivity());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateView();
        providerSearchField.setOnTouchListener(searchFieldListener);
        scrollView.setOnTouchListener(searchFieldListener);
        providerSearchField.addTextChangedListener(providerSearchFieldWatcher);
        providerSearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                providerSearchField.setCursorVisible(false);
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    KeyboardUtils.hideKeyboard(getActivity());
                }
                return false;
            }
        });
        providerSearchField.addTextChangedListener(providerSearchFieldWatcher);

        MediaSource ms = getArguments().getParcelable(MEDIA_SOURCE);
        isComeFrom247Player = getArguments().getBoolean(IS_247);

        // If there is no requestor id in media source let's attempt to get it from the team.
        // This fix the scenario where some video assets in editorial details requiring authentication
        // to watch
        if (StringUtils.isEmpty(ms.getRequestorId())){
            Team team = TeamManager.Companion.getInstance().getSelectedTeam();
            if (team != null){
                ms.setRequestorId(team.getRequestorId());
            }
        }

        setPresenter(ms);

        TrackingHelper.Companion.trackPageEvent(getPageInfo());
    }

    @Override
    public void onResume() {
        super.onResume();
        providerSearchField.clearFocus();
        providerSearchField.setCursorVisible(false);
        isClickable = true;
        //Re-enable the name text view
        providerSearchField.setText(providerSearchField.getText());



        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (presenter != null && presenter.isStarted()){

            presenter.pollAuthNToken(mediaSource.getRequestorId(), mediaSource, new TokenListener() {
                @Override
                public void onSuccess(Auth auth) {
                    if (auth != null && auth.getAuthNToken() != null) {
                        String mvpd = auth.getAuthNToken().getMvpd();
                        kochava.trackAuthSuccess(mvpd);
                        AuthInfo info = new AuthInfo("Pass:Authenticate:Success", "nbcs.passauthensuccess",mvpd, "Authenticated", "", mediaSource.getRequestorId(), auth.getAuthNToken().getUserId());
                        TrackingHelper.Companion.trackAuthEvent(info);
                    }
                    Timber.d("This is the enter point: is247: %s %s", persistentPlayer.getCurrentView(), isComeFrom247Player);
                    // Check if it is 247
                    // If it is, then need to show the 247 landscape
                    // Otherwise, just close the StreamAuthenticationFragment
                    if (persistentPlayer.getCurrentView() != null
                            && (persistentPlayer.getCurrentView() instanceof Landscape)
                            && isComeFrom247Player) {
                        persistentPlayer.showAs247();
                    } else {
                        if (getActivity() != null) {
                            ((MainActivity) getActivity()).closeStreamAuthenticationFragment();
                        }
                    }
                }

                @Override
                public void onError(Throwable e) {
                    NotificationsManagerKt.INSTANCE.showAuthNError();
                    if (getActivity() != null) {
                        ((MainActivity) getActivity()).closeStreamAuthenticationFragment();
                    }
                }
            });
            loadingScreen.setVisibility(View.VISIBLE);

        } else {
            loadingScreen.setVisibility(View.GONE);
        }
    }

    private final TextWatcher providerSearchFieldWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            filteredProviderInfoList();
        }
    };

    @Override
    public int getLayout() {
        return R.layout.fragment_stream_authentication;
    }

    @OnClick(R.id.exit_button)
    public void exitStreamAuth() {
        providerSearchField.setCursorVisible(false);
        KeyboardUtils.hideKeyboard(getActivity());
        NavigationManager.getInstance().closeStreamAuthenticationFragment(false);
        presenter.pollAuthNtokenStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        exitButton.setClickable(false);
        if (!hidden) {
            ((MainActivity) getActivity()).hideFab();
            updateView();
        }
    }

    private void filteredProviderInfoList() {
        ArrayList<ProviderInfo> matchedProviderList = new ArrayList<>();
        providerNameRecyclerView.setNestedScrollingEnabled(true);
        for (ProviderInfo companyInfo : providerInfoList) {
            if (companyInfo.getMvpdName() != null && companyInfo.getMvpdName().toLowerCase().contains(providerSearchField.getText().toString().toLowerCase())) {
                matchedProviderList.add(companyInfo);
            }
        }
        if (providerNameAdapter != null && providerNameRecyclerView != null) {
            providerNameAdapter.setCompanyInfo(matchedProviderList);
            //Scroll to top of the list upon every user input
            providerNameRecyclerView.smoothScrollToPosition(0);
            if (matchedProviderList.size() < ITEM_REQUIRED_TO_FULFILL_THE_PAGE) {
                providerNameRecyclerView.setNestedScrollingEnabled(false);
            }
        }
    }

    private void updateView() {
        //Check to see if Json file is already loaded
        exitButton.setClickable(true);
        if (providerInfoList.size() == 0) {
            loadingScreen.setVisibility(View.VISIBLE);
            getPremiumInformationFromJson();
        } else {
            loadingScreen.setVisibility(View.GONE);
        }
        //Reset to the top of the fragment.
        scrollView.scrollTo(0, 0);
    }

    public String getProviderJsonUrl() {
        if (((MainActivity) getActivity()).getConfig() != null) {
            Config config = ((MainActivity) getActivity()).getConfig();
            return config.getMvpdProviders().getLogosUrl();
        }
        return null;
    }

    public String getRequestorIdsUrl() {
        if (((MainActivity) getActivity()).getConfig() != null) {
            Config config = ((MainActivity) getActivity()).getConfig();
            return config.getMvpdProviders().getRequestorIds();
        }
        return null;
    }

    private void getPremiumInformationFromJson() {
        providerJsonUrl = getProviderJsonUrl();
        if (providerJsonUrl != null && getRequestorIdsUrl() != null) {
            getPremiumFromUrl(getRequestorIdsUrl())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<MvpdRequestor>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onNext(List<MvpdRequestor> mvpdRequestors) {

                            if (mvpdRequestors == null) {
                                return;
                            }

                            if(mediaSource.getRequestorId() == null) {
                                return;
                            }

                            for (MvpdRequestor mvpdRequestor : mvpdRequestors) {
                                if (mvpdRequestor.getMvpdPremium() == null) {
                                    return;
                                }

                                if (mvpdRequestor.getRequestorID().equals(mediaSource.getRequestorId())) {
                                    Timber.d("MvpdPremium are: %s", mvpdRequestor.getMvpdPremium());
                                    String[] mvpdPremiumArray = mvpdRequestor.getMvpdPremium().split(",");
                                    mvpdPremium.addAll(Arrays.asList(mvpdPremiumArray));
                                    if(mvpdPremium.contains(getActivity().getString(R.string.xfinity_id))) {
                                        //Move Xfinity to first grid
                                        mvpdPremium.remove(mvpdPremium.indexOf(getString(R.string.xfinity_id)));
                                        mvpdPremium.add(0, getString(R.string.xfinity_id));
                                    }
                                }
                                topProviderInfoList = new ProviderInfo[mvpdPremium.size()];
                            }

                            getCompanyInformationFromJson();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.e(e);
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        }
    }

    private void getCompanyInformationFromJson() {
        getProviderFromUrl(providerJsonUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<MvpdProviderUrl>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<MvpdProviderUrl> mvpdProviderUrls) {

                        if (mvpdProviderUrls == null) {
                            return;
                        }
                        for (MvpdProviderUrl mvpdProviderUrl : mvpdProviderUrls) {
                            if (mvpdProviderUrl.getProviderName() == null) {
                                return;
                            }
                            if (!mvpdProviderUrl.getProviderName().isEmpty()) {
                                ProviderInfo company = new ProviderInfo(mvpdProviderUrl.getProviderName(), mvpdProviderUrl.getProviderID(), mvpdProviderUrl.getStatus(), mvpdProviderUrl.getMvpdUrl());
                                String providerId = mvpdProviderUrl.getProviderID();
                                providerInfoList.add(company);
                                if (mvpdPremium.contains(providerId)) {
                                    company.setLogoUrl(mvpdProviderUrl.getLargeLogo());
                                    //Remove the null object and add in new provider info to correct index
                                    topProviderInfoList[mvpdPremium.indexOf(providerId)] = company;
                                }
                            }
                        }
                        Collections.sort(providerInfoList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {
                        initCompanyLogoRecyclerView();
                        initCompanyNameRecyclerView();
                    }
                });
    }

    private Observable<List<MvpdRequestor>> getPremiumFromUrl(String url) {
        final OkHttpClient client = new OkHttpClient();

        // TODO: handle blank or incorrect URLs so the app doesn't crash
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return Observable.create(emitter -> {
            try {
                Response response = client.newCall(request).execute();
                ArrayList<MvpdRequestor> mvpdRequestors = gson.fromJson(response.body().charStream(), requestorListType);
                emitter.onNext(mvpdRequestors);
                emitter.onComplete();
            } catch (IOException e) {
                Timber.e(e);
                emitter.onError(e);
            }
        });
    }

    private Observable<List<MvpdProviderUrl>> getProviderFromUrl(String url) {
        final OkHttpClient client = new OkHttpClient();

        // TODO: handle blank or incorrect URLs so the app doesn't crash
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return Observable.create(emitter -> {
            try {
                Response response = client.newCall(request).execute();
                ArrayList<MvpdProviderUrl> mvpdProviderUrls = gson.fromJson(response.body().charStream(), providerListType);
                emitter.onNext(mvpdProviderUrls);
                emitter.onComplete();
            } catch (IOException e) {
                Timber.e(e);
                emitter.onError(e);
            }
        });
    }

    private void initCompanyNameRecyclerView() {
        if (providerNameRecyclerView != null) {
            providerNameRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            ProviderNameListener listener = new ProviderNameListener() {

                @Override
                public void onItemClick(ProviderInfo providerInformation) {
                    if (mediaSource == null){ return; }
                    if (providerInformation.getMvpdName().equalsIgnoreCase(RsnApplication.getInstance().getResources().getString(R.string.authentication_search_no_result_found))) {
                        String emptyText = "";
                        providerSearchField.setText(emptyText);
                        filteredProviderInfoList();
                        Timber.d("Name clicked, Provider ID: %s", providerInformation.getMvpdName());

                    } else if (!providerInformation.getMvpdName().equalsIgnoreCase(RsnApplication.getInstance().getResources().getString(R.string.stream_authentication_empty_name_indicator))) {

                        presenter.doAuthN(providerInformation.getMvpdID(), mediaSource.getRequestorId());
                        loadingScreen.setVisibility(View.VISIBLE);
                        Timber.d("Name clicked, Provider ID: %s", providerInformation.getMvpdName());
                    }
                    selectedProvider = providerInformation;
                    String customLink = "Pass:Select:" + selectedProvider.getMvpdID();
                    String requestorId = "";
                    if(mediaSource != null) {
                        requestorId = mediaSource.getRequestorId();
                    }
                    AuthInfo info = new AuthInfo(customLink, "nbcs.passselected",selectedProvider.getMvpdID(),
                            "", "", requestorId,"");
                    TrackingHelper.Companion.trackAuthEvent(info);
                }
            };
            //Pass by reference, keep providerInfo as original arraylist
            ArrayList<ProviderInfo> editableProviderInfoList= new ArrayList<ProviderInfo>(providerInfoList);
            providerNameAdapter = new ProviderNameAdapter(editableProviderInfoList, listener, ITEM_REQUIRED_TO_FULFILL_THE_PAGE);
            providerNameAdapter.setProviderInfoListener(listener);
            providerNameRecyclerView.setAdapter(providerNameAdapter);
            providerNameRecyclerView.setOnTouchListener(searchFieldListener);
            loadingScreen.setVisibility(View.GONE);
        }
    }

    private void initCompanyLogoRecyclerView() {
        if (providerLogoGrid != null) {
            ProviderLogoAdapter providerLogoAdapter = new ProviderLogoAdapter(topProviderInfoList);
            providerLogoGrid.setAdapter(providerLogoAdapter);

            providerLogoGrid.setOnItemClickListener((parent, v, position, id) -> {

                ProviderInfo provider = topProviderInfoList[position]; // might cause: ArrayIndexOutOfBoundsException: length=0; index=0, if providers grid gets not filled in with providers icons yet

                String customLink = "Pass:Select:" + provider.getMvpdID();
                String requestorId = "";
                if(mediaSource != null) {
                    requestorId = mediaSource.getRequestorId();
                }
                AuthInfo info = new AuthInfo(customLink, "nbcs.passselected", provider.getMvpdID(),
                        "", "", requestorId,"");
                TrackingHelper.Companion.trackAuthEvent(info);
                if (mediaSource == null){ return; }

                if (isClickable = true) {

                    presenter.doAuthN(provider.getMvpdID(), mediaSource.getRequestorId());
                    loadingScreen.setVisibility(View.VISIBLE);
                    Timber.d("Logo clicked, Provider ID: %s", provider.getMvpdName());
                }
                isClickable = false;
            });
        }
    }

    private void setPresenter(MediaSource mediaSource) {

        this.mediaSource = mediaSource;
        this.presenter.setView(this);
    }

    @Override
    public PageInfo getPageInfo() {
        String business = "";
        Team team = TeamManager.Companion.getInstance().getSelectedTeam();

        if(team != null) {
            business = team.getRegionName();
        }
        return new PageInfo(true, business, "", "Adobe Pass", "Provider List", "", business, "", "", "", "");
    }
}
