package org.dev.android.lin.schedulegenerator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike on 12/2/2015.
 */
public class GeneratorActivity extends AppCompatActivity {

    private final List<Schedule> scheduleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_viewschedule);

        int maxBreak, maxDays, maxClasses;
        float minCredit, maxCredit;

        SharedPreferences settings =
                getApplication().getSharedPreferences(MainActivity.PREFS_NAME, 0);

        maxBreak = settings.getInt(MainActivity.KEY_BREAK, 1);
        maxDays = settings.getInt(MainActivity.KEY_DAYS, 1);
        maxClasses = settings.getInt(MainActivity.KEY_CLASSES, 1);
        minCredit = settings.getInt(MainActivity.KEY_MIN_CREDIT, 1);
        maxCredit = settings.getInt(MainActivity.KEY_MAX_CREDIT, 1);
        

        ClassListDB db = new ClassListDB(getApplication());
        db.open();

        List<Course> p_courses = db.getEssentialCoursesList();
        List<Course> s_courses = db.getSecondaryCoursesList();

        db.close();

        Schedule.Requirements requirements = new Schedule.Requirements(
                maxCredit, // max credit
                minCredit, // min credit
                maxBreak * 60, // max gap
                maxDays, // max number of class days
                maxClasses); // max number of classes
        Schedule.setRequirements(requirements);

        List<Schedule> primaryBase = generatePrimarySchedules(p_courses);

        if ( (p_courses.size() > 0) && primaryBase.isEmpty()) {
            Toast.makeText(this,
                    "Could not find valid set of classes containing all primary courses!",
                    Toast.LENGTH_SHORT).show();
            System.out.println("Could not find valid set of classes containing all primary courses!");
            return;
        }

        int remainingClassCount = requirements.maxNumberOfClasses - p_courses.size();
        float remainingCredits = (primaryBase.isEmpty()) ? requirements.maxCredits :
                requirements.maxCredits - primaryBase.get(0).getCredits();
        List<Schedule> secondaryBase = generateSecondarySchedule(primaryBase,
                s_courses,
                remainingClassCount,
                remainingCredits);

        for (Schedule primarySchedule: primaryBase) {
            if (primarySchedule.isCorrect()) {
                scheduleList.add(primarySchedule.getCompleteSchedule());
            }
            for (Schedule secondarySchedule: secondaryBase) {
                Schedule holderSchedule = Schedule.merge(primarySchedule, secondarySchedule);
                if (holderSchedule.isCorrect())
                    scheduleList.add(holderSchedule);
            }
        }


        if (primaryBase.isEmpty()) {
            for (Schedule schedule: secondaryBase) {
                if (schedule.isCorrect()) {
                    scheduleList.add(schedule);
                }
            }
        }
        ScheduleAdapter scheduleAdapter = new ScheduleAdapter(this, R.layout.schedule_layout, scheduleList);

        ListView listView = (ListView) findViewById(R.id.schedule_list);
        listView.setAdapter(scheduleAdapter);
    }

    private abstract static class abstractGenerator {
        protected List<Course> courseList = new ArrayList<>();
        protected List<Schedule> validSchedules = new ArrayList<>();

        abstract List<Schedule> generate(Schedule baseSchedule, List<Course> courseList);
    }


    private List<Schedule> generatePrimarySchedules(@NonNull List<Course> primaryCourses) {

        abstractGenerator primaryGenerator = new abstractGenerator() {

            @Override
            public List<Schedule> generate(@NonNull Schedule baseSchedule, @NonNull List<Course> courseList) {
                if (courseList.isEmpty())
                    return validSchedules;

                this.courseList = courseList;

                for (Course.Section section: courseList.get(0).getSectionList()) {
                    generateRecursive(Schedule.getNewSchedule(section), 1);
                }

                return validSchedules;
            }

            void generateRecursive(Schedule baseSchedule, int index) {
                if (index >= courseList.size()) {
                    validSchedules.add(baseSchedule);
                    return;
                }

                for (Course.Section section: courseList.get(index).getSectionList()) {
                    Schedule schedule = Schedule.getNewSchedule(baseSchedule, section);
                    if (schedule.isValid())
                        generateRecursive(schedule, index + 1);
                }

            }
        };

        return primaryGenerator.generate(null, primaryCourses);
    }


    private List<Schedule> generateSecondarySchedule(@NonNull List<Schedule> baseSchedule,
                                                            @NonNull List<Course> secondaryCourses,
                                                            final int max_class,
                                                            final float max_credit) {

        abstractGenerator generator = new abstractGenerator() {

            @Override
            public List<Schedule> generate(Schedule baseSchedules, @NonNull List<Course> courseList) {

                this.courseList = courseList;

                powerSet(this.courseList, 0);

                return validSchedules;
            }

            private List<Schedule> powerSet(List<Course> courseList, int index) {
                if (index >= courseList.size()) {
                    List<Schedule> emptySet = new ArrayList<>();
                    emptySet.add(new Schedule());
                    return emptySet;
                }

                List<Schedule> scheduleList = new ArrayList<>();
                Course head = courseList.get(index);

                for (Schedule schedule: powerSet(courseList, index+1)) {
                    scheduleList.add(schedule);
                    // add new schedule to pool a new valid schedule is added
                    for (Course.Section section: head.getSectionList()) {
                        Schedule tmpSchedule = Schedule.getNewSchedule(schedule, section);
                        if (tmpSchedule.isValid()) {
                            scheduleList.add(tmpSchedule);
                            if ( (tmpSchedule.getCredits() <= max_credit) &&
                                    (tmpSchedule.getClassCount() <= max_class) )
                                validSchedules.add(tmpSchedule);
                        }
                    }
                }
                return scheduleList;
            }
        };

        return generator.generate(null, secondaryCourses);
    }

}
