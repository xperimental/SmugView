package net.sourcewalker.smugview.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * This service is responsible for providing the {@link Authenticator} as an
 * {@link IBinder}. It is not used directly by any applications but instead by
 * the AccountManager.
 * 
 * @author Xperimental
 */
public class AuthenticationService extends Service {

    private Authenticator authenticator;

    /*
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        authenticator = new Authenticator(this);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent arg0) {
        return authenticator.getIBinder();
    }

}
