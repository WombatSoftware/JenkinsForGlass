package de.wombatsoftware.glass.jenkins;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import de.wombatsoftware.glass.jenkins.model.Jenkins;

/**
 * Activity showing the options menu.
 */
public class MenuActivity extends Activity {
	private Jenkins jenkins;
	private JenkinsService.JenkinsBinder jenkinsBinder;
	
	private ServiceConnection mConnection = new ServiceConnection() { 
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof JenkinsService.JenkinsBinder) {
            	jenkinsBinder = ((JenkinsService.JenkinsBinder) service); 
            	jenkins = jenkinsBinder.getJenkins();
                openOptionsMenu();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Nothing to do here.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().bindService(new Intent(this, JenkinsService.class), mConnection, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.jenkins, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
        	case R.id.refresh:
        		refreshJenkins();
        		return true;

        	case R.id.settings:
        		return true;

            case R.id.stop:
                stopService(new Intent(this, JenkinsService.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        // Nothing else to do, closing the Activity.
        finish();
    }
    
    private void refreshJenkins() {
    	jenkins.init();
    	jenkinsBinder.republish();
    }

	@Override
	protected void onStop() {
		super.onStop();
		// TODO: Must it get unbound? 
		// unbindService(mConnection);
	}
}