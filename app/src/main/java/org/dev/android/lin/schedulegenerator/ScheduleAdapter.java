package org.dev.android.lin.schedulegenerator;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike on 12/11/2015.
 */
public class ScheduleAdapter extends ArrayAdapter<Schedule> {

    int layoutID;
    Context context;
    List<Schedule> scheduleList = new ArrayList<>();


    public ScheduleAdapter(Context context, int resource, List<Schedule> list) {
        super(context, resource, list);
        this.context = context;
        this.layoutID = resource;
        this.scheduleList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder;

        if(row == null) {
            LayoutInflater inflater = ((AppCompatActivity) context).getLayoutInflater();
            row = inflater.inflate(layoutID, parent, false);
            holder = new Holder();

            holder.tvClassDays = (TextView)row.findViewById(R.id.days_of_classes);
            holder.tvMaxGap = (TextView)row.findViewById(R.id.max_break_time);
            holder.tvTotalCredit= (TextView)row.findViewById(R.id.total_credit);
            holder.classList = (LinearLayout)row.findViewById(R.id.class_list);

            row.setTag(holder);
        } else {
            holder = (Holder)row.getTag();
        }

        Schedule schedule = scheduleList.get(position);

        holder.tvClassDays.setText(schedule.getDaysOfWeekText());
        holder.tvTotalCredit.setText(String.valueOf(schedule.getTotalCredit()));
        int maxGap = schedule.getMaxGap(),
                gapMinute = (maxGap % 60),
                gapHour = (maxGap / 60);
        String maxGapText = String.valueOf(gapHour) + "h " + gapMinute + "m";
        holder.tvMaxGap.setText(maxGapText);
        holder.classList.removeAllViews();
        for (Course.Section section: schedule.getClassList()) {
            View view = View.inflate(context, R.layout.simple_class_description, null);
            ((TextView)view.findViewById(R.id.class_id))
                    .setText(section.getCompleteCourseName());
            ((TextView)view.findViewById(R.id.class_credit))
                    .setText(String.valueOf(section.getCredit()));
            ((TextView)view.findViewById(R.id.class_days))
                    .setText(String.valueOf(section.getDaysOfWeekAsText()));
            holder.classList.addView(view);
        }
        return row;
    }

    static class Holder {
        TextView tvClassDays, tvTotalCredit, tvMaxGap;
        LinearLayout classList;
    }
}
