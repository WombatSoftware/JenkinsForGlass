package de.wombatsoftware.glass.jenkins;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
	private static final int VIEW_DETAILS = 100;
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

	private boolean scanStarted = false;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");
				//String format = data.getStringExtra("SCAN_RESULT_FORMAT");

				SharedPreferences settings = getSharedPreferences(JenkinsService.PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
			    editor.putString(JenkinsService.PREFS_JENKINS_URL, contents);
			    editor.commit();

				jenkinsBinder.refreshJenkins();
				finish();
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		}
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
        		jenkinsBinder.refreshJenkins();

        		return true;

        	case R.id.details:
        		showJobDetails();
        		return true;

        	case R.id.settings:
        		setupJenkins();
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

    	if(!scanStarted) {
    		finish();
    	}
    }
    
    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
    	SharedPreferences settings = getSharedPreferences(JenkinsService.PREFS_NAME, 0);
	    String url = settings.getString(JenkinsService.PREFS_JENKINS_URL, null);

		if (url == null) {
			MenuItem refresh = menu.findItem(R.id.refresh);
			MenuItem details = menu.findItem(R.id.details);

			refresh.setVisible(false);
			details.setVisible(false);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
    public void onResume() {
        super.onResume();
        openOptionsMenu();
    }

    private void setupJenkins() {
    	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
        scanStarted = true;
    }

    private void showJobDetails() {
        Intent viewJobDetailsIntent = new Intent(this, ViewJobDetailsActivity.class);
        viewJobDetailsIntent.putExtra(ViewJobDetailsActivity.EXTRA_JENKINS, jenkins);
        startActivityForResult(viewJobDetailsIntent, VIEW_DETAILS);
    }

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().bindService(new Intent(this, JenkinsService.class), mConnection, 0);
    }

	@Override
	protected void onStop() {
		super.onStop();
		// TODO: Must it get unbound? 
		// unbindService(mConnection);
	}
}