package com.nbcsports.regional.nbc_rsn.editorial_detail

import com.google.gson.Gson
import com.nbcsports.regional.nbc_rsn.common.EditorialDetailsFeed
import com.nbcsports.regional.nbc_rsn.deeplink.Deeplink
import com.nbcsports.regional.nbc_rsn.editorial_detail.models.EditorialDetailItem
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Function
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class EditorialDataManager {

    companion object {
        @Volatile
        private var instance: EditorialDataManager? = null

        @Synchronized
        fun getInstance(): EditorialDataManager? {
            if (instance == null) {
                instance = EditorialDataManager()
            }

            return instance
        }

        @JvmStatic
        fun release() {
            instance = null
        }
    }

    fun getEditorialDetailsFromServer(url: String, gson: Gson): Observable<EditorialDetailsFeed> {
        val client = OkHttpClient()

        // TODO: handle blank or incorrect URLs so the app doesn't crash
        val request = Request.Builder()
                .url(url)
                .get()
                .build()

        return Observable.create<EditorialDetailsFeed> { emitter ->
            try {
                val response = client.newCall(request).execute()
                val editorialDetailsFeed = parseResponse(response, gson)
                emitter.onNext(editorialDetailsFeed)
                emitter.onComplete()
            } catch (ex: IOException) {
                emitter.onError(ex)
            }
        }
    }

    fun setDeeplinkToMediaSource(teamId: String, componentId: String): Function<EditorialDetailsFeed, ObservableSource<EditorialDetailsFeed>> {
        return Function { editorialDetailsFeed: EditorialDetailsFeed ->

            val assetsUsersCannotDeeplink = ArrayList<EditorialDetailItem>() // remove assets if user is unable to deeplink to it.

            for (item in editorialDetailsFeed.editorialDetail.components) {
                val mediaSource = item.mediaSource

                if (mediaSource != null && mediaSource.deeplink == null) {
                    val deeplink = Deeplink.getDeeplinkForEditorial(teamId, componentId)
                    if (deeplink != null) {
                        mediaSource.deeplink = deeplink
                    } else {
                        assetsUsersCannotDeeplink.add(item)
                    }

                }
            }

            editorialDetailsFeed.editorialDetail.components.removeAll(assetsUsersCannotDeeplink)

            return@Function Observable.just<EditorialDetailsFeed>(editorialDetailsFeed)
        }
    }

    @Throws(IOException::class)
    fun parseResponse(response: Response, gson: Gson): EditorialDetailsFeed {
        if (!response.isSuccessful) {
            throw IOException()
        }

        return gson.fromJson(response.body()?.charStream(), EditorialDetailsFeed::class.java)
    }

}
