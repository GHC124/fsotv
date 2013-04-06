package com.fsotv.utils;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.util.Log;

public class YoutubeFeedReader {
	public static List<String> parse(InputStream is) {
		List<String> values = null;
		try {
			XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser()
					.getXMLReader();
			YoutubeFeedHandler youtubeHandler = new YoutubeFeedHandler();
			xmlReader.setContentHandler(youtubeHandler);
			xmlReader.parse(new InputSource(is));
			values = youtubeHandler.getData();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Error parse", e.toString());
		}
		return values;
	}

}
