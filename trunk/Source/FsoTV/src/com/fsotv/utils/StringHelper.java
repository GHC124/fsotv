/**
 * 
 */
package com.fsotv.utils;

/**
 * @author CuongVM1
 * 
 */
public class StringHelper {
	/**
	 * Fortmat title
	 * @param title Raw title
	 * @return The title after formatted
	 */
	public static String fomatTitle(String title) {
		if (title.length() > 40) {
			title = title.substring(0, 40) + "...";
		}
		
		return title;

	}
	
	/**
	 * 
	 * @param description
	 * @return The description after formatted
	 */
	public static String formatDescription(String description){
		if (description.length() > 150) {
			description = description.substring(0, 150) + "...";
		}
		
		return description;
	}
	/**
	 * 
	 * @param categoryTitle Raw content
	 * @return The title after formatted
	 */
	public static String fomatCategoryTitle(String categoryTitle) {
		if (categoryTitle.length() > 50) {
			categoryTitle = categoryTitle.substring(0, 50) + "...";
		}
		
		return categoryTitle;

	}
	
}
