package net.sourcewalker.smugview.data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * This service provides the binder for the {@link SmugViewSyncAdapter}.
 * 
 * @author Xperimental
 */
public class SmugViewSyncService extends Service {

    private SmugViewSyncAdapter syncAdapter;

    /*
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        syncAdapter = new SmugViewSyncAdapter(this);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }

}
