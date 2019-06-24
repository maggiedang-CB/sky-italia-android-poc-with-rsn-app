package com.nbcsports.regional.nbc_rsn.data_bar

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DataBarAPI {

    @GET("{sportName}/{league}/scores/teams/{teamId}")
    fun getScheduleScoreForOneTeam(
            @Path("sportName") sportName: String,
            @Path("league") league: String,
            @Path("teamId") teamId: Int,
            @Query("accept") accept: String
    ): Observable<Response<DataBarModel>>

    @GET("{sportName}/{league}/events/teams/{teamId}")
    fun getEvents(
            @Path("sportName") sportName: String,
            @Path("league") league: String,
            @Path("teamId") teamId: Int,
            @Query("startDate") startDate: String,
            @Query("endDate") endDate: String,
            @Query("linescore") linescore: String
    ): Observable<Response<DataBarModel>>

    @GET("{sportName}/{league}/standings/teams/{teamId}")
    fun getStanding(
            @Path("sportName") sportName: String,
            @Path("league") league: String,
            @Path("teamId") teamId: Int,
            @Query("season") season: Int
    ): Observable<Response<DataBarModel>>
}