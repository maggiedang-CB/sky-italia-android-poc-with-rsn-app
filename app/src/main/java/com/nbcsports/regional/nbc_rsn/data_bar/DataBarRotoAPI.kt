package com.nbcsports.regional.nbc_rsn.data_bar

import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoDataBar
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoResponse
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DataBarRotoAPI {
    @GET("dataBar")
    fun getDataBarInfo(
            @Query("league") league: String,
            @Query("teamId") teamId: Int,
            @Query("nextOrPrev") nextOrPrev: String
    ): Observable<Response<RotoResponse<RotoDataBar>>>
}