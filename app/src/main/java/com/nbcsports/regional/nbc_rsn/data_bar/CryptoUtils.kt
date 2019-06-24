package com.nbcsports.regional.nbc_rsn.data_bar

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class CryptoUtils {

    companion object {
        @JvmStatic
        fun sha256(text: String): String {
            try {
                val md = MessageDigest.getInstance("SHA-256")

                md.update(text.toByteArray())
                val digest: ByteArray = md.digest()

                return String.format("%064x", BigInteger(1, digest))

            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }

            return ""
        }
    }
}