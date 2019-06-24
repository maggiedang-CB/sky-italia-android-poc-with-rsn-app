package com.nbcsports.regional.nbc_rsn.data_menu.api

import com.nbcsports.regional.nbc_rsn.data_bar.DataBarModel
import com.nbcsports.regional.nbc_rsn.data_menu.models.*
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DataMenuStatsComAPI {

    @GET("football/nfl/stats/teams/{teamId}?accept=json")
    fun getNFLTeamStats(
            @Path("teamId") teamId: Int
    ): Observable<Response<StatsResponse<StatsNFLTeamStats>>>

    @GET("hockey/nhl/stats/teams/{teamId}?accept=json")
    fun getNHLTeamStats(
            @Path("teamId") teamId: Int
    ): Observable<Response<StatsResponse<StatsNHLTeamStats>>>

    @GET("basketball/nba/stats/teams/{teamId}?accept=json")
    fun getNBATeamStats(
            @Path("teamId") teamId: Int
    ): Observable<Response<StatsResponse<StatsNBATeamStats>>>

    @GET("{sportName}/{league}/standings/teams/{teamId}?expanded=true")
    fun getStanding(
            @Path("sportName") sportName: String,
            @Path("league") league: String,
            @Path("teamId") teamId: Int
    ): Observable<Response<DataBarModel>>

    @GET("{sportName}/{league}/scores/teams/{teamId}")
    fun getSchedules(
            @Path("sportName") sportName: String,
            @Path("league") league: String,
            @Path("teamId") teamId: Int,
            @Query("season") season: Int
    ): Observable<Response<DataBarModel>>
}