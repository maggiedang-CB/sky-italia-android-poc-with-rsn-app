package com.nbcsports.regional.nbc_rsn.common;

/**
 * Created by arkadykoplyarov on 2018-05-22.
 */

public class Constants {

    // Accordingly to the "[NBCRS]_Annotations_Sprint1" document:
    // Components:
    //      F1 Standard
    //          Variations
    //              F1 Standard Image
    //              F1 Standard Video
    //              F1 Standard Audio
    //      F1 Matchup
    //      F1 Cut Out
    //          Variations
    //              F1 Cut Out Image
    //              F1 Cut Out Video
    //              F1 Cut Out Audio
    //      F2 Standard
    //          Variations
    //              F2 Standard Image
    //              F2 Standard Video
    //              F2 Standard Audio
    //      F2 Static Caption
    //      F2 Dynamic Caption
    //      F2 Matchup
    //      F2 Icon
    //          Variations
    //              F2 Icon Show
    //              F2 Icon Injury
    //              F2 Icon Radio
    //              F2 Icon Podcast
    //      Feed Standard
    //          Variations
    //              Feed Image
    //              Feed Video
    //              Feed Show
    //              Feed Podcast
    //              Feed Radio
    //      Feed Matchup
    //      Feed Text Only
    //      Feed Icon
    //          Variations
    //              Ordered
    //              Person
    //      Feed Promo

    // Teamview card types
    static final public String CARD_TYPE_F1_Standard = "f1_standard";
    static final public String CARD_TYPE_F1_Matchup = "f1 matchup";
    static final public String CARD_TYPE_F1_Cut_Out = "f1_cut_out";
    static final public String CARD_TYPE_F2_Standard = "f2_standard";
    static final public String CARD_TYPE_F2_Static_Caption = "f2 static caption";
    static final public String CARD_TYPE_F2_Dynamic_Caption = "f2 dynamic caption";
    static final public String CARD_TYPE_F2_Matchup = "f2 matchup";
    static final public String CARD_TYPE_F2_Icon = "f2 icon";
    static final public String CARD_TYPE_Feed_Standard = "feed_standard";
    static final public String CARD_TYPE_Feed_Matchup = "feed matchup";
    static final public String CARD_TYPE_Feed_Text_Only = "feed_text_only";
    static final public String CARD_TYPE_Feed_Icon = "feed icon";
    static final public String CARD_TYPE_Feed_Promo = "feed_promo";

    // Content types
    static final public String CONTENT_TYPE_IMAGE = "image";
    static final public String CONTENT_TYPE_VIDEO = "video";
    static final public String CONTENT_TYPE_AUDIO = "audio";
    static final public String CONTENT_TYPE_Show = "show";
    static final public String CONTENT_TYPE_Injury = "injury";
    static final public String CONTENT_TYPE_Radio = "audio";
    static final public String CONTENT_TYPE_Podcast = "audio";
    static final public String CONTENT_TYPE_Ordered = "ordered";
    static final public String CONTENT_TYPE_Person = "person";
    static final public String CONTENT_TYPE_TEXT = "text";
    static final public String CONTENT_TYPE_STEPPED_STORY = "stepped_story";


    // Accordingly to the "[NBCRS]_Annotations_Sprint2" document:
    // Components:
    //      Hero
    //          Variations
    //              Image
    //              No Image
    //              Video
    //      Media Overlay
    //      Tables
    //          Variations
    //              Regular
    //              Tab
    //              Team
    //      Inline Image
    //      Inline Video
    //      Body Text
    //          Variations
    //              Regular
    //              Drop Cap
    //              Flagged
    //      Inline List
    //          Variations
    //              Numbered
    //              Bulleted
    //      Pull Quote
    //          Variations
    //              Stepped Interview
    //      Recirculation
    //      Embedded Tweet
    //          Variations
    //              Embedded Twitter Image
    //              Embedded Twitter Video
    //              Embedded Twitter Text Only

    // Editorial Details component types
    static final public String EDITORIAL_COMPONENT_TYPE_HERO = "hero";
    static final public String EDITORIAL_COMPONENT_TYPE_BODY_TEXT = "bodytext";
    static final public String EDITORIAL_COMPONENT_TYPE_EMBEDDED_TWEET = "embedded_tweet";
    static final public String EDITORIAL_COMPONENT_TYPE_IMAGE_INLINE = "inlineimage";


    // Editorial component variations
    // Hero
    static final public String EDITORIAL_COMPONENT_VARIATION_IMAGE = "image";
    public static final String EDITORIAL_COMPONENT_VARIATION_NOIMAGE = "noimage";
    public static final String EDITORIAL_COMPONENT_VARIATION_NOIMAGE_SPACED = "no image";
    static final public String EDITORIAL_COMPONENT_VARIATION_VIDEO = "video";
    // Body Text

    //public static final String CONTENT_TYPE_TEXT = "text";
    // currently, the field "contentType" may be missing for "cardType": "feed text only", in JSON from server

    public static final String EDITORIAL_COMPONENT_VARIATION_REGULAR = "regular";
    public static final String EDITORIAL_COMPONENT_VARIATION_DROP_CAP = "dropcap";
    public static final String EDITORIAL_COMPONENT_VARIATION_FLAGGED = "flagged";

    // Pre load constants
    public static final String PRE_LOAD_TEAM_CONTENT_SUFFIX    = "-pre-load";
    public static final String PRE_LOAD_TEAM_TIME_STAMP_SUFFIX = "-pre-load-time-stamp";
    public static final String PRE_LOAD_LIVE_ASSETS_KEY        = "live-assets-pre-load";
    public static final String PRE_LOAD_NTP_TIME_KAY           = "ntp-time-pre-load";

    // NTP header constant
    public static final String NTP_DATE_KEY = "Date";

    // Chromecast
    public static final String CC_IS_LIVE_KEY   = "isLive";
    public static final String CC_RESOURCE_KEY  = "resource";
    public static final String CC_PLATFORM_KEY  = "platform";
    public static final String CC_MVPD_ID_KEY   = "mvpdId";
    public static final String CC_MVPD_NAME_KEY = "mvpdName";
    public static final String CC_ID_KEY        = "id";
    public static final String CC_START_KEY     = "start";
    public static final String CC_SPORT_KEY     = "sport";
    public static final String CC_LEAGUE_KEY    = "league";
    public static final String CC_PID_KEY       = "pid";
    public static final String CC_LENGTH_KEY    = "length";
    public static final String CC_TOKEN_KEY     = "token";
    public static final String CC_ADOBE_MID_KEY = "adobeMid";
    public static final String CC_IS_FREE_KEY   = "isFree";

    // Colour string
    public static final String COLOUR_TRANSPARENT = "#00000000";

    // Team argument keys
    public static final String CONFIG_KEY = "config.id.x";
    public static final String TEAM_KEY   = "team.id.x";

    // Data bar
    public static final String PREF_KEY_DATABAR_ENABLED = "data_bar_enable";

    // Data menu carousel
    public static final String CAROUSEL_DATA_KEY = "carousel_data_key";
    public static final String DATA_MENU_IS_OFF_SEASON_KEY = "data_menu_is_off_season_key";
}
