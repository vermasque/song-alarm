package org.vermasque.songalarm.test;

import org.vermasque.songalarm.SongAlarmActivity;

import android.content.res.Resources;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;

import com.jayway.android.robotium.solo.Solo;

public class SongAlarmActivityTest extends
		ActivityInstrumentationTestCase2<SongAlarmActivity>
{
	private Solo solo;
	
	private SongAlarmActivity mActivity;
	
	private Preference mAlarmTimePref, mSongPref;
	private CheckBoxPreference mAlarmEnabledPref; 
	
	public SongAlarmActivityTest()
	{
		super("org.vermasque.songalarm", SongAlarmActivity.class);
	}

	/* (non-Javadoc)
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		
		// do this to send only key events to tested activities
		setActivityInitialTouchMode(false); 
		
		mActivity = getActivity();

		mAlarmTimePref = mActivity.findPreference(SongAlarmActivity.PREF_ALARM_TIME);
		mAlarmEnabledPref = 
			(CheckBoxPreference)mActivity.findPreference(SongAlarmActivity.PREF_ALARM_ENABLED);
		mSongPref = mActivity.findPreference(SongAlarmActivity.PREF_SONG);	
		
		solo = new Solo(this.getInstrumentation(), mActivity);
	}
	
	public void testPreConditions() 
	{
		final String SUMMARY_DEFAULT = 
			mActivity.getResources().getString(
				org.vermasque.songalarm.R.string.pref_summary_default);
		
		assertAlarmNotEnabled();
		assertEquals(SUMMARY_DEFAULT, mAlarmTimePref.getSummary());
		assertEquals(SUMMARY_DEFAULT, mSongPref.getSummary());
	}
	
	public void testNoInputsSet()
	{
		attemptEnableAlarm();
		
		assertAlarmNotEnabled();
	}

	private void assertAlarmNotEnabled()
	{
		assertFalse(mActivity.isAlarmEnabled());
		assertFalse(mAlarmEnabledPref.isChecked());
	}

	private void attemptEnableAlarm()
	{
		solo.clickOnCheckBox(0);
	}
	
	public void testSongNotSet()
	{
		// Had to make a String from CharSequence
		solo.clickOnText(mAlarmTimePref.getTitle().toString());
		
		// date_time_set is in com.android.internal.R based on TimePickerDialog
		// source code.  The string is defined in 
		// <sdk>/platforms/<insert platform here>/res/values/string.xml
		solo.clickOnButton(
			getInstrumentation().getContext().getResources().getString(
				R.string.date_time_set));
		
		attemptEnableAlarm();
		
		assertAlarmNotEnabled();
	}
}
