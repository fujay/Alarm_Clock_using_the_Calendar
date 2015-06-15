package laboratory.fra_uas.eu.hefnerklammenzianochs.alarmclockusingthecalendar;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

public class Clock extends Fragment {

    public Alarms alarms;

    private List<Alarm> alarmList;
    private TimePicker timePicker;
    private ViewGroup weekdayButtons;

    private final List<List<Alarm>> alarmListList = new ArrayList<List<Alarm>>();
    private ListView listView;
    private AlarmArrayAdapter alarmArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View clock = inflater.inflate(R.layout.clock_layout, container, false);

        timePicker = (TimePicker) clock.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(getActivity()));

        weekdayButtons = (ViewGroup) clock.findViewById(R.id.weekDayButtons);

        alarms = new Alarms(this.getActivity()); //TESTEN .getApplicationContext() oder OHNE

        List<Integer> weekdays = Alarm.weekdays();
        for(int i = 0; i < weekdayButtons.getChildCount(); i++) {
            ToggleButton weekdayButton = (ToggleButton) weekdayButtons.getChildAt(i);
            weekdayButton.setTextOn(Alarm.weekdayShortString(weekdays.get(i)));
            weekdayButton.setTextOff(weekdayButton.getTextOn());
            weekdayButton.setTag(weekdays.get(i));
        }

        update();

        getActivity().getActionBar().setHomeButtonEnabled(true);

        getActivity().getActionBar().setDisplayShowCustomEnabled(true);
        Switch alarmSwitch = new Switch(this.getActivity().getApplicationContext()); // TESTEN
        alarmSwitch.setChecked(alarms.isActive(this.getActivity().getApplicationContext())); // TESTEN
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                alarms.setActive(Clock.this.getActivity().getApplicationContext(), isChecked); // TESTEN
            }
        });
        getActivity().getActionBar().setCustomView(alarmSwitch);
        alarmSwitch.setChecked(true);

        // List with Alarm groups
        listView = (ListView) clock.findViewById(android.R.id.list);
        alarmArrayAdapter = new AlarmArrayAdapter(this.getActivity().getApplicationContext(), alarmListList); // TESTEN
        listView.setAdapter(alarmArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectAlarmItem(position);
            }
        });

        return clock;
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     */
    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    /**
     * Called when the Fragment is no longer resumed
     */
    @Override
    public void onPause() {
        alarms.storeAlarms(this.getActivity().getApplicationContext()); // TESTEN
        super.onPause();

    }

    private final CompoundButton.OnCheckedChangeListener toggleWeekDayButton = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int weekday = (Integer) buttonView.getTag();
            Alarm alarm = alarms.alarmsPerDayOfWeek[weekday];
            /*Alarm alarm = alarms.alarmsPerDayOfWeek[weekday]; ((AlarmsActivity) getActivity()).alarms.alarmsPerDayOfWeek[weekday];*/
            if(isChecked) {
                alarm.active = true;
                alarm.hours = timePicker.getCurrentHour();
                alarm.minutes = timePicker.getCurrentMinute();
                alarmList.add(alarm);
            } else {
                alarm.active = false;
                alarmList.remove(alarm);
            }
        }
    };

    private final TimePicker.OnTimeChangedListener TimeChangedListener = new TimePicker.OnTimeChangedListener() {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            for(Alarm alarm : alarmList) {
                alarm.hours = hourOfDay;
                alarm.minutes = minute;
            }
        }
    };

    private void selectAlarmItem(int position) {
        List<Alarm> alarmList = alarmListList.get(position);
        listView.setItemChecked(position, true);

        Clock alarm = new Clock();
        alarm.loadAlarm(alarmList);

    }

    public void refresh() {
        // Update all alarms
        alarmListList.clear();
        alarmListList.addAll(alarms.alarmList());
        if(alarmListList.size() != 7) {
            alarmListList.add(new ArrayList<Alarm>()); // Add button
        }
        alarmArrayAdapter.notifyDataSetChanged();
    }

    public void loadAlarm(List<Alarm> alarmList) {
        this.alarmList = alarmList;
        if(timePicker != null) {
            update();
        }
    }

    private void update() {
        // Weekdays
        for(int i = 0; i < weekdayButtons.getChildCount(); i++) {
            ToggleButton button = (ToggleButton) weekdayButtons.getChildAt(i);
            button.setOnCheckedChangeListener(null); // No update
            button.setChecked(false);
            for(Alarm alarm : alarmList) {
                if(alarm.dayOfWeek == (Integer) button.getTag()) {
                     button.setChecked(true);
                    break;
                }
            }
            button.setOnCheckedChangeListener(toggleWeekDayButton);
        }
        // Time of day
        timePicker.setOnTimeChangedListener(null);
        if(alarmList.size() != 0) {
            Alarm firstAlarm = alarmList.get(0);
            timePicker.setCurrentHour(firstAlarm.hours);
            timePicker.setCurrentMinute(firstAlarm.minutes);
        } else {
            timePicker.setCurrentHour(7);
            timePicker.setCurrentMinute(0);
        }
        timePicker.setOnTimeChangedListener(TimeChangedListener);
    }

    private class AlarmArrayAdapter extends ArrayAdapter<List<Alarm>> {

        private List<List<Alarm>> alarmLists;

        public AlarmArrayAdapter(Context context, List<List<Alarm>> alarmLists) {
            super(context, android.R.layout.simple_list_item_2, android.R.id.text1, alarmLists);
            this.alarmLists = alarmLists;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            List<Alarm> alarmList = alarmLists.get(position);
            View row = super.getView(position, convertView, parent);

            TextView text1 = (TextView) row.findViewById(android.R.id.text1);
            TextView text2 = (TextView) row.findViewById(android.R.id.text2);
            if(alarmList.size() == 0) {
                text1.setText(R.string.add);
                text2.setText("");
                return row;
            }

            // Weekdays
            StringBuffer weekdays = new StringBuffer();
            for(Alarm alarm : alarmList) {
                if(weekdays.length() != 0) {
                    weekdays.append(", ");
                }
                weekdays.append(Alarm.weekdayShortString(alarm.dayOfWeek));
            }
            text1.setText(weekdays);

            // Time
            Alarm alarm = alarmList.get(0);
            text2.setText(Alarm.timeAsString(alarm.hours, alarm.minutes));

            if(listView.isItemChecked(position)) {
                row.setBackgroundColor(Color.argb(0x80, 0xA0, 0xA0, 0xA0));
            } else {
                row.setBackgroundColor(Color.TRANSPARENT);
            }

            return row;
        }
    }

}