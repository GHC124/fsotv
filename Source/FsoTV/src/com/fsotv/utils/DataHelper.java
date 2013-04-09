package com.fsotv.utils;

import java.util.List;

import android.widget.ArrayAdapter;

public class DataHelper {

	public static void updateAdapter(ArrayAdapter<Object> arrayAdapter,
			List<Object> listOfObject) {
		arrayAdapter.clear();
		for (Object object : listOfObject) {
			arrayAdapter.add(object);
		}
	}

	public static String parseUrl(String url) throws Exception {
		return url;
	}

	public static String numberWithCommas(long x) {
		String result = (x + "").replace("/\\B(?=(\\d{3})+(?!\\d))/g", ",");
		return result;
	}

	/**
	 * Function to convert seconds time to Timer Format
	 * Hours:Minutes:Seconds
	 * */
	public static String secondsToTimer(long secs) {
		int hours = (int)( secs / 3600);
		int remainder = (int)(secs % 3600);
		int minutes =  remainder / 60;
		int seconds = remainder % 60;
		String disHour = (hours < 10 ? "0" : "") + hours;
		String disMinu = (minutes < 10 ? "0": "") + minutes;
		String disSec = (seconds < 10 ? "0" : "") + seconds;
		return disHour + ":" + disMinu + ":" + disSec;
	}

	/**
	 * Function to convert milliseconds time to Timer Format
	 * Hours:Minutes:Seconds
	 * */
	public static String milliSecondsToTimer(long milliseconds) {
		String finalTimerString = "";
		String secondsString = "";

		// Convert total duration into time
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
		// Add hours if there
		if (hours > 0) {
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}

		finalTimerString = finalTimerString + minutes + ":" + secondsString;

		// return timer string
		return finalTimerString;
	}

	/**
	 * Function to get Progress percentage
	 * 
	 * @param currentDuration
	 * @param totalDuration
	 * */
	public static int getProgressPercentage(long currentDuration,
			long totalDuration) {
		Double percentage = (double) 0;

		long currentSeconds = (int) (currentDuration / 1000);
		long totalSeconds = (int) (totalDuration / 1000);

		// calculating percentage
		percentage = (((double) currentSeconds) / totalSeconds) * 100;

		// return percentage
		return percentage.intValue();
	}

	/**
	 * Function to change progress to timer
	 * 
	 * @param progress
	 *            -
	 * @param totalDuration
	 *            returns current duration in milliseconds
	 * */
	public static int progressToTimer(int progress, int totalDuration) {
		int currentDuration = 0;
		totalDuration = (int) (totalDuration / 1000);
		currentDuration = (int) ((((double) progress) / 100) * totalDuration);

		// return current duration in milliseconds
		return currentDuration * 1000;
	}
}
