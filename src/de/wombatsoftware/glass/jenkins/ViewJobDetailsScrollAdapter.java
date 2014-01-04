package de.wombatsoftware.glass.jenkins;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.glass.widget.CardScrollAdapter;

import de.wombatsoftware.glass.jenkins.model.Jenkins;
import de.wombatsoftware.glass.jenkins.model.Job;

public class ViewJobDetailsScrollAdapter extends CardScrollAdapter {
	private Jenkins jenkins;
	private final Context mContext;
	
	public ViewJobDetailsScrollAdapter(Context context, Jenkins jenkins) {
        mContext = context;
        this.jenkins = jenkins;
    }

	@Override
	public int findIdPosition(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int findItemPosition(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCount() {
		return jenkins.getJobs().size();
	}

	@Override
    public Object getItem(int position) {
		List<Job> jobs = jenkins.getJobs();

        if (position >= 0 && position < jobs.size()) {
            return jobs.get(position);
        }

        return null;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.card_job_details, parent);
        }

        final TextView[] views = new TextView[] {
            (TextView) convertView.findViewById(R.id.name)
        };

        Job job = jenkins.getJobs().get(position);
        views[0].setText(job.getName());
        
        int color = R.color.red;
        
        switch (job.getColor()) {
			case blue:
				color = R.color.blue;
				break;
	
			case yellow:
				color = R.color.yellow;
				break;
	
			default:
				break;
		}
        
        views[0].setTextColor(mContext.getResources().getColor(color));

        return setItemOnCard(this, convertView);
	}
}