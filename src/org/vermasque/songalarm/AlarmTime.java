package org.vermasque.songalarm;

/**
 * Represents clock time fields (hour and minutes) 
 * representing a once-per-day alarm.
 */
public class AlarmTime
{
	private int hourOfDay, minutes;

	/**
	 * @see AlarmTime#updateTime(int, int)
	 */
	public AlarmTime(int hourOfDay, int minutes)
	{
		updateTime(hourOfDay, minutes);
	}
	
	/**
	 * Update the clock time fields.
	 * 
	 * @param hourOfDay 24-hr clock time hour field
	 * @param minutes   clock time minutes field
	 */
	public void updateTime(int hourOfDay, int minutes)
	{
		this.hourOfDay = hourOfDay;
		this.minutes = minutes;
	}

	public int getHourOfDay()
	{
		return hourOfDay;
	}

	public int getMinutes()
	{
		return minutes;
	}
	
}