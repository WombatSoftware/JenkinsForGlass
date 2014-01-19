package de.wombatsoftware.glass.jenkins;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

		ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
		TextView textView = (TextView) convertView.findViewById(R.id.name);

        Job currentJob = jenkins.getJobs().get(position);
        textView.setText(currentJob.getName());

        int color = R.color.red;
        int image = R.drawable.red;

        switch (currentJob.getColor()) {
			case blue:
				color = R.color.blue;
				image = R.drawable.blue;
				break;
	
			case yellow:
				color = R.color.yellow;
				image = R.drawable.yellow;
				break;
	
			default:
				break;
		}

        textView.setTextColor(mContext.getResources().getColor(color));
        imageView.setImageResource(image);

        return setItemOnCard(this, convertView);
	}
}