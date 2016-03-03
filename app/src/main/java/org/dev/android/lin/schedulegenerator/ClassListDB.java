package org.dev.android.lin.schedulegenerator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Mike on 12/3/2015.
 */

public class ClassListDB {

    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;

    public ClassListDB(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    public ClassListDB open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    /* Return _ID if the course exist, otherwise create new course and return new _ID
    *  Return -1 if fails
    * */
    private long addCourse(ContentValues courseInfo) {
        //find course if exist.
        long courseId;
        Cursor cursor = db.query(CoursesTable.CourseEntry.TABLE_NAME,
                new String[]{CoursesTable.CourseEntry._ID},
                CoursesTable.CourseEntry.COLUMN_COURSE + "=?",
                new String[]{courseInfo.getAsString(CoursesTable.CourseEntry.COLUMN_COURSE)},
                null, null, null, null);

        if (cursor.moveToFirst()) {
            courseId = cursor.getLong(cursor.getColumnIndex(CoursesTable.CourseEntry._ID));
            Toast.makeText(context, "Course found: " + courseId, Toast.LENGTH_SHORT).show();
        } else {
            courseId = db.insertOrThrow(CoursesTable.CourseEntry.TABLE_NAME, null, courseInfo);
            Toast.makeText(context, "New course created: " + courseId, Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        return courseId;
    }

    /* Return Section _ID if exist, else create new Section and return _ID
    *  Return -1 if fails.
    * */
    private long addSection(ContentValues sectionInfo) {
        long sectionId = -1;
        long courseId = sectionInfo.getAsLong(SectionsTable.SectionEntry.COLUMN_COURSE_ID);
        int sectionNum = sectionInfo.getAsInteger(SectionsTable.SectionEntry.COLUMN_SECTION_NUM);

        Cursor cursor = db.query(SectionsTable.SectionEntry.TABLE_NAME,
                new String[]{SectionsTable.SectionEntry._ID},
                SectionsTable.SectionEntry.COLUMN_COURSE_ID + "=? AND " +
                        SectionsTable.SectionEntry.COLUMN_SECTION_NUM + "=?",
                new String[]{Long.toString(courseId), Integer.toString(sectionNum)},
                null, null, null, null);

        if (cursor.moveToFirst()) {
            Toast.makeText(context, "Found Section: " + cursor.getLong(0), Toast.LENGTH_SHORT).show();
        } else {
            sectionId = db.insertOrThrow(SectionsTable.SectionEntry.TABLE_NAME, null, sectionInfo);
            Toast.makeText(context, "Create Section: " + sectionId, Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        return sectionId;
    }

    public long addClass(@NonNull Course nCourse) {

        if (nCourse.getSectionList().isEmpty()) {
            return -1;
        }

        ContentValues courseInfo = new ContentValues();

        courseInfo.put(CoursesTable.CourseEntry.COLUMN_COURSE, nCourse.getName().toUpperCase());
        courseInfo.put(CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT, nCourse.getCredit());
        courseInfo.put(CoursesTable.CourseEntry.COLUMN_PRIORITY, nCourse.isPrimary());

        long courseId = addCourse(courseInfo);
        if (courseId >= 0) {
            ContentValues sectionInfo = new ContentValues();
            for (Course.Section section : nCourse.getSectionList()) {
                sectionInfo.put(SectionsTable.SectionEntry.COLUMN_SECTION_NUM,
                        section.getSectionNumber());
                sectionInfo.put(SectionsTable.SectionEntry.COLUMN_DAYS,
                        section.getDaysOfWeekByte());
                sectionInfo.put(SectionsTable.SectionEntry.COLUMN_START_TIME,
                        section.getStartTime());
                sectionInfo.put(SectionsTable.SectionEntry.COLUMN_END_TIME,
                        section.getEndTime());
                sectionInfo.put(SectionsTable.SectionEntry.COLUMN_INSTRUCTOR,
                        section.getInstructor());
                sectionInfo.put(SectionsTable.SectionEntry.COLUMN_LOCATION,
                        section.getLocation());
                sectionInfo.put(SectionsTable.SectionEntry.COLUMN_COURSE_ID, courseId);
                if (addSection(sectionInfo) < 0) {
                    Toast.makeText(context,
                            "Section " + section.getSectionNumber() + " already exist",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        return courseId;
    }

    public int deleteCourse(Course course) {
        String whereClause = CoursesTable.CourseEntry.COLUMN_COURSE + "=?";
        String[] args = {course.getName().toUpperCase()};
        return db.delete(CoursesTable.CourseEntry.TABLE_NAME, whereClause, args);
    }

    public int deleteSection(Course.Section section) {
        Cursor cursor = db.query(CoursesTable.CourseEntry.TABLE_NAME,
                new String[]{CoursesTable.CourseEntry._ID},
                CoursesTable.CourseEntry.COLUMN_COURSE + "=?",
                new String[]{section.getCourseName()},
                null, null, null);

        if (cursor.moveToFirst()) {
            long courseId = cursor.getLong(0);
            cursor.close();
            return db.delete(SectionsTable.SectionEntry.TABLE_NAME,
                    SectionsTable.SectionEntry.COLUMN_COURSE_ID + "=? AND " +
                            SectionsTable.SectionEntry.COLUMN_SECTION_NUM + "=?",
                    new String[]{"" + courseId, "" + section.getSectionNumber()});
        } else {
            cursor.close();
        }
        return -2;
    }

    public String[] getAllCourseNames() {
        Cursor cursor = db.query(CoursesTable.CourseEntry.TABLE_NAME,
                new String[]{CoursesTable.CourseEntry.COLUMN_COURSE},
                null, null, null, null, null);

        String[] courseName = new String[cursor.getCount()];
        for (int i = 0; i < courseName.length && cursor.moveToNext(); i++) {
            courseName[i] = cursor.getString(0);
        }
        cursor.close();
        return courseName;
    }

    @Nullable
    public ContentValues getCourseCredit(String course) {
        Cursor cursor = db.query(CoursesTable.CourseEntry.TABLE_NAME,
                new String[]{CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT,
                        CoursesTable.CourseEntry.COLUMN_PRIORITY},
                CoursesTable.CourseEntry.COLUMN_COURSE + "=?",
                new String[]{course.toUpperCase()}, null, null, null, null);

        ContentValues contentValues = null;

        if (cursor.moveToFirst()) {
            contentValues = new ContentValues();
            contentValues.put(CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT, cursor.getFloat(0));
            contentValues.put(CoursesTable.CourseEntry.COLUMN_PRIORITY, cursor.getString(1));
        }
        cursor.close();
        return contentValues;
    }


    public List<Course> getEssentialCoursesList() {
        List<Course> courseList = new ArrayList<>();

        Cursor cursor = db.query(CoursesTable.CourseEntry.TABLE_NAME,
                new String[]{CoursesTable.CourseEntry.COLUMN_COURSE,
                        CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT,
                        CoursesTable.CourseEntry._ID},
                CoursesTable.CourseEntry.COLUMN_PRIORITY + "=?",
                new String[]{CoursesTable.IS_PRIMARY_S},
                null, null, CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT);

        while (cursor.moveToNext()) {
            String courseName = cursor.getString(0);
            float courseCredit = cursor.getFloat(1);
            Course course = new Course(courseName, courseCredit, true);
            getSectionByCourse(cursor.getLong(2), course);
            courseList.add(course);
        }

        cursor.close();

        return courseList;

    }

        public List<Course> getSecondaryCoursesList() {

            List<Course> courseList = new ArrayList<>();

            Cursor cursor = db.query(CoursesTable.CourseEntry.TABLE_NAME,
                    new String[]{CoursesTable.CourseEntry.COLUMN_COURSE,
                            CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT,
                            CoursesTable.CourseEntry._ID},
                    CoursesTable.CourseEntry.COLUMN_PRIORITY + "=?",
                    new String[]{CoursesTable.IS_SECONDARY_S},
                    null, null, CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT);

            while (cursor.moveToNext()) {
                String courseName = cursor.getString(0);
                float courseCredit = cursor.getFloat(1);
                Course course = new Course(courseName, courseCredit, true);
                getSectionByCourse(cursor.getLong(2), course);
                courseList.add(course);
            }

            cursor.close();

            return courseList;
        }

    public HashMap<String, Course> getEssentialCoursesHash() {

        HashMap<String, Course> coursesTable = new HashMap<>();

        Cursor cursor = db.query(CoursesTable.CourseEntry.TABLE_NAME,
                new String[]{CoursesTable.CourseEntry.COLUMN_COURSE,
                        CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT,
                        CoursesTable.CourseEntry._ID},
                CoursesTable.CourseEntry.COLUMN_PRIORITY + "=?",
                new String[]{CoursesTable.IS_PRIMARY_S},
                null, null, CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT);

        while (cursor.moveToNext()) {
            String courseName = cursor.getString(0);
            float courseCredit = cursor.getFloat(1);
            Course course = new Course(courseName, courseCredit, true);
            getSectionByCourse(cursor.getLong(2), course);
            coursesTable.put(courseName, course);
        }

        cursor.close();

        return coursesTable;
    }

    public HashMap<String, Course> getSecondaryCoursesHash() {
        HashMap<String, Course> coursesTable = new HashMap<>();

        Cursor cursor = db.query(CoursesTable.CourseEntry.TABLE_NAME,
                new String[]{CoursesTable.CourseEntry.COLUMN_COURSE,
                        CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT,
                        CoursesTable.CourseEntry._ID},
                CoursesTable.CourseEntry.COLUMN_PRIORITY + "=?",
                new String[]{CoursesTable.IS_SECONDARY_S},
                null, null, CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT);

        while (cursor.moveToNext()) {
            String courseName = cursor.getString(0);
            float courseCredit = cursor.getFloat(1);
            Course course = new Course(courseName, courseCredit, true);
            getSectionByCourse(cursor.getLong(2), course);
            coursesTable.put(courseName, course);
        }

        cursor.close();

        return coursesTable;
    }

    private void getSectionByCourse(long course_id, Course course) {
        Cursor cursor = db.query(SectionsTable.SectionEntry.TABLE_NAME,
                new String[]{SectionsTable.SectionEntry.COLUMN_SECTION_NUM,
                        SectionsTable.SectionEntry.COLUMN_START_TIME,
                        SectionsTable.SectionEntry.COLUMN_END_TIME,
                        SectionsTable.SectionEntry.COLUMN_INSTRUCTOR,
                        SectionsTable.SectionEntry.COLUMN_LOCATION,
                        SectionsTable.SectionEntry.COLUMN_DAYS},
                SectionsTable.SectionEntry.COLUMN_COURSE_ID + "=?",
                new String[]{Long.toString(course_id)}, null, null,
                SectionsTable.SectionEntry.COLUMN_SECTION_NUM);

        while (cursor.moveToNext()) {
            course.addSection(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),
                    cursor.getString(3), cursor.getString(4), (byte) cursor.getInt(5));
        }
        cursor.close();
    }

    public static final class CoursesTable {

        public static final int IS_PRIMARY = 0b1;
        public static final int IS_SECONDARY = 0b0;
        public static final String IS_PRIMARY_S = Integer.toString(IS_PRIMARY);
        public static final String IS_SECONDARY_S = Integer.toString(IS_SECONDARY);

        public CoursesTable() {
        }

        public static abstract class CourseEntry implements BaseColumns {
            public static final String TABLE_NAME = "courses";
            public static final String COLUMN_COURSE = "course";
            public static final String COLUMN_DEP_CODE_TYPE = " TEXT";
            public static final String COLUMN_COURSE_CREDIT = "credit";
            public static final String COLUMN_COURSE_CREDIT_TYPE = " REAL";
            public static final String COLUMN_PRIORITY = "priority";
            public static final String COLUMN_PRIORITY_TYPE = " SMALLINT";
            public static final String UNIQUE_CONSTRAINT = "UNIQUE (" +
                    COLUMN_COURSE + ")";
        }
    }

    public static final class SectionsTable {

        public SectionsTable() {
        }

        public static abstract class SectionEntry implements BaseColumns {

            public static final String TABLE_NAME = "Section";
            public static final String COLUMN_COURSE_ID = "course_id";
            public static final String COLUMN_COURSE_ID_TYPE = " INTEGER";
            public static final String COLUMN_SECTION_NUM = "section_num";
            public static final String COLUMN_SECTION_NUM_TYPE = " INTEGER";
            public static final String COLUMN_INSTRUCTOR = "instructor";
            public static final String COLUMN_INSTRUCTOR_TYPE = " TEXT";
            public static final String COLUMN_LOCATION = "location";
            public static final String COLUMN_LOCATION_TYPE = " TEXT";
            public static final String COLUMN_DAYS = "days_of_week";
            public static final String COLUMN_DAYS_TYPE = " SMALLINT";
            public static final String COLUMN_START_TIME = "start_hour";
            public static final String COLUMN_END_TIME = "end_hour";
            public static final String COLUMN_TIME_TYPE = " INTEGER";
            public static final String UNIQUE_CONSTRAINT =
                    "UNIQUE (" + COLUMN_COURSE_ID + ", " +
                            COLUMN_SECTION_NUM + ")";
            public static final String CONSTRAINT_FK_COURSE_ID =
                    "CONSTRAINT fk_course_id FOREIGN KEY (" + COLUMN_COURSE_ID +
                            ") REFERENCES " + CoursesTable.CourseEntry.TABLE_NAME + "(" +
                            CoursesTable.CourseEntry._ID + ") ON DELETE CASCADE";
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 3;
        public static final String DATABASE_NAME = "classlist";
        private static final String SQL_CREATE_COURSE_TABLE =
                "CREATE TABLE " + CoursesTable.CourseEntry.TABLE_NAME + " (" +
                        CoursesTable.CourseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        CoursesTable.CourseEntry.COLUMN_COURSE +
                        CoursesTable.CourseEntry.COLUMN_DEP_CODE_TYPE + "," +
                        CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT +
                        CoursesTable.CourseEntry.COLUMN_COURSE_CREDIT_TYPE + " NOT NULL," +
                        CoursesTable.CourseEntry.COLUMN_PRIORITY +
                        CoursesTable.CourseEntry.COLUMN_PRIORITY_TYPE + " NOT NULL," +
                        CoursesTable.CourseEntry.UNIQUE_CONSTRAINT +
                        ")";
        private static final String SQL_CREATE_SECTION_TABLE =
                "CREATE TABLE " + SectionsTable.SectionEntry.TABLE_NAME + " (" +
                        SectionsTable.SectionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        SectionsTable.SectionEntry.COLUMN_COURSE_ID +
                        SectionsTable.SectionEntry.COLUMN_COURSE_ID_TYPE + " NOT NULL," +
                        SectionsTable.SectionEntry.COLUMN_SECTION_NUM +
                        SectionsTable.SectionEntry.COLUMN_SECTION_NUM_TYPE + " NOT NULL," +
                        SectionsTable.SectionEntry.COLUMN_INSTRUCTOR +
                        SectionsTable.SectionEntry.COLUMN_INSTRUCTOR_TYPE + " DEFAULT NULL," +
                        SectionsTable.SectionEntry.COLUMN_LOCATION +
                        SectionsTable.SectionEntry.COLUMN_LOCATION_TYPE + " DEFAULT NULL," +
                        SectionsTable.SectionEntry.COLUMN_DAYS +
                        SectionsTable.SectionEntry.COLUMN_DAYS_TYPE + " NOT NULL," +
                        SectionsTable.SectionEntry.COLUMN_START_TIME +
                        SectionsTable.SectionEntry.COLUMN_TIME_TYPE + " NOT NULL," +
                        SectionsTable.SectionEntry.COLUMN_END_TIME +
                        SectionsTable.SectionEntry.COLUMN_TIME_TYPE + " NOT NULL," +
                        SectionsTable.SectionEntry.UNIQUE_CONSTRAINT + "," +
                        SectionsTable.SectionEntry.CONSTRAINT_FK_COURSE_ID +
                        ")";
        private static final String SQL_DROP_COURSE_TABLE =
                "DROP TABLE IF EXISTS " + CoursesTable.CourseEntry.TABLE_NAME;
        //+ " CASCADE CONSTRAINTS";
        private static final String SQL_DROP_SECTION_TABLE =
                "DROP TABLE IF EXISTS " + SectionsTable.SectionEntry.TABLE_NAME;
        private static final String SQL_CREATE_TABLE =
                SQL_CREATE_COURSE_TABLE + ";" + SQL_CREATE_SECTION_TABLE;
        private static final String SQL_DROP_TABLE =
                SQL_DROP_SECTION_TABLE + ";" + SQL_DROP_COURSE_TABLE;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(SQL_CREATE_COURSE_TABLE);
                db.execSQL(SQL_CREATE_SECTION_TABLE);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            db.execSQL("PRAGMA foreign_keys=ON");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP_SECTION_TABLE);
            db.execSQL(SQL_DROP_COURSE_TABLE);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}

