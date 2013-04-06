package com.fsotv.utils;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class YoutubeFeedHandler extends DefaultHandler {
	List<String> links;
	String temp;

	public YoutubeFeedHandler() {
		links = new ArrayList<String>();
	}

	public List<String> getData() {
		return links;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase("content"))
		{
			
		};
		
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equalsIgnoreCase("id")) {
			String ids[] = temp.split("/");
			links.add(ids[ids.length - 1]);
		}
		super.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		temp = new String(ch, start, length);
		
	}
}
