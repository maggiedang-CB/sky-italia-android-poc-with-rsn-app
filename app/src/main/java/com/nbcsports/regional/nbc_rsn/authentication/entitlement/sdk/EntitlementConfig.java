package com.nbcsports.regional.nbc_rsn.authentication.entitlement.sdk;

import java.util.ArrayList;


public class EntitlementConfig {
    public ArrayList<String> entitlementChannels;
    public String RSNEntitlementsURL;
    public String entitlementURL;
    public String retransmissionURL;
    public String GMOAccessDomain;
    public String NBCDomain;
    public String NBCBlackoutURL;
    public String MLBBlackoutURL;
    public String GMOKey;
    public String deviceType; //iphone, ipad, appletv
    public Boolean isDeportesTelemundo;
}

    /*
        func toBool(_ key:String, dict:Dictionary<String,AnyObject>) -> Bool? {
        if let string = dict[key] as? String {
        return toBool(string: string)
        } else {
        return dict[key] as? Bool
        }
        }

        func toBool(string: String) -> Bool? {
        switch string.lowercased() {
        case "true","yes","1":
        return true
        case "false","no","0":
        return false
default:
        return nil
        }
        }
        }

        struct EntitlementErrorResult {
        var errorCode:EntitlementError
        var errorString:String? //this is the error string that is returned from the service being called for programmer reference. The consuming application should map error codes to customer friendly messages
        }
*/

