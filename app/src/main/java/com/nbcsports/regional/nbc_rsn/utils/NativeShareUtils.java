package com.nbcsports.regional.nbc_rsn.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;

import com.google.common.collect.Sets;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.LiveAssetManager;
import com.nbcsports.regional.nbc_rsn.RsnApplication;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.editorial_detail.EditorialDetailTemplateFragment;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.deeplink.Deeplink;
import com.nbcsports.regional.nbc_rsn.stepped_story.SteppedStoryFragment;
import com.nbcsports.regional.nbc_rsn.team_feed.template.TeamFeedFragment;
import com.nbcsports.regional.nbc_rsn.team_view.TeamsPagerFragment;
import com.nbcsports.regional.nbc_rsn.common.FeedComponent;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import timber.log.Timber;

public class NativeShareUtils {

    private final static String TEMP_FILE_EXTENSION = ".png";
    private final static String TEMP_FILE_PREFIX = "temp_";
    private final static String INTENT_TYPE_IMAGE = "image/*";
    private final static String INTENT_TYPE_TEXT = "text/plain";

    private final static String COMPONENT_ID_SHARE = "component_id={COMPONENT_ID}&";
    private final static String ACTION_SHARE = "&action={ACTION}";
    private final static String COMPONENT_ID_KEY = "{COMPONENT_ID}";
    private final static String TEAM_ID_KEY = "{TEAM_ID}";
    private final static String ACTION_KEY = "{ACTION}";

    private final static int RESIZE_IMAGE_HEIGHT = 900;
    private final static int RESIZE_IMAGE_WIDTH = 1600;


    /**
     * Store picasso image target so that it will not get garbage collected in the process
     */
    private static Target imageTarget;


    private final static HashSet<String> IMAGE_INTENT_PACKAGE_NAME_LIST = Sets.newHashSet(
            "com.twitter.android"
    );

    private final static HashSet<String> IMAGE_SHARE_COMPONENT_CONTENT_TYPES = Sets.newHashSet(
            "image", "video", "audio", "match up", "show", "podcast", "radio"
    );

    private static String generateTeamShareLink(Fragment fragment, String teamId) {
        String shareUrl = getBaseShareUrl(fragment);
        if (isAnyStringNullOrEmpty(shareUrl, teamId)) return null;

        shareUrl = shareUrl.replace(TEAM_ID_KEY, teamId);
        shareUrl = shareUrl.replace(COMPONENT_ID_SHARE, "");
        shareUrl = shareUrl.replace(ACTION_SHARE, "");

        if (!(fragment instanceof TeamFeedFragment || fragment instanceof TeamsPagerFragment
                || fragment instanceof EditorialDetailTemplateFragment || fragment instanceof SteppedStoryFragment)) {
            shareUrl = null;
        }
        return shareUrl;
    }

    private static String generateShareLink(Fragment fragment, String teamId, String componentId, MediaSource mediaSource) {
        String shareUrl = getBaseShareUrl(fragment);
        if (isAnyStringNullOrEmpty(shareUrl, teamId, componentId)) return null;

        shareUrl = shareUrl.replace(COMPONENT_ID_KEY, componentId);
        shareUrl = shareUrl.replace(TEAM_ID_KEY, teamId);

        // First check if is sharing an video
        if (mediaSource != null && mediaSource != null && mediaSource.getDeeplink() != null) {
            if (mediaSource.getDeeplink().getType() == Deeplink.Type.COMPONENT) {
                shareUrl = shareUrl.replace(ACTION_KEY, fragment.getString(R.string.deeplink_action_open));
            } else if (mediaSource.getDeeplink().getType() == Deeplink.Type.TEAM) {
                shareUrl = shareUrl.replace(ACTION_KEY, fragment.getString(R.string.deeplink_action_linkto));
            }
        } else if (fragment instanceof TeamFeedFragment || fragment instanceof TeamsPagerFragment
                || fragment instanceof EditorialDetailTemplateFragment
                || fragment instanceof SteppedStoryFragment) {
            shareUrl = shareUrl.replace(ACTION_KEY, fragment.getString(R.string.deeplink_action_open));
        } else {
            shareUrl = null;
        }
        return shareUrl;
    }

    public static String getBaseShareUrl(Fragment fragment) {
        MainActivity mainActivity = (MainActivity) fragment.getActivity();
        if (mainActivity == null
                || mainActivity.getConfig() == null
                || mainActivity.getConfig().getSharing() == null
                || mainActivity.getConfig().getSharing().getSmartLink() == null)
            return null;

        //return mainActivity.getConfig().getSharing().getSmartLink();
        return mainActivity.getConfig().getSharing().getSmartLink();
    }

    private static String generateFacebookLink(Fragment fragment, String componentId) {
        String facebookBaseUrl = getFacebookShareBaseUrl(fragment);
        if (isAnyStringNullOrEmpty(componentId, facebookBaseUrl))
            return null; // checks for null and empty string
        return facebookBaseUrl.replace(COMPONENT_ID_KEY, componentId);
    }

    public static String getFacebookShareBaseUrl(Fragment fragment) {
        MainActivity mainActivity = (MainActivity) fragment.getActivity();
        if (mainActivity == null
                || mainActivity.getConfig() == null
                || mainActivity.getConfig().getSharing() == null
                || mainActivity.getConfig().getSharing().getFacebookShareBaseUrl() == null)
            return null;

        return mainActivity.getConfig().getSharing().getFacebookShareBaseUrl();
    }

    private static boolean isAnyStringNullOrEmpty(String... params) {
        for (String param : params) {
            if (param == null || param.isEmpty()) return true;
        }
        return false;
    }

    public static ShareInfo generateShareInfo(Fragment fragment, FeedComponent feedComponent, String title, MediaSource mediaSource, String teamId) {
        if (fragment == null) return null;

        MainActivity mainActivity = (MainActivity) fragment.getActivity();
        if (feedComponent != null) {
            String componentId = feedComponent.getComponentId();
            String shareUrl = generateShareLink(fragment, teamId, componentId, null);
            String facebookShareUrl = generateFacebookLink(fragment, componentId);

            if (isAnyStringNullOrEmpty(componentId, shareUrl, facebookShareUrl)) return null;

            return new ShareInfo(mainActivity, feedComponent, shareUrl, facebookShareUrl, null, title, feedComponent.getMediaSource());
        } else if (mediaSource != null && mediaSource.getDeeplink() != null && !mediaSource.getDeeplink().getComponentId().isEmpty()) {
            String componentId = mediaSource.getDeeplink().getComponentId();
            String shareUrl = generateShareLink(fragment, teamId, componentId, mediaSource);
            String facebookShareUrl = generateFacebookLink(fragment, componentId);
            if (isAnyStringNullOrEmpty(componentId, shareUrl, facebookShareUrl)) return null;

            return new ShareInfo(mainActivity, null, shareUrl, facebookShareUrl, null, title, mediaSource);
        } else {
            String shareUrl = generateTeamShareLink(fragment, teamId);
            return new ShareInfo(mainActivity, null, shareUrl, null, null, title, mediaSource);
        }
    }

    public static void share(ShareInfo shareInfo) {
        if (shareInfo.getFeedComponent() != null && componentRequiresImage(shareInfo.getFeedComponent())) {
            createAndDisplayChooserWithImage(shareInfo);
        } else if (shareInfo.getFeedComponent() == null) {
            createAndDisplayChooserWithImageForPlayer(shareInfo);
        } else {
            createAndDisplayChooser(shareInfo);
        }
    }

    private static boolean componentRequiresImage(FeedComponent feedComponent) {
        String imageUrl = feedComponent.getImageAssetUrl();

        String contentType = feedComponent.getContentType();
        return !imageUrl.isEmpty() && IMAGE_SHARE_COMPONENT_CONTENT_TYPES.contains(contentType);
    }

    private static void createAndDisplayChooserWithImageForPlayer(ShareInfo shareInfo) {

        if (shareInfo.getActivity() == null) {
            Timber.e("createAndDisplayChooserWithImageForPlayer() - activity == null");
            return;
        }

        MainActivity mainActivity = shareInfo.getActivity();
        MediaSource mediaSource = shareInfo.getMediaSource();

        String liveImageBaseUrl = LiveAssetManager.getInstance().getAssetImageBaseUrl();
        String imageUrlSizeSuffix = mainActivity.getResources().getString(R.string.image_size_suffix);
        String imageUrl = "";

        if (mediaSource != null && mediaSource.getImage() != null
                && !mediaSource.getImage().isEmpty()
                && !liveImageBaseUrl.isEmpty()) {
            imageUrl = String.format("%s%s%s", liveImageBaseUrl, mediaSource.getImage(), imageUrlSizeSuffix);
        }

        if (imageUrl.isEmpty()) {
            createAndDisplayChooser(shareInfo);
            return;
        }

        imageTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                final File toBeSharedFolder = new File(mainActivity.getFilesDir(), mainActivity.getString(R.string.file_provider_directory));
                toBeSharedFolder.mkdirs();

                File toBeSharedFile = null;
                try {
                    toBeSharedFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_EXTENSION, toBeSharedFolder);
                    OutputStream outputStream = new FileOutputStream(toBeSharedFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                shareInfo.setShareFile(toBeSharedFile); //Assigns null when fail fails to save correctly
                createAndDisplayChooser(shareInfo);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                createAndDisplayChooser(shareInfo);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }

        };
        Picasso.get().load(imageUrl).resize(RESIZE_IMAGE_WIDTH, RESIZE_IMAGE_HEIGHT).centerInside().into(imageTarget);
    }


    private static void createAndDisplayChooserWithImage(ShareInfo shareInfo) {

        MainActivity mainActivity = shareInfo.getActivity();
        MediaSource mediaSource = shareInfo.getMediaSource();

        String imageUrl = "";
        if (!shareInfo.getFeedComponent().getCardType().contains("cut_out")
                && shareInfo.getFeedComponent().getContentType().equals("video")
                && mediaSource != null) {
            if (mediaSource.getImage() != null && !mediaSource.getImage().isEmpty()) {
                imageUrl = mediaSource.getImage();
            }
        }

        if (!shareInfo.getFeedComponent().getCardType().contains("cut_out")
                && imageUrl.isEmpty()) {
            imageUrl = shareInfo.getFeedComponent().getImageAssetUrl();
        }

        if (imageUrl.isEmpty()) {
            createAndDisplayChooser(shareInfo);
            return;
        }

        imageTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                final File toBeSharedFolder = new File(mainActivity.getFilesDir(), mainActivity.getString(R.string.file_provider_directory));
                toBeSharedFolder.mkdirs();

                File toBeSharedFile = null;
                try {
                    toBeSharedFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_EXTENSION, toBeSharedFolder);
                    OutputStream outputStream = new FileOutputStream(toBeSharedFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();

                } catch (IOException e) {
                    Timber.e(e);
                }
                shareInfo.setShareFile(toBeSharedFile); //Assigns null when fail fails to save correctly
                createAndDisplayChooser(shareInfo);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                createAndDisplayChooser(shareInfo);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        Picasso.get().load(imageUrl).resize(1600, 900).centerInside().into(imageTarget);
    }

    public static void createAndDisplayChooser(ShareInfo shareInfo) {

        MainActivity mainActivity = shareInfo.getActivity();
        PackageManager packageManager = mainActivity.getPackageManager();

        String shareUrl = shareInfo.getShareUrl();
        String shareTitle = shareInfo.getShareTitle();
        String shareText = getShareText(shareTitle, shareUrl);

        //imageFile is null if FeedComponent isn't an item to be shared with image,
        // or if image failed to load.
        File imageFile = shareInfo.getShareFile();
        Uri imageUri = convertImageFileToUri(mainActivity, imageFile);

        Intent emailIntent = createEmailIntent(shareTitle, shareText);

        // Collect all the mail apps so that we can filter them out in sendIntent
        HashSet<String> emailPackageNames = getEmailPackageNames(packageManager.queryIntentActivities(emailIntent, 0));

        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(createSendIntentTemplate(), 0);
        ArrayList<Intent> extraIntents = new ArrayList<>();

        for (ResolveInfo re : resolveInfoList) {
            String packageName = re.activityInfo.packageName;
            String name = re.activityInfo.name;

            // Initializes intents where the package names are not in the email package list.
            if (!emailPackageNames.contains(packageName)) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setComponent(new ComponentName(packageName, name));

                if (packageName.contains("facebook")) { // facebook uses its own share link

                    // url that has been verified to work: http://stage.rsnhub.nbcsports.com/node/61
                    intent.putExtra(Intent.EXTRA_TEXT, shareInfo.getShareFacebookUrl());
                    Timber.e("Facebook url: %s", shareInfo.getShareFacebookUrl());
                    intent.setType(INTENT_TYPE_TEXT);

                } else if (IMAGE_INTENT_PACKAGE_NAME_LIST.contains(packageName) && imageUri != null) { //If valid, provide image
                    intent.putExtra(Intent.EXTRA_TEXT, shareText);
                    intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    intent.setType(INTENT_TYPE_IMAGE);

                } else {
                    intent.putExtra(Intent.EXTRA_TEXT, shareText);
                    intent.setType(INTENT_TYPE_TEXT);
                }
                extraIntents.add(intent);
            }
        }

        mainActivity.displayChooser(createShareChooser(
                emailIntent,
                extraIntents,
                mainActivity.getString(R.string.share_chooser_title))
        );
    }

    private static Intent createEmailIntent(String shareTitle, String shareText) {
        String emailTo = "mailto:";
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        String emailSubject = String.format("%s %s", shareTitle, RsnApplication.getInstance().getResources().getString(R.string.by_rsn));
        Uri uri = Uri.parse(emailTo);
        emailIntent.setData(uri);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        return emailIntent;
    }

    private static HashSet<String> getEmailPackageNames(List<ResolveInfo> emailResolveInfoList) {
        HashSet<String> emailPackageNames = new HashSet<>();
        for (ResolveInfo resolveInfo : emailResolveInfoList) {
            emailPackageNames.add(resolveInfo.activityInfo.packageName);
        }
        return emailPackageNames;
    }

    //Returns a base send intent for creating a resolveInfoList.
    private static Intent createSendIntentTemplate() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType(INTENT_TYPE_TEXT);
        return sendIntent;
    }

    private static Intent createShareChooser(Intent emailIntent, ArrayList<Intent> sendIntents, String chooserTitle) {
        //Initialize a chooser intent using emailIntent. (All email possible apps will be added)
        Intent chooser = Intent.createChooser(emailIntent, chooserTitle);
        Intent[] intents = new Intent[sendIntents.size()];
        intents = sendIntents.toArray(intents);

        //Add send intents to the chooser. (Now it will have both email, and regular send intents).
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
        return chooser;
    }

    //If imageFile is not null, returns a Uri generated for sharing. (path begins with: content:// instead of file://)
    //  Otherwise, returns null.
    private static Uri convertImageFileToUri(MainActivity mainActivity, File imageFile) {
        return imageFile != null ? FileProvider.getUriForFile(
                mainActivity,
                mainActivity.getString(R.string.file_provider_authority),
                imageFile)
                : null;
    }

    /**
     * This method is used to generate the text for sharing
     * <p>
     * Note: if shareTitle is null or empty, the default text from
     * localization json will be used
     *
     * @param shareTitle
     * @param shareUrl
     * @return customized share text
     */
    private static String getShareText(String shareTitle, String shareUrl) {
        String customizedNativeShareText = "";

        if (shareTitle != null && !shareTitle.isEmpty()) {
            customizedNativeShareText = customizedNativeShareText + shareTitle;
        } else {
            customizedNativeShareText = customizedNativeShareText + LocalizationManager.NativeShareMessages.DefaultShareMessage;
        }

        if (shareUrl != null && !shareUrl.isEmpty()) {
            customizedNativeShareText = customizedNativeShareText + "\n\n" + shareUrl;
        }

        return customizedNativeShareText;
    }

    @Getter
    @Setter
    public static class ShareInfo { //Data class for sharing information
        MainActivity activity;
        FeedComponent feedComponent;
        String shareTitle;
        String shareUrl;
        File shareFile;
        String shareFacebookUrl;
        MediaSource mediaSource;

        private ShareInfo() {
        }

        public ShareInfo(MainActivity activity, FeedComponent feedComponent, String shareUrl, String shareFacebookUrl, File shareFile, String shareTitle, MediaSource mediaSource) {
            this.activity = activity;
            this.feedComponent = feedComponent;
            this.shareUrl = shareUrl;
            this.shareFacebookUrl = shareFacebookUrl;
            this.shareFile = shareFile;
            this.shareTitle = shareTitle;
            this.mediaSource = mediaSource;
        }
    }

    public static void release() {
        imageTarget = null;
    }
}
