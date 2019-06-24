package com.nbcsports.regional.nbc_rsn.authentication;


public class Injection {
    public static StreamAuthenticationContract.Presenter provideStreamAuthentication(StreamAuthenticationContract.View view) {
        try {
            StreamAuthenticationContract.View contractView = view;
            return contractView.getPresenter();
        } catch (ClassCastException castException) {
            return null;
        }
    }
}
