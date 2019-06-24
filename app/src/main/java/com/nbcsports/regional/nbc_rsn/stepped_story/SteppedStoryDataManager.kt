package com.nbcsports.regional.nbc_rsn.stepped_story

import com.google.gson.Gson
import com.nbcsports.regional.nbc_rsn.stepped_story.components.SteppedStoryFeed
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class SteppedStoryDataManager {

    companion object {
        @Volatile
        private var instance: SteppedStoryDataManager? = null

        @Synchronized
        fun getInstance(): SteppedStoryDataManager? {
            if (instance == null) {
                instance = SteppedStoryDataManager()
            }

            return instance
        }

        @JvmStatic
        fun release() {
            instance = null
        }
    }

    fun getSteppedStoryDetailsFromServer(url: String, gson: Gson): Observable<SteppedStoryFeed> {
        return Observable.create<SteppedStoryFeed> { emitter ->
            try {
                val client = OkHttpClient()

                val request = Request.Builder()
                        .url(url)
                        .get()
                        .build()

                val response = client.newCall(request).execute()
                val data = parseResponse(response, gson)
                emitter.onNext(data)
                emitter.onComplete()
            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    fun parseResponse(response: Response, gson: Gson): SteppedStoryFeed {
        if (!response.isSuccessful) {
            throw IOException()
        }

        return gson.fromJson(response.body()?.charStream(), SteppedStoryFeed::class.java)
    }
}