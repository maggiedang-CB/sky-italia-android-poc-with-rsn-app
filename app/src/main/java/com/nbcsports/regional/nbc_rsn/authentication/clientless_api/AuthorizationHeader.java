package com.nbcsports.regional.nbc_rsn.authentication.clientless_api;

import android.util.Base64;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AuthorizationHeader {
    // FIXME: need better way to store api keys;
    public static final String PUBLIC_KEY = "eVTTPODyzBPBqRm95zHqgVAZaINXoNgK";
    public static final String SECRET_KEY = "Zk9cAN0tltZlHKNT";

    public static String generateAuthorization(String verb, String requestorId, String uri){

        String digitalSignature = String.format("%s requestor_id=%s, nonce=%s, signature_method=HMAC-SHA1, request_time=%s, request_uri=%s",
                verb, requestorId, getNonce(), getRequestTime(), uri);

        HmacUtils hm1 = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, SECRET_KEY); // use a valid name here!

        String signature = encode(SECRET_KEY, digitalSignature);

        String authorization = String.format("%s, public_key=%s, signature=%s", digitalSignature, PUBLIC_KEY, signature);

        return authorization;
    }


    private static String encode(String key, String data) {
        Mac hmac = null;
        String base64 = null;
        try {

            // hmac sha 1
            hmac = Mac.getInstance(String.valueOf(HmacAlgorithms.HMAC_SHA_1));
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), String.valueOf(HmacAlgorithms.HMAC_SHA_1));
            hmac.init(secret_key);
            byte[] encoded = hmac.doFinal(data.getBytes("UTF-8"));

            // base 64
            base64 = Base64.encodeToString(encoded, Base64.NO_WRAP);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64;
    }

    private static String getNonce(){
        String nonce = UUID.randomUUID().toString();
        return nonce;
    }

    private static long getRequestTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
        return calendar.getTimeInMillis();
    }
}
