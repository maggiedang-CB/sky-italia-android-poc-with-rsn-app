package com.nbcsports.regional.nbc_rsn.authentication.entitlement.sdk;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class GMOAccessResponse {

    @Expose
    String entitled;

    @Expose
    boolean blackout;

    @Expose
    String exception;

    @Expose
    String description;

    @Expose @SerializedName("alternate_content")
    String alternateContent;

    @Expose
    Station station;

    @Expose
    Dev dev;

    private GMOAccessRequest request;
    public GMOAccessRequest getRequest() { return request; }
    public void setRequest(GMOAccessRequest request) { this.request = request; }

    public boolean isEntitled() {
        return entitled!=null && entitled.equals("yes");
    }

    public boolean hasStreamingRights() {
        if (station != null && station.hasStreamingRights != null) {
            return Boolean.parseBoolean(station.hasStreamingRights);
        } else {
            return true; // if station or station.hasStreamingRights is null, then return true.
        }
    }

    @Override
    public String toString() {
        return "AnvatoResponse{" +
                "entitled='" + entitled + '\'' +
                ", blackout=" + blackout +
                ", exception='" + exception + '\'' +
                ", description='" + description + '\'' +
                ", alternateContent=" + alternateContent +
                ", station=" + station +
                ", dev=" + dev +
                ", request=" + request +
                '}';
    }


    public static class Station {

        @Expose
        String callsign;

        @Expose
        @SerializedName("has_tve_rights")
        String hasTveRights;

        @Expose
        @SerializedName("has_mvpd_rights")
        String hasMvpdRights;

        @Expose
        @SerializedName("has_streaming_rights")
        String hasStreamingRights;

        @Expose
        @SerializedName("dma_code")
        String dmaCode;

        @Override
        public String toString() {
            return "Station{" +
                    "callsign='" + callsign + '\'' +
                    ", hasTveRights='" + hasTveRights + '\'' +
                    ", hasMvpdRights='" + hasMvpdRights + '\'' +
                    ", hasStreamingRights='" + hasStreamingRights + '\'' +
                    ", dmaCode='" + dmaCode + '\'' +
                    '}';
        }
    }

    public static class Dev {
        @Expose
        @SerializedName("gdt_home")
        GdtHome gdtHome;

        @Override
        public String toString() {
            return "Dev{" +
                    "gdtHome=" + gdtHome +
                    '}';
        }
    }

    public static class GdtHome {
        @Expose
        List<Team> teams;

        @Override
        public String toString() {
            return "GdtHome{" +
                    "teams=" + teams +
                    '}';
        }
    }

    public static class Team {
        @Expose
        String team;

        @Expose
        @SerializedName("team_id")
        String teamId;

        @Expose
        String sport;

        @Expose
        List<String> rsns;

        @Override
        public String toString() {
            return "Team{" +
                    "team='" + team + '\'' +
                    ", teamId='" + teamId + '\'' +
                    ", sport='" + sport + '\'' +
                    ", rsns=" + rsns +
                    '}';
        }
    }

    /*{
      "entitled": "no",
      "blackout": "true",
      "description": "User is not entitled for this event (NASCAR Rule)",
      "not_authorized_reasons": [
        {
          "code": 360,
          "reason": "User is not entitled because User's MVPD (Dish) is blocked with this event. (Dish)"
        }
      ],
      "dev": {
        "event": {
          "event_id": null,
          "title": "XFINITY SERIES RACE",
          "blackout_rule": "NASCAR",
          "NASCAR Rule(s)": "Dish"
        },
        "input": {
          "user": {
            "adobeMvpdId": "Dish",
            "authorizedResources": [
              "nbcsports"
            ],
            "serviceZip": "83714"
          },
          "event_id": "5049004",
          "upid": "",
          "callsign": "",
          "anvack": "nbcu_nbcsn_nbcsn_android_qa_7e7f07b29b0c5d4c61c139dc688c7f73f9c13bb3"
        }
      }
    }*/
}
