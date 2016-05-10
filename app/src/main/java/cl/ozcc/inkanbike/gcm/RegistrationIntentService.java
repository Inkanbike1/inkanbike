package cl.ozcc.inkanbike.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import cl.ozcc.inkanbike.objects.DataHelper;
import cl.ozcc.inkanbike.objects.User;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    public RegistrationIntentService() {
        super(TAG);
        Log.v("DEBUG_TOKEN", "REGISTRATION_INTENT");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
            String token = instanceID.getToken("112040571933", //"591464106786",
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            new DataHelper(getApplicationContext()).updateToken(token);
            new User().subscribeTopics(getApplicationContext(), token);

        } catch (Exception e) {}
        Intent registrationComplete = new Intent("registrationComplete");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(registrationComplete);
    }
}