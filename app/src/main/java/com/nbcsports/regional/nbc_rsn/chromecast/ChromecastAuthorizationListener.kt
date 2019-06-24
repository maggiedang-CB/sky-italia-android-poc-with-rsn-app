package com.nbcsports.regional.nbc_rsn.chromecast

import com.nbcsports.regional.nbc_rsn.authentication.Auth

interface ChromecastAuthorizationListener {
    fun onAuthorizationSuccess(auth: Auth)
    fun onAuthorizationFailure(e: Throwable?)
}