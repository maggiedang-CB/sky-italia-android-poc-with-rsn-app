package com.nbcsports.regional.nbc_rsn.team_feed.components;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clearbridge.pull_to_refresh.PullLayout;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Constants;
import com.nbcsports.regional.nbc_rsn.common.FeedComponent;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.common.components.ViewHolderPersistentPlayerMedium;
import com.nbcsports.regional.nbc_rsn.common.components.ViewHolderTypeFeedTextOnly;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.team_feed.intent.TeamFeedItemClickListenerFactory;
import com.nbcsports.regional.nbc_rsn.team_feed.template.TeamFeedFragment;
import com.nbcsports.regional.nbc_rsn.team_view.TeamsPagerFragment;
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;


public class TeamViewComponentsAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements FragmentLifeCycleListener.Interface {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TODO: move these constants to a dedicated Constants.java file?
    // These RecyclerView type constants enumerate all cardTypes & contentType variations

    /*
    * TODO: How to improve this class?
    * This class just looks huge, but actually is not messy. However, there does exist some
    * duplicated codes related to VIEW_TYPE and ViewHolder creation. Things can be cleaned up a
    * little:
    *   1. ViewHolders can be reused better. Currently we have a lot of view types for different
    *      cards, while a lot of them actually share the same layout as well as binding logic.
    *      If we move some of the logic to a base view holder instead of creating so many different
    *      view types, can avoid some duplicated code (and remove some view types).
    *   2. Maybe use an enum class to represent view types? Like
    *      '''enum ViewType(FeedComponent.Type type, String cardType, String contentType)'''
    *      Then we can have a method that finds the ViewType based on feed types.
    *      This can save us some effort if we want to add a new card in the future.
    * */

    public static final int VIEW_TYPE_PERSISTENT_PLAYER_MEDIUM = 99;

    // The RecyclerView list-view-service labels
    public static final int VIEW_TYPE_UNKNOWN = 0;
    public static final int VIEW_TYPE_HEADER = 1;
    public static final int VIEW_TYPE_THEFEED_LABEL = 2;
    public static final int VIEW_TYPE_FOOTER = 3;

    // F1 card types start from this constant.
    public static final int VIEW_TYPE_F1_START = 4;

    // f1 standard
    public static final int VIEW_TYPE_F1_Standard_Image = VIEW_TYPE_F1_START;
    public static final int VIEW_TYPE_F1_Standard_Video = 5;
    public static final int VIEW_TYPE_F1_Standard_Audio = 6;

    // f1 matchup
    public static final int VIEW_TYPE_f1_matchup = 7;
    // f1 cut out
    public static final int VIEW_TYPE_f1_cut_out_Image = 8;
    public static final int VIEW_TYPE_f1_cut_out_Video = 9;
    public static final int VIEW_TYPE_f1_cut_out_Audio = 10;

    // F2 card types start from this constant.
    public static final int VIEW_TYPE_F2_START = 11;

    // f2 standard
    public static final int VIEW_TYPE_f2_standard_Image = VIEW_TYPE_F2_START;
    public static final int VIEW_TYPE_f2_standard_Video = 12;
    public static final int VIEW_TYPE_f2_standard_Audio = 13;
    // f2 static caption
    public static final int VIEW_TYPE_f2_static_caption = 14;
    // f2 dynamic caption
    public static final int VIEW_TYPE_f2_dynamic_caption = 15;
    // f2 matchup
    public static final int VIEW_TYPE_f2_matchup = 16;
    // f2 icon
    public static final int VIEW_TYPE_f2_icon_Show = 17;
    public static final int VIEW_TYPE_f2_icon_Ordered = 18;
    public static final int VIEW_TYPE_f2_icon_Radio = 19;
    public static final int VIEW_TYPE_f2_icon_Podcast = 20;

    // Feed card types start from this constant.
    public static final int VIEW_TYPE_Feed_START = 21;

    // feed standard
    public static final int VIEW_TYPE_Feed_Standard_Image = VIEW_TYPE_Feed_START;
    public static final int VIEW_TYPE_Feed_Standard_Video = 22;
    public static final int VIEW_TYPE_Feed_Standard_Show = 23;
    public static final int VIEW_TYPE_Feed_Standard_Podcast = 24;
    public static final int VIEW_TYPE_Feed_Standard_Radio = 25;

    //feed matchup
    public static final int VIEW_TYPE_feed_matchup = 26;

    // feed text only
    public static final int VIEW_TYPE_Feed_Standard_Text = 27;

    // feed icon
    public static final int VIEW_TYPE_feed_icon_Ordered = 28;
    public static final int VIEW_TYPE_feed_icon_Person = 29;

    // feed promo
    public static final int VIEW_TYPE_feed_promo_24_7 = 30;

    // promo coming from the feed...
    public static final int VIEW_TYPE_external_promo = 31;
    public static final int VIEW_TYPE_external_promo_text_only = 32;
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private final ViewPager viewPager;
    private final TeamFeedFragment teamFeedFragment;

    TeamsPagerFragment teamsPagerFragment;
    private final List<FeedComponent> mValues;
    public final Team team;
    private List<FragmentLifeCycleListener> fragmentLifeCycleListeners = new ArrayList<>();
    private int theFeedLabelIndex = -1;
    private GradientDrawable teamColorGradient;
    private boolean refreshed;
    private boolean isMediumViewHolderReused;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final TeamFeedItemClickListenerFactory clickListenerFactory;

    public int getTheFeedLabelIndex() {
        return theFeedLabelIndex;
    }

    public void setTheFeedLabelIndex(int theFeedLabelIndex) {
        this.theFeedLabelIndex = theFeedLabelIndex;
    }

    public TeamViewComponentsAdapter(TeamsPagerFragment teamsPagerFragment, List<FeedComponent> items,
                                     Team team, GradientDrawable teamColorGradient, ViewPager viewPager,
                                     TeamFeedFragment teamFeedFragment,
                                     PersistentPlayer persistentPlayer ) {
        this.teamsPagerFragment = teamsPagerFragment;
        mValues = items;
        this.team = team;
        this.teamColorGradient = teamColorGradient;
        this.viewPager = viewPager;
        this.teamFeedFragment = teamFeedFragment;

        this.clickListenerFactory = new TeamFeedItemClickListenerFactory(teamFeedFragment, team, mValues, persistentPlayer);
    }

    public boolean isFirstFeedCard(int position) {
        return (this.getTheFeedLabelIndex() + 1 == position);
    }

    /**
     * By the two callbacks, getItemViewType() and onCreateViewHolder(),
     * we can dynamically apply a respective ViewHolder and layout of a card
     * for every particular element of the RecyclerView list
     * when user scrolls to it and it gets rendered.
     * See how it's done in the previous NBCSports-Android app:
     * https://github.com/NBC-Sports-Group/NBCSports-Android/blob/master/app/src/main/java/com/nbc/nbcsports/ui/main/core/BaseSectionedRecyclerViewAdapter.java#L59
     * https://github.com/NBC-Sports-Group/NBCSports-Android/blob/master/app/src/main/java/com/nbc/nbcsports/ui/main/upcomingBubble/UpcomingBubbleAssetViewAdapter.java#L40
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        FeedComponent.Type type = mValues.get(position).getType();
        String cardType = mValues.get(position).getCardType();
        String contentType = mValues.get(position).getContentType();

        return TeamViewComponentsAdapter.getItemViewType(type, cardType, contentType);
    }

    static public int getItemViewType(FeedComponent.Type type, String cardType, String contentType) {
        switch (type) {
            case PERSISTENT_PLAYER_MEDIUM:
                return VIEW_TYPE_PERSISTENT_PLAYER_MEDIUM;
            case HEADER:
                return VIEW_TYPE_HEADER;
            case THEFEED_LABEL:
                return VIEW_TYPE_THEFEED_LABEL;
            case FOOTER:
                return VIEW_TYPE_FOOTER;
            case FEED_PROMO:
                return VIEW_TYPE_feed_promo_24_7;
            case COMPONENT:
            default:
                // Based on "cardType" and "contentType" of a particular element of "team-view"/"components",
                // return respective viewType.

                ///// The break-down based on all cardType / contentType variations - start ////////
                //TODO: Handle all other needed types...
                if (cardType.equals(Constants.CARD_TYPE_F1_Standard)) {
                    if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_IMAGE))
                        return VIEW_TYPE_F1_Standard_Image;
                    else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_VIDEO))
                        return VIEW_TYPE_F1_Standard_Video;
                    else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_AUDIO))
                        return VIEW_TYPE_F1_Standard_Audio;
                    else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_STEPPED_STORY))
                        return VIEW_TYPE_F1_Standard_Image;
                    else
                        return VIEW_TYPE_UNKNOWN;
                } else if (cardType.equals(Constants.CARD_TYPE_F1_Cut_Out)) {
                    if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_IMAGE)) {
                        return VIEW_TYPE_f1_cut_out_Image;
                    } else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_VIDEO)) {
                        return VIEW_TYPE_f1_cut_out_Video;
                    } else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_AUDIO)) {
                        return VIEW_TYPE_f1_cut_out_Audio;
                    } else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_STEPPED_STORY)) {
                        return VIEW_TYPE_f1_cut_out_Image;
                    } else {
                        return VIEW_TYPE_UNKNOWN;
                    }
                } else if (cardType.equals(Constants.CARD_TYPE_F2_Standard)) {
                    if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_IMAGE)) {
                        return VIEW_TYPE_f2_standard_Image;
                    } else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_VIDEO)) {
                        return VIEW_TYPE_f2_standard_Video;
                    } else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_AUDIO)) {
                        return VIEW_TYPE_f2_standard_Audio;
                    } else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_STEPPED_STORY)) {
                        return VIEW_TYPE_f2_standard_Image;
                    } else {
                        return VIEW_TYPE_UNKNOWN;
                    }
                } else if (cardType.equals(Constants.CARD_TYPE_Feed_Standard)) {
                    if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_IMAGE))
                        return VIEW_TYPE_Feed_Standard_Image;
                    else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_VIDEO))
                        return VIEW_TYPE_Feed_Standard_Video;
                    else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_Show))
                        return VIEW_TYPE_Feed_Standard_Show;
                    else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_Podcast))
                        return VIEW_TYPE_Feed_Standard_Podcast;
                    else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_Radio))
                        return VIEW_TYPE_Feed_Standard_Radio;
                    else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_TEXT))
                        return VIEW_TYPE_Feed_Standard_Text;
                    else if (contentType.equalsIgnoreCase(Constants.CONTENT_TYPE_STEPPED_STORY))
                        return VIEW_TYPE_Feed_Standard_Image;
                    else
                        return VIEW_TYPE_UNKNOWN;
                } else if (cardType.equals(Constants.CARD_TYPE_Feed_Promo)) {
                    return VIEW_TYPE_external_promo;
                } else {
                    return VIEW_TYPE_UNKNOWN;
                }
                ///// The break-down based on all cardType / contentType variations - end ////////
        }
    }

    /**
     * By the two callbacks, getItemViewType() and onCreateViewHolder(),
     * we can dynamically apply a respective ViewHolder and layout of a card...
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if (compositeDisposable == null || compositeDisposable.isDisposed()){
            compositeDisposable = null;
            compositeDisposable = new CompositeDisposable();
        }

        switch (viewType) {
            case VIEW_TYPE_PERSISTENT_PLAYER_MEDIUM:
                 view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_holder_persistent_player_medium, parent, false);
                ViewHolderPersistentPlayerMedium viewHolderPersistentPlayerMedium;

                // This is triggered when the medium view holder needs to be reused
                // 1. The medium view holder is showing on both before live assets auto refresh
                //    and after live assets auto refresh
                //    and with different media source
                // However, if the media source are the same, there is no need to call this method
                // Because the notifyItemRangeChanged(1, ...) will ignore the medium view holder
                if (isMediumViewHolderReused){
                    if (teamFeedFragment != null && teamFeedFragment.getApplicationLifecycleListener() != null &&
                            teamFeedFragment.getApplicationLifecycleListener().getPreviousViewHolderPersistentPlayerMedium() != null){
                        viewHolderPersistentPlayerMedium = teamFeedFragment.getApplicationLifecycleListener().getPreviousViewHolderPersistentPlayerMedium();
                        setMediumViewHolderReused(false);
                        return viewHolderPersistentPlayerMedium;
                    }
                }

                // This is trigger when the app is
                // 1. Background --> Foreground
                //    and the medium view holder is not showing before
                // 2. The app launches and the team feed loads as it first time
                // 3. Switching between teams
                // 4. Manual refresh team feed list
                viewHolderPersistentPlayerMedium
                        = new ViewHolderPersistentPlayerMedium(this, view, viewPager, viewType);
                // Save the current medium player view holder to application lifecycle listener
                if (teamFeedFragment != null && teamFeedFragment.getApplicationLifecycleListener() != null){
                    teamFeedFragment.getApplicationLifecycleListener()
                            .setPreviousViewHolderPersistentPlayerMedium(viewHolderPersistentPlayerMedium);
                }
                return viewHolderPersistentPlayerMedium;

            // f1 standard
            case VIEW_TYPE_F1_Standard_Image:
                //TODO: To have a single xml layout file? since it's more or less same layout between F1 Standard Video/Audio/Image cards.
                //TODO: Rename teamview_card_type_video.xml to teamview_card_type_media.xml?
                //.inflate( R.layout.teamview_card_type_image, parent, false );s
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_video, parent, false);
                ViewHolderTypeImage viewHolderTypeImage = new ViewHolderTypeImage(view, viewType);

                compositeDisposable.addAll(
                        clickListenerFactory.getClickListener(viewHolderTypeImage),
                        clickListenerFactory.getLongClickListener(viewHolderTypeImage)
                );

                return viewHolderTypeImage;

            case VIEW_TYPE_F1_Standard_Video:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_video, parent, false);
                ViewHolderTypeVideo viewHolderTypeVideo =  new ViewHolderTypeVideo(this, view, viewType);

                compositeDisposable.addAll(
                        clickListenerFactory.getClickListener(viewHolderTypeVideo),
                        clickListenerFactory.getLongClickListener(viewHolderTypeVideo)
                );

                return viewHolderTypeVideo;

            case VIEW_TYPE_F1_Standard_Audio:
                //TODO: To have a single xml layout file? since it's more or less same layout between F1 Standard Video/Audio/Image cards.
                //TODO: Rename teamview_card_type_video.xml to teamview_card_type_media.xml?
                //.inflate( R.layout.teamview_card_type_audio, parent, false );

                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_video, parent, false);
                ViewHolderTypeAudio viewHolderTypeAudio = new ViewHolderTypeAudio(view, viewType);

                compositeDisposable.addAll(
                        clickListenerFactory.getClickListener(viewHolderTypeAudio),
                        clickListenerFactory.getLongClickListener(viewHolderTypeAudio)
                );

                return viewHolderTypeAudio;
            // f1 cut out
            case VIEW_TYPE_f1_cut_out_Image:
            case VIEW_TYPE_f1_cut_out_Video:
            case VIEW_TYPE_f1_cut_out_Audio:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_cut_out_f1, parent, false);
                ViewHolderTypeF1CutOut viewHolderTypeCutOutF1 = new ViewHolderTypeF1CutOut(view, viewType);
                compositeDisposable.addAll(
                        clickListenerFactory.getClickListener(viewHolderTypeCutOutF1),
                        clickListenerFactory.getLongClickListener(viewHolderTypeCutOutF1)
                );
                return viewHolderTypeCutOutF1;
            // f2 standard
            case VIEW_TYPE_f2_standard_Video:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_standard_f2, parent, false);
                ViewHolderTypeF2Video viewHolderTypeF2Video = new ViewHolderTypeF2Video(this, view, viewType);
                compositeDisposable.addAll(
                        clickListenerFactory.getClickListener(viewHolderTypeF2Video),
                        clickListenerFactory.getLongClickListener(viewHolderTypeF2Video)
                );
                return viewHolderTypeF2Video;
            case VIEW_TYPE_f2_standard_Image:
            case VIEW_TYPE_f2_standard_Audio:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_standard_f2, parent, false);
                ViewHolderTypeF2Standard viewHolderTypeStandardF2 = new ViewHolderTypeF2Standard(view, viewType);
                compositeDisposable.addAll(
                        clickListenerFactory.getClickListener(viewHolderTypeStandardF2),
                        clickListenerFactory.getLongClickListener(viewHolderTypeStandardF2)
                );
                return viewHolderTypeStandardF2;
            // feed standard
            case VIEW_TYPE_Feed_Standard_Image:
            case VIEW_TYPE_Feed_Standard_Video:
            case VIEW_TYPE_Feed_Standard_Show:
            case VIEW_TYPE_Feed_Standard_Podcast:
            case VIEW_TYPE_Feed_Standard_Radio:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_feed_standard, parent, false);
                ViewHolderTypeFeedStandard viewHolderTypeFeedStandard = new ViewHolderTypeFeedStandard(this, view, viewType);

                compositeDisposable.addAll(
                        clickListenerFactory.getClickListener(viewHolderTypeFeedStandard),
                        clickListenerFactory.getLongClickListener(viewHolderTypeFeedStandard)
                );
                return viewHolderTypeFeedStandard;

            // feed text only
            case VIEW_TYPE_Feed_Standard_Text:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_feed_text_only, parent, false);
                ViewHolderTypeFeedTextOnly viewHolderTypeFeedTextOnly = new ViewHolderTypeFeedTextOnly(team, view, viewType);

                compositeDisposable.addAll(
                        clickListenerFactory.getClickListener(viewHolderTypeFeedTextOnly),
                        clickListenerFactory.getLongClickListener(viewHolderTypeFeedTextOnly)
                );

                return viewHolderTypeFeedTextOnly;

            case VIEW_TYPE_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_header, parent, false);
                return new ViewHolderTypeHeader(view, viewType);

            case VIEW_TYPE_THEFEED_LABEL:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_thefeed_label, parent, false);
                return new ViewHolderTypeTheFeedLabel(view, viewType);

            case VIEW_TYPE_FOOTER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_footer, parent, false);
                return new ViewHolderTypeHeader(view, viewType);

            case VIEW_TYPE_feed_promo_24_7:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_feed_promo, parent, false);
                ViewHolderTypeFeedPromo viewHolderTypeFeedPromo = new ViewHolderTypeFeedPromo(view, viewType);

                compositeDisposable.add(clickListenerFactory.getClickListener(viewHolderTypeFeedPromo));

                return viewHolderTypeFeedPromo;

            case VIEW_TYPE_external_promo:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teamview_card_type_promo, parent, false);
                ViewHolderTypePromo viewHolderTypePromo = new ViewHolderTypePromo(view, viewType);
                return viewHolderTypePromo;

            //TODO: create all other needed view types...
            //case VIEW_TYPE_Whatever:
            //	view = LayoutInflater.from(parent.getContext())
            //			.inflate(R.layout.teamview_card_type_Whatever, parent, false);
            //	return new ViewHolderType_Whatever(view);

            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.teamview_card_type_blank, parent, false);
                return new ViewHolderTypeBlank(view, viewType);
        }
    }


    @Override
    //public void onBindViewHolder(final ViewHolderTypeImage holder, int position) {
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        // Generic initialization of the base type
        if (viewHolder instanceof ViewHolderTypeBase) {
            ViewHolderTypeBase holder = (ViewHolderTypeBase) viewHolder;
            holder.mItem = mValues.get(position);
            // if not ViewHolderTypeHeader and not ViewHolderTypeTheFeedLabel
            if (!(viewHolder instanceof ViewHolderTypeHeader)
                    && !(viewHolder instanceof ViewHolderTypeTheFeedLabel)
                    && !(viewHolder instanceof ViewHolderTypeBlank)
                    ) {
                // We check components to be non-null for the cases where we don't include
                // the 'teamview_card_type_base_date1_and_region.xml' into this viewHolder's layout xml.

                if (position == 0) {
                    // It should be a ViewHolderTypeHeader for position 0, but we do not get here for it. Do nothing.
                } else if (position == 1 || position == 2) { // hack for now to accomodate live medium player
                    // The first card
                    if (holder.date1ContainerView != null)
                        holder.date1ContainerView.setVisibility(View.VISIBLE);
                    // TODO: get the "date1" from the JSON feed
                    if (holder.date1View != null) holder.date1View.setText(DateFormatUtils.getMonthDayYearText(holder.mItem.getPublishedDate())); // hardcoded for demo, so far //(team.date1);
                    if (holder.rsnName != null) holder.rsnName.setVisibility(View.VISIBLE);
                    if (holder.regionView != null) holder.regionView.setText(team.getRegionName());
                } else {
                    // The second, third, etc, cards...
//                    if (holder.date1ContainerView != null) holder.date1ContainerView.setVisibility(
//                            (viewHolder instanceof ViewHolderTypeFeedStandard ||
//                                    viewHolder instanceof ViewHolderTypeFeedTextOnly)
//                                    ? View.GONE : View.INVISIBLE);
//                    if (holder.rsnName != null) holder.rsnName.setVisibility(View.GONE);
                }
            }
        }

        int intPrimaryColor = Color.parseColor(team.getPrimaryColor());
        int intSecondaryColor = Color.parseColor(team.getSecondaryColor());
        String regionBackgroundURL = team.getRegionBackgroundGreyscaleUrl();
        String liveFeedPromoImageURL = team.getLiveFeedPromoImageUrl();
        boolean isLightTeam = team.getLightProfileTeam();
        boolean playNonFeatureGifs = ((MainActivity) teamsPagerFragment.getActivity()).getConfig().getPlayNonFeatureGifs();

        // Concrete initializations of the derived types
        if (viewHolder instanceof ViewHolderTypeImage) {
            ViewHolderTypeImage holder = (ViewHolderTypeImage) viewHolder;
            holder.mItem = mValues.get(position);
            holder.setCardAttributes(playNonFeatureGifs, team, teamColorGradient, intPrimaryColor, intSecondaryColor, regionBackgroundURL, isLightTeam);

        } else if (viewHolder instanceof ViewHolderTypeVideo) {

            ViewHolderTypeVideo holder = (ViewHolderTypeVideo) viewHolder;
            holder.mItem = mValues.get(position);
            holder.setCardAttributes(playNonFeatureGifs, team, teamColorGradient, intPrimaryColor, intSecondaryColor, regionBackgroundURL, isLightTeam);
            holder.playVideo(); // Only type video has video

        } else if (viewHolder instanceof ViewHolderTypeAudio) {
            ViewHolderTypeAudio holder = (ViewHolderTypeAudio) viewHolder;
            holder.mItem = mValues.get(position);
            holder.setCardAttributes(team, playNonFeatureGifs, teamColorGradient, intPrimaryColor, intSecondaryColor, regionBackgroundURL, isLightTeam);


        } else if (viewHolder instanceof ViewHolderTypeF1CutOut) {
            ViewHolderTypeF1CutOut holder = (ViewHolderTypeF1CutOut) viewHolder;
            holder.mItem = mValues.get(position);
            holder.setCardAttributes(playNonFeatureGifs, teamColorGradient, intPrimaryColor, intSecondaryColor, regionBackgroundURL, isLightTeam);
        } else if (viewHolder instanceof ViewHolderTypeF2Standard) {
            ViewHolderTypeF2Standard holder = (ViewHolderTypeF2Standard) viewHolder;
            holder.mItem = mValues.get(position);
            holder.setCardAttributes(playNonFeatureGifs, team, intPrimaryColor);
        } else if (viewHolder instanceof ViewHolderTypeFeedStandard) {
            ViewHolderTypeFeedStandard holder = (ViewHolderTypeFeedStandard) viewHolder;
            holder.mItem = mValues.get(position);
            holder.setCardAttributes(playNonFeatureGifs, isFirstFeedCard(position));

        } else if (viewHolder instanceof ViewHolderTypeFeedTextOnly) {
            ViewHolderTypeFeedTextOnly holder = (ViewHolderTypeFeedTextOnly) viewHolder;
            holder.mItem = mValues.get(position);
            holder.setCardAttributes(isFirstFeedCard(position), false);

        } else if (viewHolder instanceof ViewHolderTypeHeader) {
            ViewHolderTypeHeader holder = (ViewHolderTypeHeader) viewHolder;
            FeedComponent item = mValues.get(position);
            Team teamData = item.getTeam();
            if (teamData != null) {
                holder.setHeaderTextAndColors(teamData);
            }

        } else if (viewHolder instanceof ViewHolderTypeTheFeedLabel) {
            ViewHolderTypeTheFeedLabel theFeedLabel = (ViewHolderTypeTheFeedLabel) viewHolder;
            FeedComponent item = mValues.get(position);
            Team team = item.getTeam();
            if (team != null) {
                theFeedLabel.setAttributes(team);
            }

        } else if (viewHolder instanceof ViewHolderTypeBlank) {

        } else if (viewHolder instanceof ViewHolderPersistentPlayerMedium) {
            ViewHolderPersistentPlayerMedium mediumPlayerHolder = (ViewHolderPersistentPlayerMedium) viewHolder;
            mediumPlayerHolder.mItem = mValues.get(position);
            mediumPlayerHolder.setMediaSource(mediumPlayerHolder.mItem.getMediaSource());
            mediumPlayerHolder.setTeamPrimaryColorToMediumView();

        } else if (viewHolder instanceof ViewHolderTypeFeedPromo){
            ViewHolderTypeFeedPromo feedPromoViewHolder = (ViewHolderTypeFeedPromo) viewHolder;
            feedPromoViewHolder.mItem = mValues.get(position);
            feedPromoViewHolder.setCardAttributes(intPrimaryColor, liveFeedPromoImageURL, isLightTeam);

        } else if (viewHolder instanceof ViewHolderTypePromo) {
            ViewHolderTypePromo promoViewHolder = (ViewHolderTypePromo) viewHolder;
            promoViewHolder.mItem = mValues.get(position);
            promoViewHolder.setCardAttributes(playNonFeatureGifs, team.getPrimaryColor(), teamFeedFragment);
        } else {
            throw new IllegalArgumentException("Wrong RecyclerView.ViewHolder");
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        /*
        RecyclerVisibilityScrollListener won't trigger setVisible unless scrolled, so even if a view holder
        is visible to user when list is first drawn, setVisible won't be called. Adding setVisible here will
        ensure that it will get called when list is visible to user.
         */
        if (holder instanceof ViewHolderTypeBase) {
            ViewHolderTypeBase videoHolder = (ViewHolderTypeBase) holder;
            videoHolder.setVisible(true);
            videoHolder.addPageChangeListener();
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof ViewHolderTypeBase) {
            ViewHolderTypeBase videoHolder = (ViewHolderTypeBase) holder;

            // special logic for medium player holder
            if (refreshed && videoHolder instanceof ViewHolderPersistentPlayerMedium){
                // do nothing except setting refreshed back to false so that mini can be surfaced
                refreshed = false;
            } else {
                videoHolder.setVisible(false);
            }
            videoHolder.removePageChangeListener();
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        teamFeedFragment.onAdapterAttachedToRecyclerView();
    }

    public void onResume() {
        if (fragmentLifeCycleListeners != null) {
            for (FragmentLifeCycleListener listener : fragmentLifeCycleListeners) {
                listener.onResume();
            }
        }
    }

    public void onPause() {
        if (fragmentLifeCycleListeners != null) {
            for (FragmentLifeCycleListener listener : fragmentLifeCycleListeners) {
                listener.onPause();
            }
        }
    }

    @Override
    public void addFragmentLifeCycleListener(FragmentLifeCycleListener fragmentLifeCycleListener) {
        this.fragmentLifeCycleListeners.add(fragmentLifeCycleListener);
    }

    @Override
    public void showNativeShare(String title, String s) {

    }

    @Override
    public PullLayout getRefreshLayout() {
        return teamFeedFragment.getRefreshLayout();
    }

    public void onDestroy() {
        if (fragmentLifeCycleListeners != null) {
            fragmentLifeCycleListeners.clear();
        }

        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        compositeDisposable = null;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void updateData(List<FeedComponent> items) {
        mValues.clear();
        mValues.addAll(items);
        notifyDataSetChanged();
    }

    /**
     * This method is used to update the team feed list items base on conditions
     * This method will be called when the app
     * Background --> Foreground
     * and the Medium view holder exist before the app went into background
     * and the Medium view holder also exist after the app go to foreground
     *
     * Assuming medium view holder is in position 0
     *
     * @param items
     */
    public void updateDataWhenBackAndFore(List<FeedComponent> items) {
        int mValuesSize = mValues.size();
        // Adding this condition to prevent crash when
        // new items size < old items size
        // and call notifyItemRangeChanged directly
        // at the bottom of the recycler view
        if (items.size() < mValuesSize){
            for (int i=1; i<items.size(); i++){
                mValues.set(i, items.get(i));
            }
            for (int i=items.size(); i<mValuesSize; i++){
                mValues.remove(i);
            }
            notifyItemRangeRemoved(items.size(), mValuesSize - items.size());
            notifyItemRangeChanged(1, items.size() - 1);
        } else {
            mValues.clear();
            mValues.addAll(items);
            if (mValues.size() > 1) {
                notifyItemRangeChanged(1, mValues.size() - 1);
            } else if (mValues.size() == 0) {
                notifyDataSetChanged();
            }
        }
    }

    /**
     * This method is used to update the team feed list items base on conditions
     * This method will be called when
     * live assets are the Same on both before refresh and after refresh
     * and the Medium view holder exist before refresh
     * and the Medium view holder also exist after refresh
     *
     * @param items
     */
    public void updateDataWhenLiveAssetsAutoRefreshAndSameMediaSource(List<FeedComponent> items) {
        updateDataWhenBackAndFore(items);
    }

    /**
     * This method is used to update the team feed list items base on conditions
     * This method will be called when
     * live assets are Different on both before refresh and after refresh
     * and the Medium view holder exist before refresh
     * and the Medium view holder also exist after refresh
     *
     * @param items
     */
    public void updateDataWhenLiveAssetsAutoRefreshAndDiffMediaSource(List<FeedComponent> items) {
        int mValuesSize = mValues.size();
        // Adding this condition to prevent crash when
        // new items size < old items size
        // and call notifyItemRangeChanged directly
        // at the bottom of the recycler view
        if (items.size() < mValuesSize) {
            for (int i=0; i<items.size(); i++){
                mValues.set(i, items.get(i));
            }
            for (int i=items.size(); i<mValuesSize; i++){
                mValues.remove(i);
            }
            notifyItemRangeRemoved(items.size(), mValuesSize - items.size());
            notifyItemRangeChanged(0, items.size());
        } else {
            mValues.clear();
            mValues.addAll(items);
            if (mValues.size() > 1) {
                notifyItemRangeChanged(0, mValues.size());
            } else if (mValues.size() == 0) {
                notifyDataSetChanged();
            }
        }
    }

    public List<FeedComponent> getComponents() {
        return mValues;
    }

    public void setRefreshed(boolean refreshed) {
        this.refreshed = refreshed;
    }

    /**
     * This method needs to be called if updating team feed list
     * and the update is related to the Medium view holder
     *
     * The default value of the isMediumViewHolderReused is false
     *
     * @param isMediumViewHolderReused
     */
    public void setMediumViewHolderReused(boolean isMediumViewHolderReused) {
        this.isMediumViewHolderReused = isMediumViewHolderReused;
    }
}
