package org.dev.android.lin.schedulegenerator;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Mike on 12/6/2015.
 */
public class Course {

    public static final byte DAY_MASK = 0b01111111;
    public static final byte MON = 0b00000001;
    public static final byte TUE = 0b00000010;
    public static final byte WED = 0b00000100;
    public static final byte THU = 0b00001000;
    public static final byte FRI = 0b00010000;
    public static final byte SAT = 0b00100000;
    public static final byte SUN = 0b01000000;
    public static final byte DAY_NON = 0b00000000;
    public static final byte[] WEEK_DAYS = {MON, TUE, WED, THU, FRI, SAT, SUN};
    public static final String[] DayDescript = {"Mo","Tu","We","Th","Fr","Sa","Su"};
    public static boolean is24HourFormat = false;

    private final String name;
    public static final Comparator<Course> comparator = new Comparator<Course>() {
        @Override
        public int compare(Course lhs, Course rhs) {
            return lhs.name.compareTo(rhs.name);
        }
    };
    private final float credit;
    private final boolean isPrimary;
    private List<Section> sectionList = new ArrayList<>();

    public Course(@NonNull String name, float credit, boolean isPrimary) {
        this.credit = credit;
        this.isPrimary = isPrimary;
        this.name = name.toUpperCase();
    }


    public Course(@NonNull String name, float credit, boolean isPrimary,
                  int sectionNumber, int startTime, int endTime, String instructor,
                  String location, byte daysOfWeek) {
        this.credit = credit;
        this.isPrimary = isPrimary;
        this.name = name.toUpperCase();
        sectionList.add(new Section(sectionNumber, startTime, endTime, instructor, location, daysOfWeek));
    }

    private static String getTimeAsText(int timeInMinutes) {
        String timeText = "";
        int hour = timeInMinutes / 60;
        int minute = timeInMinutes - (hour * 60);
        if (is24HourFormat) {
            timeText = ((hour < 10) ? "0" + hour : Integer.toString(hour)) + ":" +
                    ((minute < 10) ? "0" + minute : Integer.toString(minute));
        } else {
            int nHour = (hour == 0 || hour == 12) ? 12 : (hour % 12);
            timeText = (nHour < 10 ? "0" + nHour : Integer.toString(nHour)) + ":" +
                    ((minute < 10) ? "0" + minute : Integer.toString(minute)) +
                    ((hour / 12) > 0 ? " PM" : " AM");
        }
        return timeText;
    }

    public void addSection(Section section) {
        sectionList.add(section);
    }

    public void addSection(int sectionNumber, int startTime, int endTime, String instructor,
                           String location, byte daysOfWeek) {
        sectionList.add(new Section(sectionNumber, startTime, endTime, instructor, location, daysOfWeek));
    }

    public int size() {
        return sectionList.size();
    }

    public List<Section> getSectionList() {
        return Collections.unmodifiableList(sectionList);
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public String getName() {
        return name;
    }

    public float getCredit() {
        return credit;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Course))
            return false;
        if (obj == this)
            return true;
        Course rhs = (Course) obj;

        return this.name.equals(rhs.name);
    }

    /* check if a given byte is a valid dayOfWeek byte
    * where at least one of the bits 0 to 6 are true and
    */
    public class Section {
        protected final int sectionNumber;
        protected final int startTime; // in minutes of 24 hour clock 13:01 = 60*13 + 1
        protected final int endTime; // in minutes of 24 hour clock 13:01 = 60*13 + 1
        protected final byte daysOfWeekByte;
        protected final String instructor;
        protected final String location;
        protected boolean[] daysOfWeek = {false, false, false, false, false, false, false};

        private Section(int sectionNumber, int startTime, int endTime, String instructor,
                        String location, byte daysOfWeek) {
            this.sectionNumber = sectionNumber;
            this.startTime = startTime;
            this.endTime = endTime;
            this.instructor = instructor;
            this.location = location;
            daysOfWeekByte = (byte) (daysOfWeek & DAY_MASK);
            for (int i = 0; i < WEEK_DAYS.length; i++) {
                if ((daysOfWeek & WEEK_DAYS[i]) == WEEK_DAYS[i])
                    this.daysOfWeek[i] = true;
            }
        }

        public boolean overlaps(Section rhs) {
            // compare of two classes occur on the same day, if so check for time overlap
            if ( ((this.daysOfWeekByte & rhs.daysOfWeekByte) != DAY_NON) &&
                ((this.startTime <= rhs.endTime) && (this.endTime >= rhs.startTime)) )
                    return true;

            /*for (int i = 0; i < daysOfWeek.length; i++) {
                if ( (this.daysOfWeek[i] && rhs.daysOfWeek[i]) &&
                        ( (this.startTime <= rhs.endTime) && (this.endTime >= rhs.startTime) ) )
                    return true;
            }*/
            return false;
        }

        public boolean isPrimary() {
            return isPrimary;
        }

        public String getCourseName() {
            return name;
        }

        public float getCredit() {
            return credit;
        }

        public int getSectionNumber() {
            return sectionNumber;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getEndTime() {
            return endTime;
        }

        public int getDaysOfWeekByte() {
            return daysOfWeekByte;
        }

        public boolean[] getDaysOfWeekBoolean() {
            return daysOfWeek;
        }

        public String getInstructor() {
            return instructor;
        }

        public String getLocation() {
            return location;
        }

        public String getDaysOfWeekAsText() {
            String dayOfWeekText = "";
            for (int i = 0; i < 7; i++) {
                if (daysOfWeek[i])
                    dayOfWeekText += DayDescript[i];
            }
            return dayOfWeekText;
        }

        public String getCompleteCourseName() {
            return (name + "-" + sectionNumber);
        }

        public String getDescription() {
            String result = getDaysOfWeekAsText() + "\nFrom: " +
                    getTimeAsText(startTime) + "-" +
                    getTimeAsText(endTime);

            if (instructor != null && !instructor.isEmpty()) {
                result += "\nWith: " + instructor;
                if (location != null && !location.isEmpty()) {
                    result += "  Room: " + location;
                }
            } else if (location != null && !location.isEmpty()) {
                result += "\nRoom: " + location;
            }

            return result;
        }

    }


/*
    private String name;
    private float credit;
    private String priority;
    private Context context;

    public List<Section> sections = new ArrayList<>();

    public Course(@NonNull String name) {
        this.name = name.toUpperCase();
        credit = 0.0f;
        priority = null;
    }

    public class Section {
        private int sec_num;
        private int startTime, endTime;
        private char[] daysOfWeek = {0,0,0,0,0,0,0};
        private String instructor;
        private String location;

        public Section(int sec_num, int startTime, int endTIme, char[] daysOfWeek, String instructor, String location) {
            super();
            if (daysOfWeek.length != 7) {
                throw new IllegalArgumentException("Invalid daysOfWeek array length!");
            }
            this.sec_num = sec_num;
            this.startTime = startTime;
            this.endTime = endTIme;
            this.daysOfWeek = daysOfWeek;
            this.instructor = instructor;
            this.location = location;
        }

        public Integer getSecNum() {
            return sec_num;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getEndTime() {
            return endTime;
        }

        public char[] getDaysOfWeekBoolean() {
            return daysOfWeek;
        }

        public String getInstructor() {
            return instructor;
        }

        public String getLocation() {
            return location;
        }

        public String minuteTimeToText(int timeInMinutes) {
            String text = "";
            int hour = timeInMinutes / 60;
            int minute = timeInMinutes - (hour * 60);
            if (DateFormat.is24HourFormat(context)) {
                text = ((hour < 10) ? "0" + hour : Integer.toString(hour)) + ":" +
                        ((minute < 10) ? "0" + minute : Integer.toString(minute));
            } else {
                int nHour = (hour == 0 || hour == 12)? 12 : (hour % 12);
                text = (nHour < 10 ? "0" + nHour : Integer.toString(nHour))+ ":" +
                        ((minute < 10) ? "0" + minute : Integer.toString(minute)) +
                        ((hour / 12) > 0 ? " PM" : " AM" );
            }
            return text;
        }

        public String getDescription() {
            String result = getDaysOfWeekAsText() + "\nFrom: " +
                    minuteTimeToText(startTime) + "-" +
                    minuteTimeToText(endTime);

            if (instructor != null && !instructor.isEmpty()) {
                result += "\nWith: " + instructor;
                if (location != null && !location.isEmpty()) {
                    result += "  Room: " + location;
                }
            } else if (location != null && !location.isEmpty()) {
                result += "\nRoom: " + location;
            }

            return result;
        }


        public String getDaysOfWeekAsText() {
            String days = "";
            if (daysOfWeek[0] != 0) {
                days += "M";
            }
            if (daysOfWeek[1] != 0) {
                if (days.length() > 0)
                    days += ",";
                days += "Tu";
            }
            if (daysOfWeek[2] != 0) {
                if (days.length() > 0)
                    days += ",";
                days += "W";
            }
            if (daysOfWeek[3] != 0) {
                if (days.length() > 0)
                    days += ",";
                days += "Th";
            }

            if (daysOfWeek[4] != 0) {
                if (days.length() > 0)
                    days += ",";
                days += "F";
            }
            if (daysOfWeek[5] != 0) {
                if (days.length() > 0)
                    days += ",";
                days += "Sat";
            }
            if (daysOfWeek[6] != 0) {
                if (days.length() > 0)
                    days += ",";
                days += "Sun";
            }

            return days;
        }
    }

    public Course(@NonNull String name, float credit, @NonNull String priority, Context context) {
        super();
        this.name = name.toUpperCase();
        this.credit = credit;
        this.priority = priority;
        this.context = context;
    }

    public Course(@NonNull String course, float credit, @NonNull String priority,
                  int sectionNumber, int startTime, int endTime, @NonNull char[] classDays, String instructor,
                  String location, Context context) {

        name = course.toUpperCase();
        this.credit = credit;
        this.priority = priority;
        this.context = context;
        addSection(sectionNumber,
                startTime,
                endTime,
                classDays,
                instructor,
                location);
    }

    public void addSection(int sec_num, int startTime, int endTIme, char[] daysOfWeek,
                           String instructor, String location) {
        sections.add(new Section(sec_num, startTime, endTIme, daysOfWeek, instructor, location));
    }

    public void addSection(Section section) {
        sections.add(section);
    }

    public String getAllCourseNames() {
        return name;
    }

    public Float getCredit() {
        return credit;
    }

    public String getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof Course) {
            Course courses = (Course)obj;
            return courses.name.toUpperCase().equals(this.name.toUpperCase());
        }
        return false;
    }*/
}

