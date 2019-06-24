package com.nbcsports.regional.nbc_rsn.data_menu.api

import com.nbcsports.regional.nbc_rsn.data_menu.models.*
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DataMenuRotoAPI {
    @GET("roster")
    fun getRotoRoster(
            @Query("league") league: String = "",
            @Query("teamId") teamId: Int
    ): Observable<Response<RotoResponse<RotoPlayer>>>

    @GET("schedule")
    fun getRotoSchedule(
            @Query("league") league: String = "",
            @Query("teamId") teamId: Int,
            @Query("season") season: Int
    ): Observable<Response<RotoResponse<RotoSchedule>>>

    @GET("standings")
    fun getRotoStandings(
            @Query("league") league: String = "",
            @Query("season") season: Int
    ): Observable<Response<RotoResponse<RotoStandings>>>

    // Scores
    @GET("scores")
    fun getRotoScoresMLB(
            @Query("league") league: String = "",
            @Query("gameId") gameId: Int
    ): Observable<Response<RotoResponseScores<BoxScoreMLB>>>

    @GET("scores")
    fun getRotoScoresNBA(
            @Query("league") league: String = "",
            @Query("gameId") gameId: Int
    ): Observable<Response<RotoResponseScores<BoxScoreNBA>>>

    @GET("scores")
    fun getRotoScoresNFL(
            @Query("league") league: String = "",
            @Query("gameId") gameId: Int
    ): Observable<Response<RotoResponseScores<BoxScoreNFL>>>

    @GET("scores")
    fun getRotoScoresNHL(
            @Query("league") league: String = "",
            @Query("gameId") gameId: Int
    ): Observable<Response<RotoResponseScores<BoxScoreNHL>>>

    // Carousel
    @GET("carousel")
    fun getRotoCarouselMLB(
            @Query("league") league: String = "",
            @Query("teamId") teamId: Int
    ): Observable<Response<RotoResponse<RotoMLBCarousel>>>

    @GET("carousel")
    fun getRotoCarouselNBA(
            @Query("league") league: String = "",
            @Query("teamId") teamId: Int
    ): Observable<Response<RotoResponse<RotoNBACarousel>>>

    @GET("carousel")
    fun getRotoCarouselNFL(
            @Query("league") league: String = "",
            @Query("teamId") teamId: Int
    ): Observable<Response<RotoResponse<RotoNFLCarousel>>>

    @GET("carousel")
    fun getRotoCarouselNHL(
            @Query("league") league: String = "",
            @Query("teamId") teamId: Int
    ): Observable<Response<RotoResponse<RotoNHLCarousel>>>
}
