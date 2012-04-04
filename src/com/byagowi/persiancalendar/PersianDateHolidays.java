/*
 * March 2012
 *
 * In place of a legal notice, here is a blessing:
 *
 *    May you do good and not evil.
 *    May you find forgiveness for yourself and forgive others.
 *    May you share freely, never taking more than you give.
 *
 */
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

import com.byagowi.common.IterableNodeList;

import android.util.Log;

import calendar.PersianDate;

/**
 * Holidays repository.
 * 
 * @author ebraminio
 * 
 */
public class PersianDateHolidays {

	private static List<Holiday> holidays;

	static public void loadHolidays(InputStream xmlStream) {
		holidays = new ArrayList<Holiday>();
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			Document document = builder.parse(xmlStream);

			NodeList holidaysNodes = document.getElementsByTagName("holiday");
			for (Node node : new IterableNodeList(holidaysNodes)) {
				NamedNodeMap attrs = node.getAttributes();

				int year = Integer.parseInt(attrs.getNamedItem("year")
						.getNodeValue());
				int month = Integer.parseInt(attrs.getNamedItem("month")
						.getNodeValue());
				int day = Integer.parseInt(attrs.getNamedItem("day")
						.getNodeValue());

				String holidayTitle = node.getFirstChild().getNodeValue();

				holidays.add(new Holiday(new PersianDate(year, month, day),
						holidayTitle));
			}

		} catch (Exception e) {
			Log.e("com.byagowi.persiancalendar", e.getMessage());
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