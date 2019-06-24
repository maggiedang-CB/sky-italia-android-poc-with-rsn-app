package com.nbcsports.regional.nbc_rsn.localization.models

data class Localizations(
        val Common: Common,
        val DataBar: DataBar,
        val DataMenu: DataMenu,
        val Settings: Settings,
        val TeamSelector: TeamSelector,
        val TeamSelectorSuccess: TeamSelectorSuccess,
        val MoreTeamsSelector: MoreTeamsSelector,
        val TeamView: TeamViewLocalization,
        val Authentication: Authentication,
        val VideoPlayer: VideoPlayer,
        val Date: Date,
        val Signup: Signup,
        val Alerts: Alerts,
        val TeamNews: TeamNews,
        val MediaSettings: MediaSettings,
        val KebabMenu: KebabMenu,
        val NativeShareMessages: NativeShareMessages,
        val AppReview: AppReview
)

data class Common(
        val OK: String = "",
        val Cancel: String = "",
        val Retry: String = "",
        val Close: String = "",
        val Author: String = "")

data class DataBar(
        val Today: String = "",
        val LiveInApp: String = "",
        val Postponed: String = "",
        val Cancelled: String = "",
        val Delayed: String = "",
        val Suspended: String = "",
        val FinalScore: String = "",
        val Out: String = "",
        val Outs: String = "",
        val Quarter: String = "",
        val PowerPlay: String = "",
        val Overtime: String = "",
        val Shootout: String = "",
        val Record: String = "")

data class DataMenu(
        val Roster: String = "",
        val Schedule: String = "",
        val Standings: String = "",
        val Score: String = "",
        val League: String = "",
        val Conference: String = "",
        val Division: String = "",
        val Players: String = "",
        val NextGame: String = "",
        val AwayVsHome: String = "",
        val Record: String = "",
        val DivisionRank: String = "",
        val ConferenceRank: String = "",
        val GamesBack: String = "",
        val LastTen: String = "",
        val Streak: String = "",
        val FieldGoalPercent: String = "",
        val EffectiveFieldGoalPercent: String = "",
        val PointsPerGame: String = "",
        val ThreePointPercent: String = "",
        val PointsPercent: String = "",
        val PowerPlayPercent: String = "",
        val PowerPlayGoalsPerGame: String = "",
        val SavePercent: String = "",
        val ShotsAgainstPerGame: String = "",
        val RedZonePercent: String = "",
        val RedZoneScores: String = "",
        val ThirdDownPercent: String = "",
        val ThirdDownEfficiency: String = "",
        val Status: String = "",
        val DivisionLeaders: String = "",
        val WildCard: String = "",
        val RecordAbbreviation: String = "",
        val GamesBackAbbreviation: String = "",
        val PointsAbbreviation: String = "",
        val PercentAbbreviation: String = "",
        val RosterSortSelectionTitle: String = "",
        val ScheduleSeasonSelectionTitle: String = "",
        val DataDetailTitleFormat: String = "",
        val DataDetailUpdatedFormat: String = "",
        val DataDetailUpdated: String = "",
        val ScheduleTBD: String = "",
        val ScheduleFinal: String = "",
        val RosterSortName: String = "",
        val RosterSortPosition: String = "",
        val RosterSortAge: String = "",
        val RosterSortHeight: String = "",
        val RosterSortWeight: String = "",
        val RosterSortJersey: String = "",
        val RosterPoundsUnit: String = "",
        val RosterYearOld: String = "",
        val ClinchedPlayoffs: String = "",
        val ClinchedDivision: String = "",
        val ClinchedConference: String = "",
        val ClinchedHomeField: String = "",
        val ClinchedBye: String = "",
        val ClinchedTrophy: String = "",
        val BoxscoreFinal: String = "",
        val BoxscoreInProgress: String = "",
        val BoxscoreRecentGame: String = "",
        val BoxscoreGameInProgress: String = "",
        val BoxscoreLinescoreTotalAbbreviation: String = "",
        val BoxscoreLinescoreRunsAbbreviation: String = "",
        val BoxscoreLinescoreShootOut: String = "",
        val BoxscoreLinescoreOverTime: String = "",
        val BoxscorePointsAbbreviation: String = "",
        val BoxscoreMLBBatting: String = "",
        val BoxscoreMLBPitching: String = "",
        val BoxscoreMLBAtBatsAbbreviation: String = "",
        val BoxscoreMLBARunsAbbreviation: String = "",
        val BoxscoreMLBAHitsAbbreviation: String = "",
        val BoxscoreMLBABasesOnBallsAbbreviation: String = "",
        val BoxscoreMLBARunsBattedInAbbreviation: String = "",
        val BoxscoreMLBAInningsPitchedAbbreviation: String = "",
        val BoxscoreMLBAEarnedRunsPitchedAbbreviation: String = "",
        val BoxscoreMLBAWalksAllowedPitchedAbbreviation: String = "",
        val BoxscoreMLBAStrikeOutsPitchedAbbreviation: String = "",
        val BoxscoreNBAPlayers: String = "",
        val BoxscoreNBAMinutesAbbreviation: String = "",
        val BoxscoreNBAReboundsAbbreviation: String = "",
        val BoxscoreNBAAssistsAbbreviation: String = "",
        val BoxscoreNFLPassing: String = "",
        val BoxscoreNFLRushing: String = "",
        val BoxscoreNFLReceiving: String = "",
        val BoxscoreNFLDefense: String = "",
        val BoxscoreNFLKickReturns: String = "",
        val BoxscoreNFLPuntReturns: String = "",
        val BoxscoreNFLKicking: String = "",
        val BoxscoreNFLPunting: String = "",
        val BoxscoreNFLAttemptsAbbreviation: String = "",
        val BoxscoreNFLCarriesAbbreviation: String = "",
        val BoxscoreNFLReceptionsAbbreviation: String = "",
        val BoxscoreNFLTacklesAbbreviation: String = "",
        val BoxscoreNFLSacksAbbreviation: String = "",
        val BoxscoreNFLReturnsAbbreviation: String = "",
        val BoxscoreNFLFieldGoalsAbbreviation: String = "",
        val BoxscoreNFLExtraPointAbbreviation: String = "",
        val BoxscoreNFLIn20Abbreviation: String = "",
        val BoxscoreNFLPercentAbbreviation: String = "",
        val BoxscoreNFLLongestAbbreviation: String = "",
        val BoxscoreNFLYardsAbbreviation: String = "",
        val BoxscoreNFLAverageAbbreviation: String = "",
        val BoxscoreNFLTouchdownsAbbreviation: String = "",
        val BoxscoreNFLInterceptionsAbbreviation: String = "",
        val BoxscoreNFLNumberAbbreviation: String = "",
        val BoxscoreNHLSkaters: String = "",
        val BoxscoreNHLGoalie: String = "",
        val BoxscoreNHLGoalsAbbreviation: String = "",
        val BoxscoreNHLAssistsAbbreviation: String = "",
        val BoxscoreNHLShotsOnGoalAbbreviation: String = "",
        val BoxscoreNHLGoalsAgainstAbbreviation: String = "",
        val BoxscoreNHLShotsAgainstAbbreviation: String = "",
        val BoxscoreNHLSavesAbbreviation: String = "",
        val BoxscoreNHLSavePercentAbbreviation: String = "",
        val BoxscoreNHLScoringSummary: String = "",
        val BoxscoreNHLNoGoal: String = "",
        val BoxscoreNHLPeriod: String = "",
        val BoxscoreNHLOvertime: String = ""
)

data class Settings(
        val Settings: String = "",
        val GuestUser: String = "",
        val InstallSince: String = "",
        val MyTeams: String = "",
        val EditReorder: String = "",
        val Notifications: String = "",
        val AllowNotifications: String = "",
        val BreakingNews: String = "",
        val TeamNews: String = "",
        val Data: String = "",
        val MediaSettings: String = "",
        val Support: String = "",
        val FAQ: String = "",
        val UpdateApp: String = "",
        val About: String = "",
        val Feedback :String = "",
        val Privacy: String = "",
        val TermsUse: String = "",
        val Share: String = "",
        val ViewLess: String = "",
        val ViewMore: String = "",
        val FabigationHandedness: String = "",
        val LogOut: String = "",
        val LogOutCTA: String = "",
        val LogOutConfirmTitle: String = "",
        val LogOutConfirmText: String = "")

data class TeamSelector(
        val MyTeams: String = "",
        val RemoveAll: String = "",
        val SelectAll: String = "",
        val SaveTeams: String = "",
        val HoldAndDrag: String = "")

data class TeamSelectorSuccess(
        val RotatedText: String = "",
        val TitleText: String = "",
        val SubtitleText: String = "")

data class MoreTeamsSelector(
        val MoreTeams: String = "",
        val MoreTeamsCard: String = "",
        val NumberOfTeamsSingular: String = "",
        val NumberOfTeamsPlural: String = "")

data class TeamViewLocalization(
        val TheFeed: String = "",
        val LiveFeedPromo: String = "",
        val UpNext: String = "")

data class Authentication(
        val TimeOut: String = "",
        val Incomplete: String = "")

data class VideoPlayer(
        val Live: String = "",
        val Close: String = "",
        val GoLive: String = "",
        val LastViewed: String = "",
        val SignIn: String = "",
        val PreviewRemaining: String = "",
        val PreviewEndedTitle: String = "",
        val PreviewEndedDescription: String = "",
        val PreviewUnavailableTitle: String = "",
        val PreviewUnavailableDescription: String = "")

data class Date(
        val MinutesAgoSingular: String = "",
        val MinutesAgoPlural: String = "",
        val HoursAgoSingular: String = "",
        val HoursAgoPlural: String = "",
        val DaysAgoSingular: String = "",
        val DaysAgoPlural: String = "")

data class Signup(
        val SignUpMessage: String = "",
        val Facebook: String = "",
        val Twitter: String = "",
        val Email: String = "",
        val Connect: String = "",
        val Signup: String = "",
        val Or: String = "",
        val AlreadyHaveAccount: String = "",
        val TermsConditionPrivacy: String = "",
        val FirstName: String = "",
        val LastName: String = "",
        val ZipLocation: String = "",
        val Password: String = "",
        val Subscribe: String = "",
        val LiveStreaming: String = "",
        val Optional: String = "",
        val NBCNewsletter: String = "",
        val CableUseQuestion: String = "",
        val CreateAccount: String = "",
        val CreateAccountTerms: String = "",
        val InvalidName: String = "",
        val InvalidEmail: String = "",
        val AlreadyUsedEmail: String = "",
        val InvalidPassword: String = "",
        val InvalidPhone: String = "",
        val InvalidZip: String = "",
        val InvalidSocial: String = "")

data class Alerts(
        val NetworkError: NetworkError,
        val PlaybackError: PlaybackError,
        val AuthorizeError: AuthorizeError,
        val ServerError: ServerError,
        val WifiError: WifiError,
        val EmailError: EmailError,
        val AppUpToDate: AppUpToDate,
        val AuthError: AuthError,
        val LogOut: LogOut,
        val CellularStreamError: CellularStreamError,
        val FTUEFlick: FTUEFlick,
        val FTUETap: FTUETap,
        val FTUEDataMenu: FTUEDataMenu,
        val GMOEntitlementError: GMOEntitlementError,
        val EnableLocationServices: EnableLocationServices,
        val RSNEntitlementError: RSNEntitlementError,
        val ChromecastFailed: ChromecastFailed,
        val ChromecastError: ChromecastError,
        val ChromecastEnded: ChromecastEnded,
        val ChromecastAssetError: ChromecastAssetError
)

data class NetworkError(
        val Title: String = "",
        val Message: String = "")

data class PlaybackError(
        val Title: String = "",
        val Message: String = "")

data class AuthorizeError(
        val Title: String = "",
        val Message: String = "")

data class ServerError(
        val Title: String = "",
        val Message: String = "")

data class WifiError(
        val Title: String = "",
        val Message: String = "")

data class EmailError(
        val Title: String = "",
        val Message: String = "")

data class AppUpToDate(
        val Title: String = "",
        val Message: String = "")

data class AuthError(
        val Title: String = "",
        val Message: String = "")

data class LogOut(
        val Title: String = "",
        val Message: String = "")

data class CellularStreamError(
        val Title: String = "",
        val Message: String = "")

data class FTUEFlick(
        val Title: String = "",
        val Message: String = "")

data class FTUETap(
        val Title: String = "",
        val Message: String = "")

data class FTUEDataMenu(
        val Title: String = "",
        val Message: String = "")

data class GMOEntitlementError(
        val Title: String = "",
        val Message: String = "")

data class EnableLocationServices(
        val Title: String = "",
        val Message: String = "")

data class RSNEntitlementError(
        val Title: String = "",
        val Message: String = "")

data class ChromecastFailed(
        val Title: String = "",
        val Message: String = "")

data class ChromecastError(
        val Title: String = "",
        val Message: String = "")

data class ChromecastEnded(
        val Title: String = "",
        val Message: String = "")

data class ChromecastAssetError(
        val Title: String = "",
        val Message: String = "")

data class TeamNews(
        val TeamNews: String = "",
        val EditNotificationSettings: String = "",
        val AllChangedSaved: String = "",
        val All: String = "",
        val GameStart: String = "",
        val FinalScore: String = "")

data class MediaSettings(
        val MediaSettings: String = "",
        val LiveVideoPreferences: String = "",
        val CellularDataForLiveStream: String = "",
        val AllowTheUseOfCellularDataForLiveStreaming: String = "",
        val AutoPlayLiveGames: String = "",
        val MediaStreamPreferences: String = "",
        val CellularDataForMediaStream: String = "",
        val AllowTheUseOfCellularDataForNonLiveVideoAndAudioStreaming: String = "",
        val AutoPlayVideos: String = "")

data class KebabMenu(
        val Chromecast: String = "",
        val ChromecastConnecting: String = "",
        val Share: String = "",
        val Mute: String = "",
        val Unmute: String = "",
        val Airplay: String = "",
        val CCOn: String = "",
        val CCOff: String = "")

data class NativeShareMessages(
        val DefaultShareMessage: String = "")

data class AppReview(
        val AreYouEnjoyingApp: String = "",
        val Yes: String = "",
        val No: String = "",
        val Postpone: String = ""
)