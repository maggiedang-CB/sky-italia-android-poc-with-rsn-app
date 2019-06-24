package com.nbcsports.regional.nbc_rsn.common

class TeamFeedDataManager(val data: ArrayList<FeedComponent>, val team: Team) {

    // todo: data is only public because we don't want to refactor the data logic yet. Make it private some day.

    fun isTargetSteppedStory(targetPos: Int): Boolean {
        // todo: how are we deciding this?
        return true
    }
    
    fun findRecirculationItemPos(currentPos: Int): Int {
        var targetPos = currentPos + 1
        if (targetPos < data.size) {
            var fc = data[targetPos]
            // Note, the FeedComponent.Type.FOOTER is the type of the very last item that is expected to be at teem feed list.
            // So in this card's editorial list, add a Recirculation item only if the next item in the teem feed list
            // is a real card, i.e. of type COMPONENT or PERSISTENT_PLAYER_MEDIUM (under index 0) or FEED_PROMO (under index 11 or last but before FOOTER),
            // but not of types HEADER, THEFEED_LABEL or FOOTER.

            //Find next none feed promo, componentId != "" and content type not empty feed to display
            //Reset position to redirect to f1 card
            while (FeedComponent.Type.FEED_PROMO === fc.type || fc.componentId.isEmpty() || fc.contentType.isEmpty()) {
                if (targetPos < data.size - 1) {
                    fc = data[++targetPos]
                } else {
                    targetPos = 0
                }
            }

            if (FeedComponent.Type.HEADER !== fc.type && FeedComponent.Type.THEFEED_LABEL !== fc.type
                    && FeedComponent.Type.FOOTER !== fc.type) {
                return targetPos
            }
        }

        return -1
    }
}