package com.byagowi.persiancalendar;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import calendar.PersianDate;

public class PersianDateHolidays {

	private static List<Holiday> holidays;

	static public void loadHolidays(InputStream xmlStream) {
		holidays = new ArrayList<Holiday>();
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			Document document = builder.parse(xmlStream);

			NodeList holidaysNodes = document.getElementsByTagName("holiday");
			for (int i = 0; i < holidaysNodes.getLength(); i++) {
				Node node = holidaysNodes.item(i);
				NamedNodeMap attrs = node.getAttributes();

				int year = Integer.parseInt(attrs.getNamedItem("year")
						.getTextContent());
				int month = Integer.parseInt(attrs.getNamedItem("month")
						.getTextContent());
				int day = Integer.parseInt(attrs.getNamedItem("day")
						.getTextContent());

				holidays.add(new Holiday(new PersianDate(year, month, day),
						node.getTextContent()));
			}

		} catch (Exception e) {
			// forget it, do nothing
		}
	}

	static public String getHolidayTitle(PersianDate day) {
		for (Holiday holiday : holidays) {
			if (holiday.getDate().equals(day)) {
				return holiday.getTitle();
			}
		}
		return null;
	}
}