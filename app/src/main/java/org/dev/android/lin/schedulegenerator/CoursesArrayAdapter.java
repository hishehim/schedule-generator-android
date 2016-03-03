package org.dev.android.lin.schedulegenerator;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Mike on 12/6/2015.
 */
public class CoursesArrayAdapter extends ArrayAdapter<Course> {

    int layoutID;
    Context context;
    List<Course> coursesList;


    public CoursesArrayAdapter(Context context, int resource, List<Course> list) {
        super(context, resource, list);
        this.layoutID = resource;
        this.context = context;
        coursesList = list;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        View row = convertView;
        Holder holder;

        if (row == null) {
            LayoutInflater inflater = ((AppCompatActivity) context).getLayoutInflater();
            row = inflater.inflate(layoutID, parent, false);
            holder = new Holder();
            holder.courseTitle = (TextView) row.findViewById(R.id.course_title);
            holder.courseCredit = (TextView) row.findViewById(R.id.course_credit);
            holder.sectionList = (LinearLayout) row.findViewById(R.id.section_list);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        final Course course = coursesList.get(position);

        holder.courseTitle.setText(course.getName());
        holder.courseCredit.setText(String.valueOf(course.getCredit()));
        holder.sectionList.removeAllViews();
        View header = row.findViewById(R.id.course_display);

        header.setTag(course.getName());
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewParent viewParent = v.getParent();
                if (viewParent instanceof View) {
                    View sectionList = ((View) viewParent).findViewById(R.id.section_list);
                    if (sectionList != null) {
                        sectionList.setVisibility(sectionList.isShown() ? View.GONE : View.VISIBLE);
                    }
                }
            }
        });

        header.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final Object tag = v.getTag();
                        if (tag != null) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Delete Course?")
                                    .setMessage("All related sections will be deleted!")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MainActivity.deleteCourse(course, context);
                                            coursesList.remove(course);
                                            notifyDataSetChanged();
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();
                        }
                        return false;
                    }
                }
        );

        for (final Course.Section section : course.getSectionList()) {
            final View sectionLayout = View.inflate(context, R.layout.section_layout, null);
            final String sectionNumber = String.valueOf(section.getSectionNumber());
            ((TextView) sectionLayout.findViewById(R.id.sect_num))
                    .setText(sectionNumber);
            ((TextView) sectionLayout.findViewById(R.id.section_description))
                    .setText(section.getDescription());
            sectionLayout.setTag(sectionNumber);
            //sectionLayout.setTag(course.getName());

            sectionLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final View view = v;
                    final String courseName = section.getCourseName();
                    final int sectionNumber = section.getSectionNumber();

                    new AlertDialog.Builder(context)
                            .setTitle("Delete Section?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.deleteSection(section, context);
                                    ((ViewGroup) view.getParent()).removeView(view);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                    return false;
                }
            });
            holder.sectionList.addView(sectionLayout);

        }

        return row;
    }

    static class Holder {
        TextView courseTitle, courseCredit;
        LinearLayout sectionList;
    }

}
