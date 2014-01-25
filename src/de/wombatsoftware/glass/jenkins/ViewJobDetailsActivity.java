package de.wombatsoftware.glass.jenkins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollView;

import de.wombatsoftware.glass.jenkins.model.Jenkins;
import de.wombatsoftware.glass.jenkins.model.Jenkins.Credentials;
import de.wombatsoftware.glass.jenkins.model.Job;

/**
 * Activity to set the timer.
 */
public class ViewJobDetailsActivity extends Activity implements GestureDetector.ScrollListener, GestureDetector.BaseListener{

    public static final String EXTRA_JENKINS = "jenkins";
    private static final String TAG = "ViewJobDetailsActivity";

    private Jenkins jenkins;
    private JenkinsService.JenkinsBinder jenkinsBinder;
    private ViewJobDetailsScrollAdapter mAdapter;
    
    private ServiceConnection mConnection = new ServiceConnection() { 
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof JenkinsService.JenkinsBinder) {
            	jenkinsBinder = ((JenkinsService.JenkinsBinder) service); 
            	jenkins = jenkinsBinder.getJenkins();
            	
            	mAdapter = new ViewJobDetailsScrollAdapter(ViewJobDetailsActivity.this, jenkins);

                mView.setAdapter(mAdapter);
                setContentView(mView);

                mDetector = new GestureDetector(ViewJobDetailsActivity.this).setBaseListener(ViewJobDetailsActivity.this).setScrollListener(ViewJobDetailsActivity.this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Nothing to do here.
        }
    };

    private GestureDetector mDetector;
    
    private CardScrollView mView;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mDetector.onMotionEvent(event);
    }

    @Override
	public boolean onGesture(Gesture gesture) {
		Log.d(TAG, "Gesture: " + gesture.toString());

		if (gesture == Gesture.TWO_TAP) {
			Credentials credentials = jenkins.getCredentials();
			String token = credentials.getToken();

			if (token != null) {
				Job job = jenkins.getJobs().get(mView.getSelectedItemPosition());
				HttpClient httpclient = new DefaultHttpClient();
				String postUrl = job.getUrl() + "/build?token=" + token;

				HttpPost httppost = new HttpPost(postUrl);
				jenkins.createSecurityHeader(httppost);

				try {
					HttpResponse response = httpclient.execute(httppost);
					logHttpResponse(response);

					AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
					audio.playSoundEffect(Sounds.SUCCESS);
				} catch (ClientProtocolException e) {
					Log.e(TAG, "A ClientProtocolException occured", e);
				} catch (IOException e) {
					Log.e(TAG, "An IOException occured", e);
				}
			}

			return true;
		}

        return false;
	}

    @Override
    public void onPause() {
        super.onPause();
        mView.deactivate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mView.activate();
    }

	@Override
	public boolean onScroll(float displacement, float delta, float velocity) {
		// TODO Auto-generated method stub
		return false;
	}

	private void logHttpResponse(HttpResponse response) {
		try {
			InputStream in = response.getEntity().getContent();
			InputStreamReader is = new InputStreamReader(in);
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(is);
			String read = br.readLine();
	
			while (read != null) {
				sb.append(read);
				read = br.readLine();
			}

			Log.d(TAG, "Status: " + response.getStatusLine().getStatusCode() + " # Entity: " + sb.toString());
		} catch(IOException e) {
			Log.e(TAG, "An IOException occured", e);
		}
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().bindService(new Intent(this, JenkinsService.class), mConnection, 0);
        
        mView = new CardScrollView(ViewJobDetailsActivity.this) {
            @Override
            public final boolean dispatchGenericFocusedEvent(MotionEvent event) {
                if (mDetector.onMotionEvent(event)) {
                    return true;
                }

                return super.dispatchGenericFocusedEvent(event);
            }
        };
    }
}