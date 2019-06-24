package com.nbcsports.regional.nbc_rsn.utils

import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

object PreferenceUtils {

    var rxPreferences: RxSharedPreferences? = null

    fun setBoolean(key: String, value: Boolean) {
        val pref = rxPreferences?.getBoolean(key)
        pref?.set(value)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val pref = rxPreferences?.getBoolean(key, defaultValue)
        return pref?.get() ?: defaultValue
    }

    fun setString(key: String, value: String) {
        val pref = rxPreferences?.getString(key)
        pref?.set(value)
    }

    fun getString(key: String, defaultValue: String): String {
        val pref = rxPreferences?.getString(key, defaultValue)
        return pref?.get() ?: defaultValue
    }

    fun setInt(key: String, value: Int) {
        val pref = rxPreferences?.getInteger(key)
        pref?.set(value)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        val pref = rxPreferences?.getInteger(key, defaultValue)
        return pref?.get() ?: defaultValue
    }

    fun setLong(key: String, value: Long) {
        val pref = rxPreferences?.getLong(key)
        pref?.set(value)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        val pref = rxPreferences?.getLong(key, defaultValue)
        return pref?.get() ?: defaultValue
    }

    fun <T> setList(key: String, list: ArrayList<T>) {
        val json = Gson().toJson(list)
        val pref = rxPreferences?.getString(key, "")
        pref?.set(json)
    }

    fun <T: Any> getList(key: String, elementType: Type, defaultValue: ArrayList<T>): ArrayList<T> {
        val storedValue = getString(key, "")
        return if (storedValue.isNullOrEmpty()) {
            defaultValue
        } else {
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(List::class.java, elementType)
            val adapter = moshi.adapter<ArrayList<T>>(type)

            return adapter.fromJson(storedValue) ?: defaultValue
        }
    }
}
