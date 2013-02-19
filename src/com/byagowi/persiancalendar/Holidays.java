package com.byagowi.persiancalendar;

import android.util.Log;
import calendar.PersianDate;
import com.byagowi.common.IterableNodeList;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Holidays repository.
 *
 * @author ebraminio
 */
class Holidays {

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

        } catch (ParserConfigurationException e) {
            Log.e("com.byagowi.persiancalendar", e.getMessage());
        } catch (SAXException e) {
            Log.e("com.byagowi.persiancalendar", e.getMessage());
        } catch (IOException e) {
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