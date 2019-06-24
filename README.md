# sky-italia-android-poc-with-rsn-app
Sky Italia IMA POC for android using existing code from RSN app (https://github.com/NBC-Sports-Group/rsn-mobile-app-android)

>> To change ad tag for testing, go to file "imaAds.java" and change value of String variable "adTag" to the URL of the ad's VAST XML.

Currently It is able to:
- Play pre-roll on medium.
- Play pre-roll on landscape (Only if video is only played as landscape i.e Live Assets).
- Clicking LEARN MORE on the top corner of ad works and will not pause the ad or crash app.
- Scrolling down on an article will not show mini player when an ad is playing, but will instead pause ad until it is viewed again by scrolling up.
- If ad is playing initalially on medium (portrait) and orientation is changed to landscape, the app will not show landscape view until ad is done playing. (i.e Portrait view locked until ad is done if video is viewed as medium)


TODO:
- Figure out if we can get ad to play on mini.
- Figure out if we can get ad to continue playing on the correct view if medium -> landscape and landscape -> medium.
- Disable loading wheel animation (originaly for when the video buffers) when the ad is playing.
