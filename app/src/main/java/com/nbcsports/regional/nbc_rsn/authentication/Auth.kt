package com.nbcsports.regional.nbc_rsn.authentication

import com.nbcsports.regional.nbc_rsn.authentication.clientless_response.*
import com.nbcsports.regional.nbc_rsn.common.Config
import okhttp3.ResponseBody

data class Auth(
        var isCheckAuthNSuccess: Boolean = false,
        var isCreateRegistrationRecordSuccessful: Boolean = false,
        private val registrationRecord: ResponseBody? = null,
        var regCode: String? = null,
        var redirectUrl: String? = null,
        var authNToken: AuthNToken? = null,
        var authZToken: AuthZToken? = null,
        var shortMediaToken: ShortMediaToken? = null,
        var resourcePassNBCXml: String? = null,
        var requestorId: String? = null,
        var nbcToken: NbcToken? = null,
        var landScapeLogoUrl: String? = null,
        var teamViewLogoUrl: String? = null,
        var mvpdRedirectUrl: String? = null,
        var userMetadata: UserMetadata? = null
) {


    fun isTempPass(config: Config) : Boolean {
        if (this.authZToken == null) {
            return false
        }

        return this.authZToken?.mvpd.equals(config.adobePass.tempPassProvider, ignoreCase = true)
    }

    // testing on authn for temp pass, needs to be merged with isTempPass which is testing on authz
    fun isTempPassAuthN(config: Config) : Boolean {
        if (this.authNToken == null) {
            return false
        }

        return this.authNToken?.mvpd.equals(config.adobePass.tempPassProvider, ignoreCase = true)
    }

    override fun toString(): String {
        return ("Auth{authNToken= $authNToken, authZToken=$authZToken, nbcToken=$nbcToken}")
    }
}
