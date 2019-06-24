package com.nbcsports.regional.nbc_rsn.localization;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.RsnApplication;
import com.nbcsports.regional.nbc_rsn.localization.models.Localizations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Localization manager
 *
 * Usages: LocalizationManager.Common.OK
 *         LocalizationManager.Common.getAuthor(String)
 */
public class LocalizationManager {

    private static Localizations localizations;
    // Single pattern means in target string,
    // there is only one piece of sub string to be replaced
    private static Pattern regexSinglePattern;
    // Multiple pattern means in target string,
    // there are at least one piece of sub string to be replaced
    private static Pattern regexMultiplePattern;

    private LocalizationManager() {}

    // This method need to be called once before using other methods in LocalizationManager
    public static void init(Localizations newLocalizations) {
        localizations = newLocalizations;
        initSinglePattern();
        initMultiplePattern();
    }

    private static void initSinglePattern() {
        regexSinglePattern = Pattern.compile("\\{.*\\}");
    }

    private static void initMultiplePattern() {
        regexMultiplePattern = Pattern.compile("\\{\\w*\\}");
    }

    public static boolean isInitialized() {
        return localizations != null;
    }

    public static class Common {

        public final static String OK     = localizations.getCommon().getOK();
        public final static String Cancel = localizations.getCommon().getCancel();
        public final static String Retry  = localizations.getCommon().getRetry();
        public final static String Close  = localizations.getCommon().getClose();

        private Common() {}

        private static Matcher authorMatcher;

        public static String getAuthor(String author) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (authorMatcher == null){
                authorMatcher = regexSinglePattern.matcher(localizations.getCommon().getAuthor());
            }
            return authorMatcher.replaceAll(author);
        }
    }

    public static class DataBar {

        public final static String Today      = localizations.getDataBar().getToday();
        public final static String LiveInApp  = localizations.getDataBar().getLiveInApp();
        public final static String Postponed  = localizations.getDataBar().getPostponed();
        public final static String Cancelled  = localizations.getDataBar().getCancelled();
        public final static String Delayed    = localizations.getDataBar().getDelayed();
        public final static String Suspended  = localizations.getDataBar().getSuspended();
        public final static String FinalScore = localizations.getDataBar().getFinalScore();
        public final static String Out        = localizations.getDataBar().getOut();
        public final static String Outs       = localizations.getDataBar().getOuts();
        public final static String Quarter    = localizations.getDataBar().getQuarter();
        public final static String PowerPlay  = localizations.getDataBar().getPowerPlay();
        public final static String Overtime   = localizations.getDataBar().getOvertime();
        public final static String Shootout   = localizations.getDataBar().getShootout();
        public final static String Record     = localizations.getDataBar().getRecord();

        private DataBar() {}
    }

    public static class DataMenu {

        public final static String Roster				                       = localizations.getDataMenu().getRoster();
        public final static String Schedule				                       = localizations.getDataMenu().getSchedule();
        public final static String Standings				                   = localizations.getDataMenu().getStandings();
        public final static String Score                                       = localizations.getDataMenu().getScore();
        public final static String League				                       = localizations.getDataMenu().getLeague();
        public final static String Conference				                   = localizations.getDataMenu().getConference();
        public final static String Division				                       = localizations.getDataMenu().getDivision();
        public final static String Record				                       = localizations.getDataMenu().getRecord();
        public final static String GamesBack				                   = localizations.getDataMenu().getGamesBack();
        public final static String LastTen	                                   = localizations.getDataMenu().getLastTen();
        public final static String FieldGoalPercent	                           = localizations.getDataMenu().getFieldGoalPercent();
        public final static String ThreePointPercent                           = localizations.getDataMenu().getThreePointPercent();
        public final static String PowerPlayPercent	                           = localizations.getDataMenu().getPowerPlayPercent();
        public final static String SavePercent	                               = localizations.getDataMenu().getSavePercent();
        public final static String RedZonePercent	                           = localizations.getDataMenu().getRedZonePercent();
        public final static String ThirdDownPercent	                           = localizations.getDataMenu().getThirdDownPercent();
        public final static String DivisionLeaders	                           = localizations.getDataMenu().getDivisionLeaders();
        public final static String WildCard	                                   = localizations.getDataMenu().getWildCard();
        public final static String RecordAbbreviation	                       = localizations.getDataMenu().getRecordAbbreviation();
        public final static String GamesBackAbbreviation                       = localizations.getDataMenu().getGamesBackAbbreviation();
        public final static String PointsAbbreviation	                       = localizations.getDataMenu().getPointsAbbreviation();
        public final static String PercentAbbreviation                         = localizations.getDataMenu().getPercentAbbreviation();
        public final static String RosterSortSelectionTitle	                   = localizations.getDataMenu().getRosterSortSelectionTitle();
        public final static String ScheduleSeasonSelectionTitle	               = localizations.getDataMenu().getScheduleSeasonSelectionTitle();
        public final static String DataDetailUpdated                           = localizations.getDataMenu().getDataDetailUpdated();
        public final static String ScheduleTBD                                 = localizations.getDataMenu().getScheduleTBD();
        public final static String ScheduleFinal                               = localizations.getDataMenu().getScheduleFinal();
        public final static String RosterSortName                              = localizations.getDataMenu().getRosterSortName();
        public final static String RosterSortPosition                          = localizations.getDataMenu().getRosterSortPosition();
        public final static String RosterSortAge                               = localizations.getDataMenu().getRosterSortAge();
        public final static String RosterSortHeight	                           = localizations.getDataMenu().getRosterSortHeight();
        public final static String RosterSortWeight	                           = localizations.getDataMenu().getRosterSortWeight();
        public final static String RosterSortJersey                            = localizations.getDataMenu().getRosterSortJersey();
        public final static String RosterPoundsUnit                            = localizations.getDataMenu().getRosterPoundsUnit();
        public final static String RosterYearOld                               = localizations.getDataMenu().getRosterYearOld();
        public final static String ClinchedPlayoffs                            = localizations.getDataMenu().getClinchedPlayoffs();
        public final static String ClinchedDivision                            = localizations.getDataMenu().getClinchedDivision();
        public final static String ClinchedConference                          = localizations.getDataMenu().getClinchedConference();
        public final static String ClinchedHomeField                           = localizations.getDataMenu().getClinchedHomeField();
        public final static String ClinchedBye				                   = localizations.getDataMenu().getClinchedBye();
        public final static String ClinchedTrophy				               = localizations.getDataMenu().getClinchedTrophy();
        public final static String BoxscoreFinal                               = localizations.getDataMenu().getBoxscoreFinal();
        public final static String BoxscoreInProgress                          = localizations.getDataMenu().getBoxscoreInProgress();
        public final static String BoxscoreRecentGame                          = localizations.getDataMenu().getBoxscoreRecentGame();
        public final static String BoxscoreGameInProgress                      = localizations.getDataMenu().getBoxscoreGameInProgress();
        public final static String BoxscoreLinescoreTotalAbbreviation          = localizations.getDataMenu().getBoxscoreLinescoreTotalAbbreviation();
        public final static String BoxscoreLinescoreRunsAbbreviation           = localizations.getDataMenu().getBoxscoreLinescoreRunsAbbreviation();
        public final static String BoxscoreLinescoreShootOut                   = localizations.getDataMenu().getBoxscoreLinescoreShootOut();
        public final static String BoxscoreLinescoreOverTime                   = localizations.getDataMenu().getBoxscoreLinescoreOverTime();
        public final static String BoxscorePointsAbbreviation                  = localizations.getDataMenu().getBoxscorePointsAbbreviation();
        public final static String BoxscoreMLBBatting                          = localizations.getDataMenu().getBoxscoreMLBBatting();
        public final static String BoxscoreMLBPitching                         = localizations.getDataMenu().getBoxscoreMLBPitching();
        public final static String BoxscoreMLBAtBatsAbbreviation               = localizations.getDataMenu().getBoxscoreMLBAtBatsAbbreviation();
        public final static String BoxscoreMLBARunsAbbreviation                = localizations.getDataMenu().getBoxscoreMLBARunsAbbreviation();
        public final static String BoxscoreMLBAHitsAbbreviation                = localizations.getDataMenu().getBoxscoreMLBAHitsAbbreviation();
        public final static String BoxscoreMLBABasesOnBallsAbbreviation        = localizations.getDataMenu().getBoxscoreMLBABasesOnBallsAbbreviation();
        public final static String BoxscoreMLBARunsBattedInAbbreviation        = localizations.getDataMenu().getBoxscoreMLBARunsBattedInAbbreviation();
        public final static String BoxscoreMLBAInningsPitchedAbbreviation      = localizations.getDataMenu().getBoxscoreMLBAInningsPitchedAbbreviation();
        public final static String BoxscoreMLBAEarnedRunsPitchedAbbreviation   = localizations.getDataMenu().getBoxscoreMLBAEarnedRunsPitchedAbbreviation();
        public final static String BoxscoreMLBAWalksAllowedPitchedAbbreviation = localizations.getDataMenu().getBoxscoreMLBAWalksAllowedPitchedAbbreviation();
        public final static String BoxscoreMLBAStrikeOutsPitchedAbbreviation   = localizations.getDataMenu().getBoxscoreMLBAStrikeOutsPitchedAbbreviation();
        public final static String BoxscoreNBAPlayers                          = localizations.getDataMenu().getBoxscoreNBAPlayers();
        public final static String BoxscoreNBAMinutesAbbreviation              = localizations.getDataMenu().getBoxscoreNBAMinutesAbbreviation();
        public final static String BoxscoreNBAReboundsAbbreviation             = localizations.getDataMenu().getBoxscoreNBAReboundsAbbreviation();
        public final static String BoxscoreNBAAssistsAbbreviation              = localizations.getDataMenu().getBoxscoreNBAAssistsAbbreviation();
        public final static String BoxscoreNFLPassing                          = localizations.getDataMenu().getBoxscoreNFLPassing();
        public final static String BoxscoreNFLRushing                          = localizations.getDataMenu().getBoxscoreNFLRushing();
        public final static String BoxscoreNFLReceiving                        = localizations.getDataMenu().getBoxscoreNFLReceiving();
        public final static String BoxscoreNFLDefense                          = localizations.getDataMenu().getBoxscoreNFLDefense();
        public final static String BoxscoreNFLKickReturns                      = localizations.getDataMenu().getBoxscoreNFLKickReturns();
        public final static String BoxscoreNFLPuntReturns                      = localizations.getDataMenu().getBoxscoreNFLPuntReturns();
        public final static String BoxscoreNFLKicking                          = localizations.getDataMenu().getBoxscoreNFLKicking();
        public final static String BoxscoreNFLPunting                          = localizations.getDataMenu().getBoxscoreNFLPunting();
        public final static String BoxscoreNFLAttemptsAbbreviation             = localizations.getDataMenu().getBoxscoreNFLAttemptsAbbreviation();
        public final static String BoxscoreNFLCarriesAbbreviation              = localizations.getDataMenu().getBoxscoreNFLCarriesAbbreviation();
        public final static String BoxscoreNFLReceptionsAbbreviation           = localizations.getDataMenu().getBoxscoreNFLReceptionsAbbreviation();
        public final static String BoxscoreNFLTacklesAbbreviation              = localizations.getDataMenu().getBoxscoreNFLTacklesAbbreviation();
        public final static String BoxscoreNFLSacksAbbreviation                = localizations.getDataMenu().getBoxscoreNFLSacksAbbreviation();
        public final static String BoxscoreNFLReturnsAbbreviation              = localizations.getDataMenu().getBoxscoreNFLReturnsAbbreviation();
        public final static String BoxscoreNFLFieldGoalsAbbreviation           = localizations.getDataMenu().getBoxscoreNFLFieldGoalsAbbreviation();
        public final static String BoxscoreNFLExtraPointAbbreviation           = localizations.getDataMenu().getBoxscoreNFLExtraPointAbbreviation();
        public final static String BoxscoreNFLIn20Abbreviation                 = localizations.getDataMenu().getBoxscoreNFLIn20Abbreviation();
        public final static String BoxscoreNFLPercentAbbreviation              = localizations.getDataMenu().getBoxscoreNFLPercentAbbreviation();
        public final static String BoxscoreNFLLongestAbbreviation              = localizations.getDataMenu().getBoxscoreNFLLongestAbbreviation();
        public final static String BoxscoreNFLYardsAbbreviation                = localizations.getDataMenu().getBoxscoreNFLYardsAbbreviation();
        public final static String BoxscoreNFLAverageAbbreviation              = localizations.getDataMenu().getBoxscoreNFLAverageAbbreviation();
        public final static String BoxscoreNFLTouchdownsAbbreviation           = localizations.getDataMenu().getBoxscoreNFLTouchdownsAbbreviation();
        public final static String BoxscoreNFLInterceptionsAbbreviation        = localizations.getDataMenu().getBoxscoreNFLInterceptionsAbbreviation();
        public final static String BoxscoreNFLNumberAbbreviation               = localizations.getDataMenu().getBoxscoreNFLNumberAbbreviation();
        public final static String BoxscoreNHLGoalsAbbreviation                = localizations.getDataMenu().getBoxscoreNHLGoalsAbbreviation();
        public final static String BoxscoreNHLSkaters                          = localizations.getDataMenu().getBoxscoreNHLSkaters();
        public final static String BoxscoreNHLGoalie                           = localizations.getDataMenu().getBoxscoreNHLGoalie();
        public final static String BoxscoreNHLAssistsAbbreviation              = localizations.getDataMenu().getBoxscoreNHLAssistsAbbreviation();
        public final static String BoxscoreNHLShotsOnGoalAbbreviation          = localizations.getDataMenu().getBoxscoreNHLShotsOnGoalAbbreviation();
        public final static String BoxscoreNHLGoalsAgainstAbbreviation         = localizations.getDataMenu().getBoxscoreNHLGoalsAgainstAbbreviation();
        public final static String BoxscoreNHLShotsAgainstAbbreviation         = localizations.getDataMenu().getBoxscoreNHLShotsAgainstAbbreviation();
        public final static String BoxscoreNHLSavesAbbreviation                = localizations.getDataMenu().getBoxscoreNHLSavesAbbreviation();
        public final static String BoxscoreNHLSavePercentAbbreviation          = localizations.getDataMenu().getBoxscoreNHLSavePercentAbbreviation();
        public final static String BoxscoreNHLScoringSummary                   = localizations.getDataMenu().getBoxscoreNHLScoringSummary();
        public final static String BoxscoreNHLNoGoal                           = localizations.getDataMenu().getBoxscoreNHLNoGoal();
        public final static String BoxscoreNHLOvertime                         = localizations.getDataMenu().getBoxscoreNHLOvertime();

        private DataMenu() {}

        private static Matcher playersMatcher;
        private static Matcher nextGameMatcher;
        private static Matcher awayVsHomeMatcher;
        private static Matcher divisionRankMatcher;
        private static Matcher conferenceRankMatcher;
        private static Matcher streakMatcher;
        private static Matcher effectiveFieldGoalPercentMatcher;
        private static Matcher pointsPerGameMatcher;
        private static Matcher pointsPercentMatcher;
        private static Matcher powerPlayGoalsPerGameMatcher;
        private static Matcher shotsAgainstPerGameMatcher;
        private static Matcher redZoneScoresMatcher;
        private static Matcher thirdDownEfficiencyMatcher;
        private static Matcher statusMatcher;
        private static Matcher dataDetailTitleFormatMatcher;
        private static Matcher dataDetailUpdatedFormatMatcher;
        private static Matcher boxscoreNHLPeriodMatcher;

        public static String getPlayers(String playersCount) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (playersMatcher == null){
                playersMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getPlayers());
            }
            return playersMatcher.replaceAll(playersCount);
        }

        public static String getNextGame(String date) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (nextGameMatcher == null){
                nextGameMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getNextGame());
            }
            return nextGameMatcher.replaceAll(date);
        }

        public static String getAwayVsHome(String away, String home) {
            if (regexMultiplePattern == null){
                initMultiplePattern();
            }
            if (awayVsHomeMatcher == null){
                awayVsHomeMatcher = regexMultiplePattern.matcher(localizations.getDataMenu().getAwayVsHome());
            }
            return regexMultiplePattern.matcher(awayVsHomeMatcher.replaceFirst(away)).replaceAll(home);
        }

        public static String getDivisionRank(String ordinal) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (divisionRankMatcher == null){
                divisionRankMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getDivisionRank());
            }
            return divisionRankMatcher.replaceAll(ordinal);
        }

        public static String getConferenceRank(String ordinal) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (conferenceRankMatcher == null){
                conferenceRankMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getConferenceRank());
            }
            return conferenceRankMatcher.replaceAll(ordinal);
        }

        public static String getStreak(String streak) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (streakMatcher == null){
                streakMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getStreak());
            }
            return streakMatcher.replaceAll(streak);
        }

        public static String getEffectiveFieldGoalPercent(String value) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (effectiveFieldGoalPercentMatcher == null){
                effectiveFieldGoalPercentMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getEffectiveFieldGoalPercent());
            }
            return effectiveFieldGoalPercentMatcher.replaceAll(value);
        }

        public static String getPointsPerGame(String points) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (pointsPerGameMatcher == null){
                pointsPerGameMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getPointsPerGame());
            }
            return pointsPerGameMatcher.replaceAll(points);
        }

        public static String getPointsPercent(String value) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (pointsPercentMatcher == null){
                pointsPercentMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getPointsPercent());
            }
            return pointsPercentMatcher.replaceAll(value);
        }

        public static String getPowerPlayGoalsPerGame(String value) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (powerPlayGoalsPerGameMatcher == null){
                powerPlayGoalsPerGameMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getPowerPlayGoalsPerGame());
            }
            return powerPlayGoalsPerGameMatcher.replaceAll(value);
        }

        public static String getShotsAgainstPerGame(String value) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (shotsAgainstPerGameMatcher == null){
                shotsAgainstPerGameMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getShotsAgainstPerGame());
            }
            return shotsAgainstPerGameMatcher.replaceAll(value);
        }

        public static String getRedZoneScores(String value) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (redZoneScoresMatcher == null){
                redZoneScoresMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getRedZoneScores());
            }
            return redZoneScoresMatcher.replaceAll(value);
        }

        public static String getThirdDownEfficiency(String value) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (thirdDownEfficiencyMatcher == null){
                thirdDownEfficiencyMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getThirdDownEfficiency());
            }
            return thirdDownEfficiencyMatcher.replaceAll(value);
        }

        public static String getStatus(String status) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (statusMatcher == null){
                statusMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getStatus());
            }
            return statusMatcher.replaceAll(status);
        }

        public static String getDataDetailTitleFormat(String team, String title) {
            if (regexMultiplePattern == null){
                initMultiplePattern();
            }
            if (dataDetailTitleFormatMatcher == null){
                dataDetailTitleFormatMatcher = regexMultiplePattern.matcher(localizations.getDataMenu().getDataDetailTitleFormat());
            }
            return regexMultiplePattern.matcher(dataDetailTitleFormatMatcher.replaceFirst(team)).replaceAll(title);
        }

        public static String getDataDetailUpdatedFormat(String updated, String time) {
            if (regexMultiplePattern == null){
                initMultiplePattern();
            }
            if (dataDetailUpdatedFormatMatcher == null){
                dataDetailUpdatedFormatMatcher = regexMultiplePattern.matcher(localizations.getDataMenu().getDataDetailUpdatedFormat());
            }
            return regexMultiplePattern.matcher(dataDetailUpdatedFormatMatcher.replaceFirst(updated)).replaceAll(time);
        }

        public static String getBoxscoreNHLPeriod(String ordinal) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (boxscoreNHLPeriodMatcher == null){
                boxscoreNHLPeriodMatcher = regexSinglePattern.matcher(localizations.getDataMenu().getBoxscoreNHLPeriod());
            }
            return boxscoreNHLPeriodMatcher.replaceAll(ordinal);
        }
    }

    public static class Settings {

        public final static String Settings             = localizations.getSettings().getSettings();
        public final static String GuestUser            = localizations.getSettings().getGuestUser();
        public final static String MyTeams              = localizations.getSettings().getMyTeams();
        public final static String EditReorder          = localizations.getSettings().getEditReorder();
        public final static String Notifications        = localizations.getSettings().getNotifications();
        public final static String AllowNotifications   = localizations.getSettings().getAllowNotifications();
        public final static String BreakingNews         = localizations.getSettings().getBreakingNews();
        public final static String TeamNews             = localizations.getSettings().getTeamNews();
        public final static String Data                 = localizations.getSettings().getData();
        public final static String MediaSettings        = localizations.getSettings().getMediaSettings();
        public final static String Support              = localizations.getSettings().getSupport();
        public final static String FAQ                  = localizations.getSettings().getFAQ();
        public final static String UpdateApp            = localizations.getSettings().getUpdateApp();
        public final static String About                = localizations.getSettings().getAbout();
        public final static String Feedback             = localizations.getSettings().getFeedback();
        public final static String Privacy              = localizations.getSettings().getPrivacy();
        public final static String TermsUse             = localizations.getSettings().getTermsUse();
        public final static String Share                = localizations.getSettings().getShare();
        public final static String ViewLess             = localizations.getSettings().getViewLess();
        public final static String ViewMore             = localizations.getSettings().getViewMore();
        public final static String FabigationHandedness = localizations.getSettings().getFabigationHandedness();
        public final static String LogOut               = localizations.getSettings().getLogOut();
        public final static String LogOutCTA            = localizations.getSettings().getLogOutCTA();
        public final static String LogOutConfirmTitle   = localizations.getSettings().getLogOutConfirmTitle();
        public final static String LogOutConfirmText    = localizations.getSettings().getLogOutConfirmText();

        private Settings() {}

        private static Matcher installSinceMatcher;

        public static String getInstallSince(String date) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (installSinceMatcher == null){
                installSinceMatcher = regexSinglePattern.matcher(localizations.getSettings().getInstallSince());
            }
            return installSinceMatcher.replaceAll(date);
        }

        public static String getSettingsAutoDetect(String settingKey) {
            if (!settingKey.contains("Settings.")){
                return RsnApplication.getInstance().getString(R.string.settings_support_not_matched);
            }
            String resultString = settingKey;
            switch (settingKey){
                case "Settings.FAQ":
                    resultString = FAQ;
                    break;
                case "Settings.UpdateApp":
                    resultString = UpdateApp;
                    break;
                case "Settings.About":
                    resultString = About;
                    break;
                case "Settings.Feedback":
                    resultString = Feedback;
                    break;
                case "Settings.Privacy":
                    resultString = Privacy;
                    break;
                case "Settings.TermsUse":
                    resultString = TermsUse;
                    break;
                case "Settings.Share":
                    resultString = Share;
                    break;
                default:
                    resultString = RsnApplication.getInstance().getString(R.string.settings_support_not_matched);
                    break;
            }
            return resultString;
        }
    }

    public static class TeamSelector {

        public final static String MyTeams     = localizations.getTeamSelector().getMyTeams();
        public final static String RemoveAll   = localizations.getTeamSelector().getRemoveAll();
        public final static String SelectAll   = localizations.getTeamSelector().getSelectAll();
        public final static String SaveTeams   = localizations.getTeamSelector().getSaveTeams();
        public final static String HoldAndDrag = localizations.getTeamSelector().getHoldAndDrag();

        private TeamSelector() {}
    }

    public static class TeamSelectorSuccess {

        public final static String RotatedText  = localizations.getTeamSelectorSuccess().getRotatedText();
        public final static String TitleText    = localizations.getTeamSelectorSuccess().getTitleText();
        public final static String SubtitleText = localizations.getTeamSelectorSuccess().getSubtitleText();

        private TeamSelectorSuccess() {}
    }

    public static class MoreTeamsSelector {

        public final static String MoreTeams     = localizations.getMoreTeamsSelector().getMoreTeams();
        public final static String MoreTeamsCard = localizations.getMoreTeamsSelector().getMoreTeamsCard();

        private MoreTeamsSelector() {}

        private static Matcher numberOfTeamsSingularMatcher;
        private static Matcher numberOfTeamsPluralMatcher;

        public static String getNumberOfTeamsSingular(String number) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (numberOfTeamsSingularMatcher == null){
                numberOfTeamsSingularMatcher = regexSinglePattern.matcher(localizations.getMoreTeamsSelector().getNumberOfTeamsSingular());
            }
            return numberOfTeamsSingularMatcher.replaceAll(number);
        }

        public static String getNumberOfTeamsPlural(String number) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (numberOfTeamsPluralMatcher == null){
                numberOfTeamsPluralMatcher = regexSinglePattern.matcher(localizations.getMoreTeamsSelector().getNumberOfTeamsPlural());
            }
            return numberOfTeamsPluralMatcher.replaceAll(number);
        }
    }

    public static class TeamView {

        public final static String TheFeed       = localizations.getTeamView().getTheFeed();
        public final static String LiveFeedPromo = localizations.getTeamView().getLiveFeedPromo();
        public final static String UpNext        = localizations.getTeamView().getUpNext();

        private TeamView() {}
    }

    public static class Authentication {

        public final static String TimeOut    = localizations.getAuthentication().getTimeOut();
        public final static String Incomplete = localizations.getAuthentication().getIncomplete();

        private Authentication() {}
    }

    public static class VideoPlayer {

        public final static String Live                          = localizations.getVideoPlayer().getLive();
        public final static String Close                         = localizations.getVideoPlayer().getClose();
        public final static String GoLive                        = localizations.getVideoPlayer().getGoLive();
        public final static String LastViewed                    = localizations.getVideoPlayer().getLastViewed();
        public final static String SignIn                        = localizations.getVideoPlayer().getSignIn();
        public final static String PreviewRemaining              = localizations.getVideoPlayer().getPreviewRemaining();
        public final static String PreviewEndedTitle             = localizations.getVideoPlayer().getPreviewEndedTitle();
        public final static String PreviewEndedDescription       = localizations.getVideoPlayer().getPreviewEndedDescription();
        public final static String PreviewUnavailableTitle       = localizations.getVideoPlayer().getPreviewUnavailableTitle();
        public final static String PreviewUnavailableDescription = localizations.getVideoPlayer().getPreviewUnavailableDescription();

        private VideoPlayer() {}
    }

    public static class Date {

        private Date() {}

        private static Matcher minutesAgoSingularMatcher;
        private static Matcher minutesAgoPluralMatcher;
        private static Matcher hoursAgoSingularMatcher;
        private static Matcher hoursAgoPluralMatcher;
        private static Matcher daysAgoSingularMatcher;
        private static Matcher daysAgoPluralMatcher;

        public static String getMinutesAgoSingular(String time) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (minutesAgoSingularMatcher == null){
                minutesAgoSingularMatcher = regexSinglePattern.matcher(localizations.getDate().getMinutesAgoSingular());
            }
            return minutesAgoSingularMatcher.replaceAll(time);
        }

        public static String getMinutesAgoPlural(String time) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (minutesAgoPluralMatcher == null){
                minutesAgoPluralMatcher = regexSinglePattern.matcher(localizations.getDate().getMinutesAgoPlural());
            }
            return minutesAgoPluralMatcher.replaceAll(time);
        }

        public static String getMinutesAgoAutoDetect(String time) {
            try {
                int timeInt = Integer.parseInt(time);
                if (timeInt == 1){
                    return getMinutesAgoSingular(time);
                } else {
                    return getMinutesAgoPlural(time);
                }
            } catch (Exception e){
                return getMinutesAgoPlural(time);
            }
        }

        public static String getHoursAgoSingular(String time) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (hoursAgoSingularMatcher == null){
                hoursAgoSingularMatcher = regexSinglePattern.matcher(localizations.getDate().getHoursAgoSingular());
            }
            return hoursAgoSingularMatcher.replaceAll(time);
        }

        public static String getHoursAgoPlural(String time) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (hoursAgoPluralMatcher == null){
                hoursAgoPluralMatcher = regexSinglePattern.matcher(localizations.getDate().getHoursAgoPlural());
            }
            return hoursAgoPluralMatcher.replaceAll(time);
        }

        public static String getHoursAgoAutoDetect(String time) {
            try {
                int timeInt = Integer.parseInt(time);
                if (timeInt == 1){
                    return getHoursAgoSingular(time);
                } else {
                    return getHoursAgoPlural(time);
                }
            } catch (Exception e){
                return getHoursAgoPlural(time);
            }
        }

        public static String getDaysAgoSingular(String time) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (daysAgoSingularMatcher == null){
                daysAgoSingularMatcher = regexSinglePattern.matcher(localizations.getDate().getDaysAgoSingular());
            }
            return daysAgoSingularMatcher.replaceAll(time);
        }

        public static String getDaysAgoPlural(String time) {
            if (regexSinglePattern == null){
                initSinglePattern();
            }
            if (daysAgoPluralMatcher == null){
                daysAgoPluralMatcher = regexSinglePattern.matcher(localizations.getDate().getDaysAgoPlural());
            }
            return daysAgoPluralMatcher.replaceAll(time);
        }

        public static String getDaysAgoAutoDetect(String time) {
            try {
                int timeInt = Integer.parseInt(time);
                if (timeInt == 1){
                    return getDaysAgoSingular(time);
                } else {
                    return getDaysAgoPlural(time);
                }
            } catch (Exception e){
                return getDaysAgoPlural(time);
            }
        }
    }

    public static class Signup {

        public final static String SignUpMessage         = localizations.getSignup().getSignUpMessage();
        public final static String Facebook              = localizations.getSignup().getFacebook();
        public final static String Twitter               = localizations.getSignup().getTwitter();
        public final static String Email                 = localizations.getSignup().getEmail();
        public final static String Connect               = localizations.getSignup().getConnect();
        public final static String Signup                = localizations.getSignup().getSignup();
        public final static String Or                    = localizations.getSignup().getOr();
        public final static String AlreadyHaveAccount    = localizations.getSignup().getAlreadyHaveAccount();
        public final static String TermsConditionPrivacy = localizations.getSignup().getTermsConditionPrivacy();
        public final static String FirstName             = localizations.getSignup().getFirstName();
        public final static String LastName              = localizations.getSignup().getLastName();
        public final static String ZipLocation           = localizations.getSignup().getZipLocation();
        public final static String Password              = localizations.getSignup().getPassword();
        public final static String Subscribe             = localizations.getSignup().getSubscribe();
        public final static String LiveStreaming         = localizations.getSignup().getLiveStreaming();
        public final static String Optional              = localizations.getSignup().getOptional();
        public final static String NBCNewsletter         = localizations.getSignup().getNBCNewsletter();
        public final static String CableUseQuestion      = localizations.getSignup().getCableUseQuestion();
        public final static String CreateAccount         = localizations.getSignup().getCreateAccount();
        public final static String CreateAccountTerms    = localizations.getSignup().getCreateAccountTerms();
        public final static String InvalidName           = localizations.getSignup().getInvalidName();
        public final static String InvalidEmail          = localizations.getSignup().getInvalidEmail();
        public final static String AlreadyUsedEmail      = localizations.getSignup().getAlreadyUsedEmail();
        public final static String InvalidPassword       = localizations.getSignup().getInvalidPassword();
        public final static String InvalidPhone          = localizations.getSignup().getInvalidPhone();
        public final static String InvalidZip            = localizations.getSignup().getInvalidZip();
        public final static String InvalidSocial         = localizations.getSignup().getInvalidSocial();

        private Signup() {}
    }

    public static class Alerts {

        private Alerts() {}

        public static class NetworkError {

            public final static String Title   = localizations.getAlerts().getNetworkError().getTitle();
            public final static String Message = localizations.getAlerts().getNetworkError().getMessage();

            private NetworkError() {}
        }

        public static class PlaybackError {

            public final static String Title   = localizations.getAlerts().getPlaybackError().getTitle();
            public final static String Message = localizations.getAlerts().getPlaybackError().getMessage();

            private PlaybackError() {}
        }

        public static class AuthorizeError {

            public final static String Title   = localizations.getAlerts().getAuthorizeError().getTitle();
            public final static String Message = localizations.getAlerts().getAuthorizeError().getMessage();

            private AuthorizeError() {}
        }

        public static class ServerError {

            public final static String Title   = localizations.getAlerts().getServerError().getTitle();
            public final static String Message = localizations.getAlerts().getServerError().getMessage();

            private ServerError() {}
        }

        public static class WifiError {

            public final static String Title   = localizations.getAlerts().getWifiError().getTitle();
            public final static String Message = localizations.getAlerts().getWifiError().getMessage();

            private WifiError() {}
        }

        public static class EmailError {

            public final static String Title   = localizations.getAlerts().getEmailError().getTitle();
            public final static String Message = localizations.getAlerts().getEmailError().getMessage();

            private EmailError() {}
        }

        public static class AppUpToDate {

            public final static String Title   = localizations.getAlerts().getAppUpToDate().getTitle();
            public final static String Message = localizations.getAlerts().getAppUpToDate().getMessage();

            private AppUpToDate() {}
        }

        public static class LogOut {

            public final static String Title   = localizations.getAlerts().getLogOut().getTitle();
            public final static String Message = localizations.getAlerts().getLogOut().getMessage();

            private LogOut() {}
        }

        public static class CellularStreamError {

            public final static String Title   = localizations.getAlerts().getCellularStreamError().getTitle();
            public final static String Message = localizations.getAlerts().getCellularStreamError().getMessage();

            private CellularStreamError() {}
        }

        public static class FTUEFlick {

            public final static String Title   = localizations.getAlerts().getFTUEFlick().getTitle();
            public final static String Message = localizations.getAlerts().getFTUEFlick().getMessage();

            private FTUEFlick() {}
        }

        public static class FTUETap {

            public final static String Title   = localizations.getAlerts().getFTUETap().getTitle();
            public final static String Message = localizations.getAlerts().getFTUETap().getMessage();

            private FTUETap() {}
        }

        public static class FTUEDataMenu {

            public final static String Title   = localizations.getAlerts().getFTUEDataMenu().getTitle();
            public final static String Message = localizations.getAlerts().getFTUEDataMenu().getMessage();

            private FTUEDataMenu() {}
        }

        public static class GMOEntitlementError {

            public final static String Title   = localizations.getAlerts().getGMOEntitlementError().getTitle();
            public final static String Message = localizations.getAlerts().getGMOEntitlementError().getMessage();

            private GMOEntitlementError() {}
        }

        public static class EnableLocationServices {

            public final static String Title   = localizations.getAlerts().getEnableLocationServices().getTitle();
            public final static String Message = localizations.getAlerts().getEnableLocationServices().getMessage();

            private EnableLocationServices() {}
        }

        public static class RSNEntitlementError {

            public final static String Title   = localizations.getAlerts().getRSNEntitlementError().getTitle();
            public final static String Message = localizations.getAlerts().getRSNEntitlementError().getMessage();

            private RSNEntitlementError() {}
        }

        public static class ChromecastFailed {

            public final static String Title   = localizations.getAlerts().getChromecastFailed().getTitle();

            private static Matcher messageMatcher;

            private ChromecastFailed() {}

            public static String getMessage(String error) {
                if (regexSinglePattern == null){
                    initSinglePattern();
                }
                if (messageMatcher == null){
                    messageMatcher = regexSinglePattern.matcher(localizations.getAlerts().getChromecastFailed().getMessage());
                }
                return messageMatcher.replaceAll(error);
            }
        }

        public static class ChromecastError {

            public final static String Title = localizations.getAlerts().getChromecastError().getTitle();

            private static Matcher messageMatcher;

            private ChromecastError() {}

            public static String getMessage(String error) {
                if (regexSinglePattern == null){
                    initSinglePattern();
                }
                if (messageMatcher == null){
                    messageMatcher = regexSinglePattern.matcher(localizations.getAlerts().getChromecastError().getMessage());
                }
                return messageMatcher.replaceAll(error);
            }
        }

        public static class ChromecastEnded {

            public final static String Title   = localizations.getAlerts().getChromecastEnded().getTitle();
            public final static String Message = localizations.getAlerts().getChromecastEnded().getMessage();

            private ChromecastEnded() {}
        }

        public static class ChromecastAssetError {

            public final static String Title   = localizations.getAlerts().getChromecastAssetError().getTitle();
            public final static String Message = localizations.getAlerts().getChromecastAssetError().getMessage();

            private ChromecastAssetError() {}
        }
    }

    public static class TeamNews {

        public final static String TeamNews                 = localizations.getTeamNews().getTeamNews();
        public final static String EditNotificationSettings = localizations.getTeamNews().getEditNotificationSettings();
        public final static String AllChangedSaved          = localizations.getTeamNews().getAllChangedSaved();
        public final static String All                      = localizations.getTeamNews().getAll();
        public final static String GameStart                = localizations.getTeamNews().getGameStart();
        public final static String FinalScore               = localizations.getTeamNews().getFinalScore();

        private TeamNews() {}
    }

    public static class MediaSettings {

        public final static String MediaSettings                                             = localizations.getMediaSettings().getMediaSettings();
        public final static String LiveVideoPreferences                                      = localizations.getMediaSettings().getLiveVideoPreferences();
        public final static String CellularDataForLiveStream                                 = localizations.getMediaSettings().getCellularDataForLiveStream();
        public final static String AllowTheUseOfCellularDataForLiveStreaming                 = localizations.getMediaSettings().getAllowTheUseOfCellularDataForLiveStreaming();
        public final static String AutoPlayLiveGames                                         = localizations.getMediaSettings().getAutoPlayLiveGames();
        public final static String MediaStreamPreferences                                    = localizations.getMediaSettings().getMediaStreamPreferences();
        public final static String CellularDataForMediaStream                                = localizations.getMediaSettings().getCellularDataForMediaStream();
        public final static String AllowTheUseOfCellularDataForNonLiveVideoAndAudioStreaming = localizations.getMediaSettings().getAllowTheUseOfCellularDataForNonLiveVideoAndAudioStreaming();
        public final static String AutoPlayVideos                                            = localizations.getMediaSettings().getAutoPlayVideos();

        private MediaSettings() {}
    }

    public static class KebabMenu {

        public final static String Chromecast           = localizations.getKebabMenu().getChromecast();
        public final static String ChromecastConnecting = localizations.getKebabMenu().getChromecastConnecting();
        public final static String Share                = localizations.getKebabMenu().getShare();
        public final static String Mute                 = localizations.getKebabMenu().getMute();
        public final static String Unmute               = localizations.getKebabMenu().getUnmute();
        public final static String Airplay              = localizations.getKebabMenu().getAirplay();
        public final static String CCOn                 = localizations.getKebabMenu().getCCOn();
        public final static String CCOff                = localizations.getKebabMenu().getCCOff();

        private KebabMenu() {}
    }

    public static class NativeShareMessages {

        public final static String DefaultShareMessage = localizations.getNativeShareMessages().getDefaultShareMessage();

        private NativeShareMessages() {}
    }

    public static class AppReview {
        public final static String AreYouEnjoyingApp    = localizations.getAppReview().getAreYouEnjoyingApp();
        public final static String Yes                  = localizations.getAppReview().getYes();
        public final static String No                   = localizations.getAppReview().getNo();
        public final static String Postpone             = localizations.getAppReview().getPostpone();

        private AppReview() {}
    }

}