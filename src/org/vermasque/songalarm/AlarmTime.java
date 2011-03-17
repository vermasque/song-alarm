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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents clock time fields (hour and minutes) 
 * representing a once-per-day alarm.
 */
public class AlarmTime implements Parcelable
{
    public static final Parcelable.Creator<AlarmTime> CREATOR
    = new Parcelable.Creator<AlarmTime>() {
		public AlarmTime createFromParcel(Parcel in) {
		    return new AlarmTime(in);
		}
		
		public AlarmTime[] newArray(int size) {
		    return new AlarmTime[size];
		}
    };
	
	private int mHourOfDay, mMinutes;

	/**
	 * @see AlarmTime#updateTime(int, int)
	 */
	public AlarmTime(int hourOfDay, int minutes)
	{
		updateTime(hourOfDay, minutes);
	}
	
	/**
	 * @see AlarmTime#writeToParcel(Parcel, int)
	 */
	private AlarmTime(Parcel in)
	{
		updateTime(in.readInt(), in.readInt());
	}

	/**
	 * Update the clock time fields.
	 * 
	 * @param hourOfDay 24-hr clock time hour field
	 * @param minutes   clock time minutes field
	 */
	public void updateTime(int hourOfDay, int minutes)
	{
		this.mHourOfDay = hourOfDay;
		this.mMinutes = minutes;
	}

	public int getHourOfDay()
	{
		return mHourOfDay;
	}

	public int getMinutes()
	{
		return mMinutes;
	}

	@Override
	public int describeContents()
	{
		return 0; // no special marshalling information to indicate
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) // flags ignored
	{
		dest.writeInt(mHourOfDay);
		dest.writeInt(mMinutes);
	}
	
	public long toTimestamp()
	{
		Calendar cal = Calendar.getInstance();	
		
		// guarantee current time set to avoid side effects of previous calls to this function
		cal.setTimeInMillis(System.currentTimeMillis()); 
		
		int currentHourOfDay = cal.get(Calendar.HOUR_OF_DAY), 
			currentMinutes = cal.get(Calendar.MINUTE);
		
		if (mHourOfDay < currentHourOfDay || 
			(currentHourOfDay == mHourOfDay && mMinutes <= currentMinutes)) 
		{
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND,      0);
		cal.set(Calendar.HOUR_OF_DAY, mHourOfDay);
		cal.set(Calendar.MINUTE,      mMinutes);
		
		return cal.getTimeInMillis();
	}
}