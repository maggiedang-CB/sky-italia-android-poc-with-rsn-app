package com.nbcsports.regional.nbc_rsn.urban_airship;

import android.content.Context;
import androidx.annotation.NonNull;

import com.urbanairship.AirshipReceiver;
import com.urbanairship.actions.ActionValue;
import com.urbanairship.actions.DeepLinkAction;
import com.urbanairship.actions.tags.AddTagsAction;
import com.urbanairship.json.JsonList;
import com.urbanairship.json.JsonValue;
import com.urbanairship.push.PushMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PushNotificationReceiver extends AirshipReceiver {

    public PushNotificationReceiver() {
        super();
    }

    @Override
    protected void onNotificationPosted(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {

        PushMessage message = notificationInfo.getMessage();
        Map<String, ActionValue> messageMap = message.getActions();
        List<String> messageKeys = new ArrayList<>(messageMap.keySet());
        List<String> tagsAttached = new ArrayList<>();
        String deeplinkUri = null;
        String notificationText = null;

        for (String key : messageKeys) {
            JsonValue actionJsonValue = messageMap.get(key).toJsonValue();

            if (key.equals(DeepLinkAction.DEFAULT_REGISTRY_SHORT_NAME)) {
                deeplinkUri = actionJsonValue.getString();

            } else if (key.equals(AddTagsAction.DEFAULT_REGISTRY_SHORT_NAME)) {
                JsonList messageJsonList = actionJsonValue.getList();
                for (JsonValue messageJsonElement : messageJsonList) {
                    String tag = messageJsonElement.getString();
                    if (tag != null && !tag.isEmpty()) {
                        tagsAttached.add(tag);
                    }
                }
                notificationText = notificationInfo.getMessage().getAlert();
            }
        }

        if (notificationText == null) return;

        if (deeplinkUri == null) {
            NotificationsManagerKt.INSTANCE.showPushNotification(tagsAttached, notificationText, notificationInfo.getNotificationId());
        } else {
            NotificationsManagerKt.INSTANCE.showPushNotification(tagsAttached, notificationText, notificationInfo.getNotificationId(), deeplinkUri);
        }
    }
}
