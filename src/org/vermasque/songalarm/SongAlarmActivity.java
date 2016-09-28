/**
 *  This file is part of Song Alarm.
 * 
 *  Song Alarm is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  Copyright Sahil Verma, 2011.  
 */

package org.vermasque.songalarm;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import android.widget.Toast;

public class SongAlarmActivity extends PreferenceActivity implements OnPreferenceClickListener, OnTimeSetListener, OnPreferenceChangeListener
{
	public static final String PREF_SONG = "song";
	public static final String PREF_ALARM_ENABLED = "alarm_enabled";
	public static final String PREF_ALARM_TIME = "alarm_time";
	
	private static final int RESULT_ID_SONG = 0;
	
	private static final int DIALOG_ID_ALARM_TIME = 0;
	
	private Preference mAlarmTimePref, mAlarmEnabledPref, mSongPref;	
	
	private AlarmTime alarmTime;
	
	private PendingIntent lastAlarmIntent;
	
	private Uri songUri;
	
	public SongAlarmActivity()
	{
		alarmTime       = null;
		lastAlarmIntent = null;
		songUri         = null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Add views of preferences to existing empty layout
		// Content view must have been set in superclass onCreate
		addPreferencesFromResource(R.xml.preferences);

		mAlarmTimePref = findPreference(PREF_ALARM_TIME);
		mAlarmEnabledPref = findPreference(PREF_ALARM_ENABLED);
		mSongPref = findPreference(PREF_SONG);
		
		mAlarmTimePref.setOnPreferenceClickListener(this);		
		mAlarmEnabledPref.setOnPreferenceChangeListener(this);
		mSongPref.setOnPreferenceClickListener(this);
	}
	
	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle state)
	{
		super.onRestoreInstanceState(state);
		
		lastAlarmIntent = state.getParcelable(PREF_ALARM_ENABLED);
		songUri = state.getParcelable(PREF_SONG);
		alarmTime = state.getParcelable(PREF_ALARM_TIME);
		
		updateSongSummary();
		updateTimeSummary();
	}

	private void updateTimeSummary()
	{
		if (null == alarmTime) {
			return;
		}
		
		Calendar cal = Calendar.getInstance();	
		
		cal.setTimeInMillis(alarmTime.toTimestamp());
		
		mAlarmTimePref.setSummary(
			DateFormat.format(
					getResources().getString(R.string.date_format_alarm_time), 
					cal)
		);		
	}

	private void updateSongSummary()
	{
		if (null == songUri) {
			return;
		}
		
		// assume external storage because audio files are not tied
		// to a specific application and should be world-readable and exportable
		Uri contentUri = Audio.Media.EXTERNAL_CONTENT_URI;
						
		String[] columns = {
			Audio.AudioColumns.ARTIST,   Audio.AudioColumns.ALBUM, 
			Audio.AudioColumns.DURATION, Audio.AudioColumns.TITLE
		};
		
		String whereClause = BaseColumns._ID + "=?";
		
		String[] selectArgs = {songUri.getLastPathSegment()}; //{"LIMIT 1"}; ?
		
		Cursor cursor = managedQuery(contentUri, columns, whereClause, selectArgs, null);
					
		if (cursor.moveToFirst()) {
			
			String artist, album, title;
			long duration;
			
			try {
				artist = getColumnStringValue(cursor, Audio.AudioColumns.ARTIST);
				album  = getColumnStringValue(cursor, Audio.AudioColumns.ALBUM);
				title  = getColumnStringValue(cursor, Audio.AudioColumns.TITLE);
			
				duration = cursor.getLong(
					cursor.getColumnIndexOrThrow(Audio.AudioColumns.DURATION));
			
				StringBuilder builder = new StringBuilder();
		
				builder.append(title).append('\n');
				builder.append(artist).append('\n');
				builder.append(album).append('\n');
				
				addMinSecString(builder, duration);	
				
				mSongPref.setSummary(builder.toString());
			}
			catch(IllegalArgumentException e) {
				Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		
		outState.putParcelable(PREF_ALARM_ENABLED, lastAlarmIntent);
		outState.putParcelable(PREF_SONG, songUri);
		outState.putParcelable(PREF_ALARM_TIME, alarmTime);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
		case DIALOG_ID_ALARM_TIME:
		{
			Resources res = getResources();
			boolean bIs24Hour = false;
			
			if (DateFormat.is24HourFormat(this))
			{
				bIs24Hour = true;
			}
			
			return new TimePickerDialog(
				this, // context 
				this, // time set handler
				res.getInteger(R.integer.default_hour), 
				res.getInteger(R.integer.default_minutes), 
				bIs24Hour // not 24-hr clock format
			);
		}
		default:
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		if (DIALOG_ID_ALARM_TIME == id && null != alarmTime)
		{
			((TimePickerDialog)dialog).updateTime(
				alarmTime.getHourOfDay(), alarmTime.getMinutes()
			);
		}
	}

	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		if (pref == mAlarmTimePref)
		{
			showDialog(DIALOG_ID_ALARM_TIME);	
			return true;
		}
		else if (pref == mSongPref) {
			Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
			
			innerIntent.setType("audio/*");
			innerIntent.addCategory(Intent.CATEGORY_OPENABLE);
			
			startActivityForResult(
				Intent.createChooser(innerIntent, null), RESULT_ID_SONG);
			
			return true;
		}
	
		return false;
	}
	
	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent)
	{
		if (RESULT_OK == resultCode) {
			switch (requestCode)
			{
			case RESULT_ID_SONG:
				songUri = resultIntent.getData();
						
				if (isAlarmEnabled()) {
					updateAlarm();
				}
				
				updateSongSummary();
				break;
			default:
				break;
			}
		}
	}

	private void updateAlarm()
	{
		disableAlarm();
		enableAlarm();
		showToast(R.string.info_alarm_updated);
	}

	private void disableAlarm()
	{
		getAlarmManager().cancel(lastAlarmIntent);
		lastAlarmIntent = null;
	}

	private void addMinSecString(StringBuilder builder, long durationMillis)
	{		
		long minutes = durationMillis / 1000 / 60;
		long seconds = (durationMillis - (minutes * 60 * 1000)) / 1000;
		
		builder.append(minutes).append(':');
		
		if (seconds < 10)
		{
			builder.append('0');
		}
		
		builder.append(seconds);
	}

	private String getColumnStringValue(Cursor cursor, String columnName)
	{
		return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
	}

	@Override
	public void onTimeSet(TimePicker picker, int hourOfDay, int minutes)
	{
		if (null == alarmTime) {
			alarmTime = new AlarmTime(hourOfDay, minutes);
		} else {
			alarmTime.updateTime(hourOfDay, minutes);
		}
		
		if (isAlarmEnabled()) {
			updateAlarm();
		}
		
		updateTimeSummary();
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
				if (null == alarmTime)
				{
					showToast(R.string.error_no_time_set);
				} 
				else if (null == songUri) 
				{
					showToast(R.string.error_no_song_set);
				}
				else
				{		
					enableAlarm();
					showToast(R.string.info_alarm_enabled);
					allowChange = true;
				}
			} 
			else
			{
				disableAlarm();			
				showToast(R.string.info_alarm_disabled);
				allowChange = true;

			}
		}
		
		return allowChange;
	}

	private void enableAlarm()
	{
		Intent innerIntent = new Intent(Intent.ACTION_VIEW, songUri);
		
		// required by PendingIntent.getActivity
		innerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		// if a previous intent was created that matches this one,
		// cancel it just to start fresh 
		lastAlarmIntent = 
			PendingIntent.getActivity(
				this, 
				0, 
				innerIntent, 
				PendingIntent.FLAG_CANCEL_CURRENT);
		
		getAlarmManager().setRepeating(
			AlarmManager.RTC_WAKEUP, 
			alarmTime.toTimestamp(), 
			AlarmManager.INTERVAL_DAY, 
			lastAlarmIntent);
	}

	private void showToast(int messageResourceId)
	{
		Toast.makeText(
			this, 
			getResources().getString(messageResourceId), 
			Toast.LENGTH_SHORT
		).show();
	}

	private AlarmManager getAlarmManager()
	{
		return (AlarmManager)getSystemService(ALARM_SERVICE);
	}

	public boolean isAlarmEnabled()
	{
		return (null != lastAlarmIntent);
	}
}
