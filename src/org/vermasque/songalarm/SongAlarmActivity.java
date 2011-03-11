package org.vermasque.songalarm;

import java.util.Calendar;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import android.widget.Toast;

public class SongAlarmActivity extends PreferenceActivity implements OnPreferenceClickListener, OnTimeSetListener, OnPreferenceChangeListener
{
	private static final long NO_ALARM_TIME_SET = -1;
	
	private Preference mAlarmTimePref, mAlarmEnabledPref;

	private long alarmTimestamp;
	
	public SongAlarmActivity()
	{
		alarmTimestamp = NO_ALARM_TIME_SET;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Add views of preferences to existing empty layout
		// Content view must have been set in superclass onCreate
		addPreferencesFromResource(R.xml.preferences);

		mAlarmTimePref = findPreference("alarm_time");
		mAlarmEnabledPref = findPreference("alarm_enabled");
		
		mAlarmTimePref.setOnPreferenceClickListener(this);		
		mAlarmEnabledPref.setOnPreferenceChangeListener(this);
	}
	
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		if (pref == mAlarmTimePref) {
			Resources res = getResources();
			
			new TimePickerDialog(
				this, // context 
				this, // time set handler
				res.getInteger(R.integer.default_hour), 
				res.getInteger(R.integer.default_minutes), 
				false // not 24-hr clock format
			).show();
			
			return true;
		}
	
		return false;
	}
	
	@Override
	public void onTimeSet(TimePicker picker, int hourOfDay, int minutes)
	{
		Calendar cal = Calendar.getInstance();
		
		// guarantee current time set to avoid side effects of previous calls to this function
		cal.setTimeInMillis(System.currentTimeMillis()); 
		
		int currentHourOfDay = cal.get(Calendar.HOUR_OF_DAY), 
			currentMinutes = cal.get(Calendar.MINUTE);
		
		if (currentHourOfDay > hourOfDay || 
			(currentHourOfDay == hourOfDay && currentMinutes > minutes)) 
		{
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND,      0);
		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE,      minutes);
		
		alarmTimestamp = cal.getTimeInMillis();
		
		mAlarmTimePref.setSummary(
			DateFormat.format(
					getResources().getString(R.string.date_format_alarm_time), 
					cal)
		);
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue)
	{
		boolean allowChange = false;
		
		if (mAlarmEnabledPref == pref)
		{
			boolean enableAlarm = ((Boolean)newValue).booleanValue();
			
			if (enableAlarm) 
			{
				if (NO_ALARM_TIME_SET == alarmTimestamp) 
				{
					Toast.makeText(
						this, 
						getResources().getString(R.string.error_no_alarm_set), 
						Toast.LENGTH_SHORT
					).show();
				}
				else
				{		
					Toast.makeText(
						this, 
						getResources().getString(R.string.info_alarm_enabled), 
						Toast.LENGTH_SHORT
					).show();
					allowChange = true;				
				}
			} 
			else
			{	
				allowChange = true;
				alarmTimestamp = NO_ALARM_TIME_SET;
			}
		}
		
		return allowChange;
	}
	
	
}