package de.wombatsoftware.glass.jenkins;

import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollView;

import de.wombatsoftware.glass.jenkins.model.Jenkins;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

/**
 * Activity to set the timer.
 */
public class ViewJobDetailsActivity extends Activity implements GestureDetector.ScrollListener {

    public static final String EXTRA_JENKINS = "jenkins";

    private GestureDetector mDetector;
    private CardScrollView mView;
    private ViewJobDetailsScrollAdapter mAdapter;
    
    private Jenkins jenkins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        jenkins = (Jenkins) getIntent().getSerializableExtra(EXTRA_JENKINS);

        mAdapter = new ViewJobDetailsScrollAdapter(this, jenkins);

        mView = new CardScrollView(this) {
            @Override
            public final boolean dispatchGenericFocusedEvent(MotionEvent event) {
                if (mDetector.onMotionEvent(event)) {
                    return true;
                }
                return super.dispatchGenericFocusedEvent(event);
            }
        };
        mView.setAdapter(mAdapter);
        setContentView(mView);
        
        mDetector = new GestureDetector(this).setScrollListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mView.activate();
    }

    @Override
    public void onPause() {
        super.onPause();
        mView.deactivate();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mDetector.onMotionEvent(event);
    }

    @Override
    public void onBackPressed() {
        
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        
    }

	@Override
	public boolean onScroll(float displacement, float delta, float velocity) {
		// TODO Auto-generated method stub
		return false;
	}
}