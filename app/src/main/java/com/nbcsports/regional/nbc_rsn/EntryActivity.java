package com.nbcsports.regional.nbc_rsn;


import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.nbcsports.regional.nbc_rsn.deeplink.DeeplinkManager;

import static com.nbcsports.regional.nbc_rsn.deeplink.DeeplinkManager.State.PENDING;

// https://stackoverflow.com/a/34499615
public class EntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        DeeplinkManager manager = DeeplinkManager.getInstance();
        if (manager.setDeeplinkIntent(getIntent())) {
            manager.setState(PENDING);
        }

        // Check to see if this Activity is the root activity
        if (isTaskRoot()) {
            /*
            // This Activity is the only Activity, so
            //  the app wasn't running. So start the app from the
            //  beginning (redirect to MainActivity)
            Intent mainIntent = getIntent(); // Copy the Intent used to launch me
            // Launch the real root Activity (launch Intent)
            mainIntent.setClass(this, MainActivity.class);

            // I'm done now, so finish()
            //startActivity(mainIntent);
            */

            // Start MainActivity as new regardless of intent type.
            //   if getIntent() is an intent for with a deeplink, it is stored in DeeplinkManager and
            //   handled MainActivity.loadFirstScreen()
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // App was already running, so just finish, which will drop the user
            //  in to the activity that was at the top of the task stack
            finish();
        }
    }
}
