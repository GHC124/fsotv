/**
 * 
 */
package com.fsotv.bao;

import java.io.IOException;

import com.fsotv.utils.JsonHelper;
import com.fsotv.vo.VideosVO;
import com.google.gson.Gson;

/**
 * @author Cuongvm1
 * @version 0.1
 * @created 05/04/2013
 * @updated 05/04/2013
 * 
 */
public class VideoBao {
	public static VideosVO getVideoDetail() {
		Gson gson = new Gson();
		String content;
		VideosVO videoVO = null;
		try {
			content = JsonHelper.getJSONC();
			videoVO = gson.fromJson(content, VideosVO.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return videoVO;

	}
}
