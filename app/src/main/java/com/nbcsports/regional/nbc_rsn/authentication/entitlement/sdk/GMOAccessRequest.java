package com.nbcsports.regional.nbc_rsn.authentication.entitlement.sdk;

import java.util.Arrays;

public class GMOAccessRequest {

    public User user;

    private GMOAccessRequest(User user) {
        this.user = user;
    }

    public static GMOAccessRequest buildEntitlementRequest(String channel, String mvpdId, String zip, boolean encryptedZip){
        User user = new User();
        user.authorizedResources = new String[]{channel};
        if (mvpdId != null){
            user.adobeMvpdId = mvpdId;
        }
        if (encryptedZip) {
            user.encryptedServiceZip = zip;
        } else {
            user.serviceZip = zip;
        }
        return new GMOAccessRequest(user);
    }

    public static GMOAccessRequest buildBlackoutRequest(String channel, String mvpdId, String zip, boolean encryptedZip) {
        User user = new User();
        if (mvpdId != null){
            user.adobeMvpdId = mvpdId;
        }
        user.authorizedResources = new String[]{channel};
        if (encryptedZip) {
            user.encryptedServiceZip = zip;
        } else {
            user.serviceZip = zip;
        }
        return new GMOAccessRequest(user);
    }

    @Override
    public String toString() {
        return "AnvatoRequest{" +
                "user=" + user +
                '}';
    }

    static class User {
        public String adobeMvpdId;
        public String[] authorizedResources;
        public String device = "android";
        public String serviceZip;
        public String encryptedServiceZip;

        @Override
        public String toString() {
            return "User{" +
                    "adobeMvpdId='" + adobeMvpdId + '\'' +
                    "device='" + device + '\'' +
                    ", authorizedResources=" + Arrays.toString(authorizedResources) +
                    ", serviceZip='" + serviceZip + '\'' +
                    ", encryptedServiceZip='" + encryptedServiceZip + '\'' +
                    '}';
        }
    }
}
