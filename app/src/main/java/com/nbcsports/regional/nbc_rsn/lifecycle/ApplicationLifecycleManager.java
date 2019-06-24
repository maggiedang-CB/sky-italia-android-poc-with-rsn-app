package com.nbcsports.regional.nbc_rsn.lifecycle;

import androidx.lifecycle.ProcessLifecycleOwner;

import java.util.ArrayList;
import java.util.List;

public class ApplicationLifecycleManager {

    private static List<ApplicationLifecycleListener> listenersList;

    private ApplicationLifecycleManager() {}

    public static void addApplicationLifecycleListener(
            ApplicationLifecycleListener applicationLifecycleListener) {
        if (applicationLifecycleListener != null){
            System.out.println("This is the enter point: Start adding lifecycle listener: "
                    +applicationLifecycleListener.getTeamFeedFragment().getTeam().getDisplayName());
            if (listenersList == null){
                listenersList = new ArrayList<>();
            }
            if (!listenersList.contains(applicationLifecycleListener)){
                ProcessLifecycleOwner.get().getLifecycle()
                        .addObserver(applicationLifecycleListener);
                listenersList.add(applicationLifecycleListener);
                System.out.println("This is the enter point: Success in adding lifecycle listener: "
                        +applicationLifecycleListener.getTeamFeedFragment().getTeam().getDisplayName());
            } else {
                System.out.println("This is the enter point: Fail in adding lifecycle listener: "
                        +applicationLifecycleListener.getTeamFeedFragment().getTeam().getDisplayName()
                        +" (already exist)");
            }
        }
    }

    public static void removeApplicationLifecycleListener(
            ApplicationLifecycleListener applicationLifecycleListener) {
        if (applicationLifecycleListener != null){
            System.out.println("This is the enter point: Start removing lifecycle listener: "
                    +applicationLifecycleListener.getTeamFeedFragment().getTeam().getDisplayName());
            ProcessLifecycleOwner.get().getLifecycle()
                    .removeObserver(applicationLifecycleListener);
            if (listenersList != null){
                listenersList.remove(applicationLifecycleListener);
                System.out.println("This is the enter point: Success in removing lifecycle listener: "
                        +applicationLifecycleListener.getTeamFeedFragment().getTeam().getDisplayName());
            } else {
                System.out.println("This is the enter point: Fail in removing lifecycle listener: "
                        +applicationLifecycleListener.getTeamFeedFragment().getTeam().getDisplayName()
                        +" (listenersList is null)");
            }
        }
    }

    public static void removeAllListeners() {
        if (listenersList != null){
            List<ApplicationLifecycleListener> listenersListTemp = new ArrayList<>(listenersList);
            System.out.println("This is the enter point: before clean listenersList: "+listenersListTemp.size());
            for (ApplicationLifecycleListener item : listenersListTemp){
                removeApplicationLifecycleListener(item);
            }
            System.out.println("This is the enter point: after clean listenersList: "+listenersList.size());
            listenersListTemp.clear();
        }
    }

}
