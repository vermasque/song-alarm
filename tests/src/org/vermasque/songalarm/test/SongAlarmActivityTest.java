package org.vermasque.songalarm.test;

import org.vermasque.songalarm.SongAlarmActivity;

import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.test.ActivityInstrumentationTestCase2;

public class SongAlarmActivityTest extends
		ActivityInstrumentationTestCase2<SongAlarmActivity>
{
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
	}
	
	public void testPreConditions() 
	{
		final String SUMMARY_DEFAULT = 
			mActivity.getResources().getString(
				org.vermasque.songalarm.R.string.pref_summary_default);
		
		assertFalse(mAlarmEnabledPref.isChecked());
		assertEquals(SUMMARY_DEFAULT, mAlarmTimePref.getSummary());
		assertEquals(SUMMARY_DEFAULT, mSongPref.getSummary());
	}
}
