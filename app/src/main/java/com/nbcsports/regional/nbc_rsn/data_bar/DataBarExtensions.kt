package com.nbcsports.regional.nbc_rsn.data_bar

import org.joda.time.*

fun Event.isYesterday(): Boolean {
    val eventDate = DateTime(startDate[0].full)
    val yesterday = Interval(DateTime.now().minusDays(1).withTimeAtStartOfDay(), Days.ONE)
    return yesterday.contains(eventDate)
}

fun Event.isToday(): Boolean {
    val eventDate = DateTime(startDate[0].full)
    val today = Interval(DateTime.now().withTimeAtStartOfDay(), Days.ONE)
    return today.contains(eventDate)
}

fun Event.isWithin12Hours(): Boolean {
    val now = DateTime()
    val eventTime = DateTime(startDate[0].full)
    val last12Hours = Interval(now.minusHours(12), now)
    return last12Hours.contains(eventTime)
}

fun Event.isPreGame(): Boolean {
    return eventStatus?.eventStatusId in listOf(GameState.PRE_GAME.eid, GameState.POSTPONED.eid)
}

fun Event.isPreGameExact(): Boolean {
    return eventStatus?.eventStatusId == GameState.PRE_GAME.eid
}

fun Event.isLive(): Boolean {
    return eventStatus?.eventStatusId in listOf(GameState.IN_PROGRESS.eid, GameState.DELAYED.eid)
}

fun Event.isPostGame(): Boolean {
    return eventStatus?.eventStatusId in listOf(GameState.CANCELLED.eid, GameState.SUSPENDED.eid, GameState.FINAL.eid)
}

fun GameState.isPreGame() : Boolean {
    return eid in listOf(GameState.PRE_GAME.eid, GameState.POSTPONED.eid)
}

fun GameState.isLive() : Boolean {
    return eid in listOf(GameState.IN_PROGRESS.eid, GameState.DELAYED.eid)
}

fun GameState.isPostGame() : Boolean {
    return eid in listOf(GameState.CANCELLED.eid, GameState.SUSPENDED.eid, GameState.FINAL.eid)
}