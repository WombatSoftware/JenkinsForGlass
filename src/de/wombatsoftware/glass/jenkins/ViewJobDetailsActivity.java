package de.wombatsoftware.glass.jenkins;

import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollView;

import de.wombatsoftware.glass.jenkins.model.Jenkins;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;

/**
 * Activity to set the timer.
 */
public class ViewJobDetailsActivity extends Activity implements GestureDetector.ScrollListener {

    public static final String EXTRA_JENKINS = "jenkins";

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

                mDetector = new GestureDetector(ViewJobDetailsActivity.this).setScrollListener(ViewJobDetailsActivity.this);
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

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mDetector.onMotionEvent(event);
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
}