/**
 * 
 */
package com.fsotv.utils;

/**
 * @author CuongVM1
 * 
 */
public class StringHelper {
	
	public static String fomatTitle(String title) {
		if (title.length() > 40) {
			title = title.substring(0, 40) + "...";
		}
		
		return title;

	}
	
	public static String formatDescription(String description){
		if (description.length() > 150) {
			description = description.substring(0, 150) + "...";
		}
		
		return description;
	}
	
	public static String fomatCategoryTitle(String title) {
		if (title.length() > 50) {
			title = title.substring(0, 50) + "...";
		}
		
		return title;

	}
	
}
