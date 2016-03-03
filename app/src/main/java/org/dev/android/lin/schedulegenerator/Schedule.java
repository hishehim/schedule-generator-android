package org.dev.android.lin.schedulegenerator;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Class used to hold information on scheduleList.
 */
public class Schedule {

    protected static Requirements requirements = new Requirements();
    protected Schedule baseSchedule = null;
    protected List<Course.Section> classList = new ArrayList<>();
    protected ScheduleMetaData metaData = new ScheduleMetaData();
    protected int maxGapBetweenClass;

    public static
    @NonNull
    Schedule getNewSchedule(@NonNull Course.Section section) {
        Schedule schedule = new Schedule();

        schedule.addClass(section);
        return schedule;
    }

    public static
    @NonNull
    Schedule getNewSchedule(@NonNull Schedule base, @NonNull Course.Section section) {

        Schedule schedule = new Schedule();

        if (base.isValid())
            schedule.baseSchedule = base;

        schedule.metaData = base.metaData.clone();
        //schedule.maxGapBetweenClass = base.maxGapBetweenClass;
        schedule.addClass(section);

        return schedule;
    }

    /**
     * @return If <code>a</code> can merge with <code>b</code>, a new schedule is return with all the courses merged
     * Otherwise, an invalid schedule will be returned.
     */
    public static
    @NonNull
    Schedule merge(@NonNull Schedule a,
                   @NonNull Schedule b) {

        ScheduleMetaData combinedMetaData = ScheduleMetaData.merge(a.metaData, b.metaData);
        if (!combinedMetaData.validate(Schedule.requirements))
            return new Schedule();

        Schedule schedule = a.clone();

        Schedule mergeFromSchedule = b;
        while (mergeFromSchedule != null) {
            for (Course.Section section : mergeFromSchedule.classList) {
                schedule.addClass(section);
                if (!schedule.isValid()) {
                    return new Schedule();
                }
            }
            mergeFromSchedule = mergeFromSchedule.baseSchedule;
        }

        return schedule;
    }

    /**
     * Adds a class to the schedule. A class will only be added if the resulting schedule
     * compiles with the set requirement.
     * <p/>
     * The "correctness" of the schedule is preserved.
     *
     * @param section The section to be added to the schedule
     * @return <code>true</code> if section was successfully added
     * <code>false</code> if section could not be added to do conflict
     */
    public boolean addClass(@NonNull Course.Section section) {
        /*
        * validity metadata change
        * */
        if ((metaData.totalClasses >= requirements.maxNumberOfClasses) ||
                ((metaData.totalCredits + section.getCredit()) > requirements.maxCredits))
            return false;

        int projectedDaysOfClasses = metaData.totalDaysOfClasses;
        boolean[] projectClassDays = new boolean[7];

        for (int i = 0; i < projectClassDays.length; i++) {
            if (section.daysOfWeek[i]) {
                if (!metaData.daysOfClasses[i]) {
                    projectedDaysOfClasses++;
                    if (projectedDaysOfClasses > requirements.maxNumberOfDays)
                        return false;
                }
                projectClassDays[i] = true;
            } else {
                projectClassDays[i] = metaData.daysOfClasses[i];
            }
        }

        Schedule currentSchedule = this;
        while (currentSchedule != null) {
            for (Course.Section mSection : currentSchedule.classList) {
                if (mSection.overlaps(section)) {
                    return false;
                }
            }
            currentSchedule = currentSchedule.baseSchedule;
        }

        this.classList.add(section);
        this.metaData.totalCredits += section.getCredit();
        this.metaData.totalClasses++;
        this.metaData.totalDaysOfClasses = projectedDaysOfClasses;
        this.metaData.daysOfClasses = projectClassDays;
        return true;
    }

    public float getCredits() {
        return metaData.totalCredits;
    }

    public int getClassCount() {
        return metaData.totalClasses;
    }

    public ScheduleMetaData getMetaData() {
        return metaData;
    }

    public
    @NonNull
    Schedule getCompleteSchedule() {
        Schedule schedule = this.clone();

        Schedule parent = baseSchedule;
        while (parent != null) {
            if (!parent.classList.isEmpty())
                schedule.classList.addAll(parent.classList);
            parent = parent.baseSchedule;
        }
        return schedule;
    }

    /**
     * Indicate if schedule is in a valid state. A valid state being this schedule is not
     * an empty schedule that references another schedule.
     *
     * @return <code>true</code> if schedule is in a valid state
     * <code>false</code> if otherwise
     */
    public boolean isValid() {
        return !classList.isEmpty();
    }

    /**
     * Indicate if schedule is in a correct state. A correct state being this schedule is
     * valid and compiles to the set requirement.
     *
     * @return <code>true</code> if schedule is in a valid state
     * <code>false</code> if otherwise
     */
    public boolean isCorrect() {
        if (!isValid() || !metaData.validate(requirements)) {
            return false;
        }

        class TimeRange {
            int start_time;
            int end_time;

            public TimeRange(int s, int e) {
                start_time = s;
                end_time = e;
            }
        }

        List<TimeRange> classTime = new ArrayList<>();
        Schedule currentSchedule = this;
        while (currentSchedule != null) {
            for (Course.Section section : currentSchedule.classList) {
                for (int i = 0; i < section.daysOfWeek.length; i++) {
                    if (section.daysOfWeek[i]) {
                        classTime.add(new TimeRange(i * 10000 + section.startTime,
                                i * 10000 + section.endTime));
                    }
                }
            }
            currentSchedule = currentSchedule.baseSchedule;
        }

        Collections.sort(classTime, new Comparator<TimeRange>() {
            @Override
            public int compare(TimeRange lhs, TimeRange rhs) {
                if (lhs.start_time == rhs.start_time)
                    return 0;
                if (lhs.start_time < rhs.start_time)
                    return -1;
                return 1;
            }
        });

        TimeRange prev = classTime.get(0);
        for (int i = 1; i < classTime.size(); i++) {
            TimeRange cur = classTime.get(i);
            int tempGap = (cur.start_time - prev.end_time);
            if ((tempGap < 10000) && (tempGap > maxGapBetweenClass)) {
                if (tempGap > requirements.maxGap)
                    return false;
                maxGapBetweenClass = tempGap;
            }
            prev = cur;
        }
        return true;
    }

    public
    @NonNull
    Schedule clone() {
        Schedule schedule = new Schedule();
        schedule.baseSchedule = this.baseSchedule;
        schedule.classList.addAll(this.classList);
        schedule.metaData = this.metaData.clone();
        schedule.maxGapBetweenClass = this.maxGapBetweenClass;
        return schedule;
    }

    public static class ScheduleMetaData {
        private float totalCredits = 0.0f;
        private int totalClasses = 0;
        private int totalDaysOfClasses = 0;
        private boolean[] daysOfClasses = {false, false, false, false, false, false, false};

        public ScheduleMetaData() {
        }

        public static ScheduleMetaData merge(ScheduleMetaData a, ScheduleMetaData b) {
            ScheduleMetaData metaData = new ScheduleMetaData();
            metaData.totalCredits = a.totalCredits + b.totalCredits;
            metaData.totalClasses = a.totalClasses + b.totalClasses;
            for (int i = 0; i < a.daysOfClasses.length; i++) {
                metaData.daysOfClasses[i] = (a.daysOfClasses[i] || b.daysOfClasses[i]);
                if (metaData.daysOfClasses[i])
                    metaData.totalDaysOfClasses += 1;
            }
            return metaData;
        }

        public boolean validate(Requirements requirements) {
            return ((totalCredits >= requirements.minCredits) &&
                    (totalCredits <= requirements.maxCredits) &&
                    (totalClasses <= requirements.maxNumberOfClasses) &&
                    (totalDaysOfClasses <= requirements.maxNumberOfDays));
        }

        @Override
        public ScheduleMetaData clone() {
            ScheduleMetaData metaData = new ScheduleMetaData();
            metaData.totalCredits = this.totalCredits;
            metaData.totalClasses = this.totalClasses;
            metaData.totalDaysOfClasses = this.totalDaysOfClasses;
            metaData.daysOfClasses = this.daysOfClasses.clone();
            return metaData;
        }
    }

    public static class Requirements {
        public float maxCredits = 0f;
        public float minCredits = 0f;
        public int maxGap = 0;
        public int maxNumberOfDays = 0;
        public int maxNumberOfClasses = 0;

        public Requirements() {
        }

        public Requirements(float maxCredits, float minCredits, int maxGap,
                            int maxNumberOfDays, int maxNumberOfClasses) {
            this.maxCredits = maxCredits;
            this.minCredits = minCredits;
            this.maxGap = maxGap;
            this.maxNumberOfDays = maxNumberOfDays;
            this.maxNumberOfClasses = maxNumberOfClasses;
        }

        @Override
        public Requirements clone() {
            Requirements requirements = new Requirements();
            requirements.maxCredits = this.maxCredits;
            requirements.minCredits = this.minCredits;
            requirements.maxGap = this.maxGap;
            requirements.maxNumberOfDays = this.maxNumberOfDays;
            requirements.maxNumberOfClasses = this.maxNumberOfClasses;
            return requirements;
        }
    }

    public @NonNull String getDaysOfWeekText() {
        String daysText = "";
        for(int i = 0; i < metaData.daysOfClasses.length; i++) {
            if (metaData.daysOfClasses[i])
                daysText += Course.DayDescript[i];
        }
        return daysText;
    }

    public float getTotalCredit() {
        return metaData.totalCredits;
    }

    public int getMaxGap() {
        return maxGapBetweenClass;
    }

    public List<Course.Section> getClassList() {
        return Collections.unmodifiableList(classList);
    }

    public static void setRequirements(Requirements requirements) {
        Schedule.requirements = requirements.clone();
    }
}
