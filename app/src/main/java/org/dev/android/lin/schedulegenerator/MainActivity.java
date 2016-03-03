package org.dev.android.lin.schedulegenerator;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_BREAK = "maxBreak";
    public static final String KEY_DAYS = "maxDays";
    public static final String KEY_CLASSES = "maxClasses";
    public static final String KEY_MIN_CREDIT = "minCredits";
    public static final String KEY_MAX_CREDIT = "maxCredits";
    public static final String PREFS_NAME = "ScheduleGeneratorPreference";
    public static final int DIALOG_REQUEST_CODE = 999;
    public static final String KEY_PRIMARY_CLASS = "KEY_PRIMARY_CLASS";
    public static final String KEY_SECONDARY_CLASS = "KEY_SECONDARY_CLASS";
    public static final int[] DAY_IDS = {R.id.section_monday, R.id.section_tuesday, R.id.section_wednesday,
            R.id.section_thursday, R.id.section_friday, R.id.section_saturday,
            R.id.section_sunday};
    public static final Float[] CREDIT_LIST = {0.0f, 0.5f, 1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f,
            5.0f, 5.5f, 6.0f, 6.5f, 7.0f};
    private static final List<Course> essentialCourses = new ArrayList<>();
    private static final List<Course> secondaryCourses = new ArrayList<>();
    private static ClassListDB classListDB;
    private static HashMap<String, Course> essentialCoursesHash;
    private static HashMap<String, Course> secondaryCoursesHash;
    private static CoursesArrayAdapter essentialCourseAdapter;
    private static CoursesArrayAdapter secondaryCourseAdapter;
    private static List<String> allCourseNames = new ArrayList<>();

    public static void refreshCourses(int option) {
        if (option == 0 || option == 1) {
            essentialCoursesHash = classListDB.getEssentialCoursesHash();
            essentialCourses.clear();
            essentialCourses.addAll(essentialCoursesHash.values());
            Collections.sort(essentialCourses, Course.comparator);
            essentialCourseAdapter.notifyDataSetChanged();
        }
        if (option == 0 || option == 2) {
            secondaryCoursesHash = classListDB.getSecondaryCoursesHash();
            secondaryCourses.clear();
            secondaryCourses.addAll(secondaryCoursesHash.values());
            Collections.sort(secondaryCourses, Course.comparator);
            secondaryCourseAdapter.notifyDataSetChanged();
        }
        allCourseNames = new ArrayList<>();
        allCourseNames.addAll(essentialCoursesHash.keySet());
        allCourseNames.addAll(secondaryCoursesHash.keySet());
    }

    public static void deleteSection(Course.Section section, Context context) {
        if (classListDB.deleteSection(section) >= 0) {
            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
        }
    }

    public static void deleteCourse(Course course, Context context) {
        if (classListDB.deleteCourse(course) >= 0) {
            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Course.is24HourFormat = DateFormat.is24HourFormat(getApplicationContext());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PageAdapter pageAdapter;
        ViewPager viewPager;

        pageAdapter = new PageAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(pageAdapter);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        classListDB = new ClassListDB(getApplication());
        classListDB.open();

        essentialCourseAdapter = new CoursesArrayAdapter(this, R.layout.course_row, essentialCourses);
        secondaryCourseAdapter = new CoursesArrayAdapter(this, R.layout.course_row, secondaryCourses);
        refreshCourses(0);
/*
        essentialCoursesHash = classListDB.getEssentialCoursesHash();
        secondaryCoursesHash = classListDB.getSecondaryCoursesHash();
        essentialCourses = new ArrayList<>(essentialCoursesHash.values());
        secondaryCourses = new ArrayList<>(secondaryCoursesHash.values());
        allCourseNames.addAll(essentialCoursesHash.keySet());
        allCourseNames.addAll(secondaryCoursesHash.keySet());*/


        Button addBtn = (Button) findViewById(R.id.btn_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fragmentManager = getSupportFragmentManager();

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                Fragment prev = fragmentManager.findFragmentByTag("dialog");

                if (prev != null) {
                    fragmentTransaction.remove(prev);
                }
                fragmentTransaction.addToBackStack(null);

                DialogFragment addClassFrag = AddClassDialogFragment.newInstance();
                    /*Bundle args = new Bundle();
                    args.putBoolean("isEssential", true);
                    addClassFrag.setArguments(args);*/
                addClassFrag.show(fragmentTransaction, "dialog");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_generate:
                Intent intent = new Intent(this, GeneratorActivity.class);
                startActivity(intent);
        }
        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        classListDB.close();
    }

    /*
     * Fragment Pages
     */
    /* Page that lists all the courses */
    public static class EssentialCourses extends Fragment {

        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {

            final View pageView = inflater.inflate(R.layout.fragment_course_list, container, false);
            ((TextView) pageView.findViewById(R.id.label_course_list)).setText(
                    getResources().getString(R.string.label_primary));

            final ListView listView = (ListView) pageView.findViewById(R.id.course_list);
            listView.setAdapter(essentialCourseAdapter);

            return pageView;
        }


        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            essentialCourseAdapter.notifyDataSetChanged();
        }
    }

    public static class SecondaryCourses extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View pageView = inflater.inflate(R.layout.fragment_course_list, container, false);

            ((TextView) pageView.findViewById(R.id.label_course_list)).setText(
                    getResources().getString(R.string.label_secondary));

            final ListView listView = (ListView) pageView.findViewById(R.id.course_list);
            listView.setAdapter(secondaryCourseAdapter);

            return pageView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            secondaryCourseAdapter.notifyDataSetChanged();
        }

    }

    /* Page that shows the preferences list  */
    public static class SettingsFragment extends Fragment {

        private String breakBarPrefix, dayBarPrefix, classBarPrefix,
                minCreditPrefix, maxCreditPrefix;
        private TextView BreakBarVal, ClassBarVal, DayBarVal, minCreditVal, maxCreditVal;

        private SharedPreferences settings;
        private SharedPreferences.Editor editor;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            breakBarPrefix = getResources().getString(R.string.label_max_gap) + " ";
            dayBarPrefix = getResources().getString(R.string.label_days) + " ";
            classBarPrefix = getResources().getString(R.string.label_max_class) + " ";
            minCreditPrefix = getResources().getString(R.string.label_min_credit) + " ";
            maxCreditPrefix = getResources().getString(R.string.label_max_credit) + " ";

            settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
            editor = settings.edit();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View pageView = inflater.inflate(R.layout.fragment_settings, container, false);


            SeekBar BreakBar = ((SeekBar) pageView.findViewById(R.id.seekBar_max_gap));
            SeekBar ClassBar = ((SeekBar) pageView.findViewById(R.id.seekBar_class_per_day));
            SeekBar DayBar = ((SeekBar) pageView.findViewById(R.id.seekBar_days));
            SeekBar MinCreditBar = ((SeekBar) pageView.findViewById(R.id.seekBar_min_credit));
            SeekBar MaxCreditBar = ((SeekBar) pageView.findViewById(R.id.seekBar_max_credit));

            BreakBarVal = ((TextView) pageView.findViewById(R.id.label_max_break));
            DayBarVal = (TextView) pageView.findViewById(R.id.label_days_off);
            ClassBarVal = (TextView) pageView.findViewById(R.id.label_class_per_day);
            minCreditVal = (TextView) pageView.findViewById(R.id.label_min_credit);
            maxCreditVal = (TextView) pageView.findViewById(R.id.label_max_credit);

            BreakBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String text = breakBarPrefix + progress;
                    BreakBarVal.setText(text);
                    editor.putInt(KEY_BREAK, progress);
                    editor.commit();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            ClassBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String text = classBarPrefix + (progress + 1);
                    ClassBarVal.setText(text);
                    editor.putInt(KEY_CLASSES, progress + 1);
                    editor.commit();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            DayBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String text = dayBarPrefix + (progress + 1);
                    DayBarVal.setText(text);
                    editor.putInt(KEY_DAYS, progress + 1);
                    editor.commit();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            MinCreditBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String text = minCreditPrefix + (progress + 1);
                    minCreditVal.setText(text);
                    editor.putInt(KEY_MIN_CREDIT, progress + 1);
                    editor.commit();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            MaxCreditBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String text = maxCreditPrefix + (progress + 1);
                    maxCreditVal.setText(text);
                    editor.putInt(KEY_MAX_CREDIT, progress + 1);
                    editor.commit();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            int temp = settings.getInt(KEY_BREAK, 1);
            if (temp < 0 || temp > BreakBar.getMax()) {
                BreakBar.setProgress(BreakBar.getMax() / 2);
            } else {
                if (temp == 0) {
                    BreakBar.setProgress(1);
                }
                BreakBar.setProgress(temp);
            }
            temp = settings.getInt(KEY_DAYS, 1) - 1;
            if (temp < 0 || temp > DayBar.getMax()) {
                DayBar.setProgress((DayBar.getMax() / 2) + 1);
            } else {
                if (temp == 0) {
                    DayBar.setProgress(1);
                }
                DayBar.setProgress(temp);
            }
            temp = settings.getInt(KEY_CLASSES, 1) - 1;
            if (temp < 0 || temp > ClassBar.getMax()) {
                ClassBar.setProgress((ClassBar.getMax() / 2) + 1);
            } else {
                if (temp == 0) {
                    ClassBar.setProgress(1);
                }
                ClassBar.setProgress(temp);
            }
            temp = settings.getInt(KEY_MIN_CREDIT, 1) - 1;
            if (temp < 0 || temp > MinCreditBar.getMax()) {
                MinCreditBar.setProgress((MinCreditBar.getMax() / 2) + 1);
            } else {
                if (temp == 0) {
                    MinCreditBar.setProgress(1);
                }
                MinCreditBar.setProgress(temp);
            }
            temp = settings.getInt(KEY_MAX_CREDIT, 1) - 1;
            if (temp < 0 || temp > MaxCreditBar.getMax()) {
                MaxCreditBar.setProgress((MaxCreditBar.getMax() / 2) + 1);
            } else {
                if (temp == 0) {
                    MaxCreditBar.setProgress(1);
                }
                MaxCreditBar.setProgress(temp);
            }

            return pageView;
        }

        @Override
        public void onPause() {
            super.onPause();
            editor.commit();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            editor.commit();
        }
    }

    /* popup add class form */
    public static class AddClassDialogFragment extends DialogFragment {

        ArrayAdapter<Float> creditAdapter;
        private AutoCompleteTextView autoCompleteCourseName;
        private Spinner spinnerCourseCredit;
        private RadioGroup radioGroupPriority;
        private EditText edTxtSecNum;
        private EditText edTxtInstructor;
        private EditText edTxtLocation;
        private CheckBox chkBxsDaysOfWeek[] = new CheckBox[7];
        private int classTimeStartHour = 0;
        private int classTimeStartMinute = 0;
        private int classTimeEndHour = 0;
        private int classTImeEndMinute = 0;

        public static AddClassDialogFragment newInstance() {
            return new AddClassDialogFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {

            getDialog().setTitle("Add Class");
            final View v = inflater.inflate(R.layout.fragment_add_class, container, false);

            autoCompleteCourseName = (AutoCompleteTextView) v.findViewById(R.id.edtxt_course);
            radioGroupPriority = (RadioGroup) v.findViewById(R.id.rg_course_priority);
            edTxtSecNum = (EditText) v.findViewById(R.id.edtxt_section_num);
            edTxtInstructor = (EditText) v.findViewById(R.id.edtxt_instructor);
            edTxtLocation = (EditText) v.findViewById(R.id.edtxt_section_location);
            spinnerCourseCredit = (Spinner) v.findViewById(R.id.spinner_course_credit);
            for (int i = 0; i < 7; i++) {
                chkBxsDaysOfWeek[i] = (CheckBox) v.findViewById(DAY_IDS[i]);
            }

            creditAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_row, CREDIT_LIST);
            spinnerCourseCredit.setAdapter(creditAdapter);

            spinnerCourseCredit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (view != null) {
                        ((TextView) view).setTextColor(ContextCompat.getColor(getContext(),
                                R.color.colorAccent));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinnerCourseCredit.setSelection(2);


            if (!allCourseNames.isEmpty()) {
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),
                        R.layout.spinner_row, allCourseNames);
                autoCompleteCourseName.setAdapter(arrayAdapter);
                autoCompleteCourseName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {
                        setCorrespondingInfo(view);
                    }
                });
                autoCompleteCourseName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus && v != null) {
                            setCorrespondingInfo(v);
                        }
                    }
                });
            }

            /*
            *  Setting Time
            * */

            final TextView startTime = (TextView) v.findViewById(R.id.start_time);
            final TextView endTime = (TextView) v.findViewById(R.id.end_time);

            startTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment picker = new TimePickerFragment();
                    Bundle args = new Bundle();
                    args.putBoolean("setStartTime", true);
                    args.putInt("hour", classTimeStartHour);
                    args.putInt("minute", classTimeStartMinute);
                    picker.setArguments(args);
                    picker.show(getFragmentManager(), "timePicker");
                }
            });
            endTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment picker = new TimePickerFragment();
                    Bundle args = new Bundle();
                    args.putBoolean("setStartTime", false);
                    args.putInt("hour", classTimeEndHour);
                    args.putInt("minute", classTImeEndMinute);
                    picker.setArguments(args);
                    picker.show(getFragmentManager(), "timePicker");
                }
            });


            /*
            *  Save Button onClick: Validates all required fields,
            *  Validate database add before closing form
            *  Fail validation will keep the form open
            * */
            Button btnSave = (Button) v.findViewById(R.id.btn_save);

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Course nCourse = validateForm();
                    if (nCourse != null) {
                        if (classListDB.addClass(nCourse) >= 0) {
                            if (nCourse.isPrimary()) {
                                refreshCourses(1);
                            } else {
                                refreshCourses(2);
                            }
                            dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Class Already Exists",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            /*
            *  Cancel Button
            * */
            Button btnCancel = (Button) v.findViewById(R.id.btn_cancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            return v;
        }

        @Nullable
        public Course validateForm() {

            View view = getView();
            if (view == null) {
                dismiss();
            }

            String courseName;
            boolean isPrimary = false;
            int sectionNumber;

            if ((courseName = this.autoCompleteCourseName.getText().toString()).length() < 3) {
                showTextFormErrorToast("Course name too short", this.autoCompleteCourseName);
                return null;
            }

            if (spinnerCourseCredit.getSelectedItemPosition() < 0) {
                Toast.makeText(getActivity(), "Select autoCompleteCourseName credit",
                        Toast.LENGTH_SHORT).show();
            }

            if (radioGroupPriority.getCheckedRadioButtonId() == -1) {
                Toast.makeText(getActivity(), "Select a priority", Toast.LENGTH_SHORT).show();
                return null;
            } else if (radioGroupPriority.getCheckedRadioButtonId() == R.id.rb_essential)
                isPrimary = true;

            try { // exception throw on empty or invalid string
                sectionNumber = Integer.parseInt(edTxtSecNum.getText().toString());
            } catch (NumberFormatException e) {
                showTextFormErrorToast("Section Number Invalid", edTxtSecNum);
                return null;
            }

            if ((classTimeStartHour * 60 + classTimeStartMinute) >=
                    (classTimeEndHour * 60 + classTImeEndMinute)) {
                Toast.makeText(getActivity(), "End-time is not after start-time",
                        Toast.LENGTH_SHORT).show();
                return null;
            }

            int daysOfWeek = Course.DAY_NON;
            for (int i = 0; i < 7; i++) {
                if (chkBxsDaysOfWeek[i].isChecked()) {
                    daysOfWeek = (daysOfWeek | Course.WEEK_DAYS[i]);
                }
            }
            if (daysOfWeek == Course.DAY_NON) {
                Toast.makeText(getActivity(), "No class days checked", Toast.LENGTH_SHORT).show();
                return null;
            }

            return new Course(
                    courseName.replaceAll(" ",""),
                    Float.parseFloat(spinnerCourseCredit.getSelectedItem().toString()),
                    isPrimary,
                    sectionNumber,
                    classTimeStartHour * 60 + classTimeStartMinute,
                    classTimeEndHour * 60 + classTImeEndMinute,
                    edTxtInstructor.getText().toString(),
                    edTxtLocation.getText().toString(),
                    (byte) daysOfWeek
            );
        }

        private void showTextFormErrorToast(String text, EditText editText) {
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            editText.requestFocus();
        }

        public void setClassTime(int hour, int minute, boolean isStartTime) {
            View view = getView();
            if (view == null) {
                return;
            }
            TextView timeText;
            if (isStartTime) {
                timeText = (TextView) view.findViewById(R.id.start_time);
                classTimeStartHour = hour;
                classTimeStartMinute = minute;
            } else {
                timeText = (TextView) view.findViewById(R.id.end_time);
                classTimeEndHour = hour;
                classTImeEndMinute = minute;
            }
            if (timeText != null) {
                String nTime;
                if (DateFormat.is24HourFormat(getActivity())) {
                    nTime = ((hour < 10) ? "0" + hour : Integer.toString(hour)) + ":" +
                            ((minute < 10) ? "0" + minute : Integer.toString(minute));
                } else {
                    int nHour = (hour == 0 || hour == 12) ? 12 : (hour % 12);
                    nTime = (nHour < 10 ? "0" + nHour : Integer.toString(nHour)) + ":" +
                            ((minute < 10) ? "0" + minute : Integer.toString(minute)) +
                            ((hour / 12) > 0 ? " PM" : " AM");
                }
                timeText.setText(nTime);
            }
        }

        public void setCorrespondingInfo(View view) {
            if (view != null) {
                Course course = essentialCoursesHash.get(((TextView) view).getText().toString());
                if (course == null) {
                    course = secondaryCoursesHash.get(((TextView) view).getText().toString());
                }
                if (course != null) {
                    spinnerCourseCredit.setSelection(creditAdapter.getPosition(course.getCredit()));

                    RadioButton radioButton;

                    if (course.isPrimary()) {
                        radioButton = (RadioButton) radioGroupPriority.findViewById(R.id.rb_essential);
                        if (radioButton != null) {
                            radioButton.setChecked(true);
                        }
                    } else {
                        radioButton = (RadioButton) radioGroupPriority.findViewById(R.id.rb_secondary);
                        if (radioButton != null) {
                            radioButton.setChecked(true);
                        }
                    }
                }
            }
        }

        public static class TimePickerFragment extends DialogFragment
                implements TimePickerDialog.OnTimeSetListener {

            @NonNull
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                // Use the current time as the default values for the picker
                // final Calendar c = Calendar.getInstance();
                int hour = 0;
                int minute = 0;
                Bundle args = getArguments();
                if (args != null) {
                    hour = args.getInt("hour", 0);
                    minute = args.getInt("minute", 0);
                }

                // Create a new instance of TimePickerDialog and return it
                return new TimePickerDialog(getActivity(), this, hour, minute,
                        DateFormat.is24HourFormat(getActivity()));
            }

            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Do something with the time chosen by the user
                AddClassDialogFragment parentFrag = (AddClassDialogFragment)
                        getFragmentManager().findFragmentByTag("dialog");
                if (parentFrag != null) {
                    if (getArguments().getBoolean("setStartTime", false)) {
                        parentFrag.setClassTime(hourOfDay, minute, true);
                    } else {
                        parentFrag.setClassTime(hourOfDay, minute, false);
                    }
                }
            }
        }
    }

    /*
    *  Class to manage fragment pages
    * */
    public class PageAdapter extends FragmentStatePagerAdapter {

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new EssentialCourses();
                case 1:
                    return new SecondaryCourses();
                case 2:
                    return new SettingsFragment();
            }
            return (new Fragment());
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Essential";
                case 1:
                    return "Secondary";
                case 2:
                    return "Preferences";
            }
            return null;
        }

    }
}
