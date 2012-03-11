package com.byagowi.persiancalendar;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import calendar.PersianDate;

public class PersianDateHolidays {
	static class Holiday {
		private PersianDate date;
		private String title;

		public PersianDate getDate() {
			return date;
		}

		public void setDate(PersianDate date) {
			this.date = date;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public Holiday(PersianDate date, String title) {
			this.date = date;
			this.title = title;
		}
	}

	// please see this gist: https://gist.github.com/2016752
	static private List<Holiday> holidays = Arrays
			.asList(new Holiday[] {
					new Holiday(new PersianDate(1390, 1, 1), "آغاز نوروز"),
					new Holiday(new PersianDate(1390, 1, 2), "عید نوروز"),
					new Holiday(new PersianDate(1390, 1, 3), "عید نوروز"),
					new Holiday(new PersianDate(1390, 1, 4), "عید نوروز"),
					new Holiday(new PersianDate(1390, 1, 12),
							"روز جمهوری اسلامی ایران"),
					new Holiday(new PersianDate(1390, 1, 13), "روز طبیعت"),
					new Holiday(new PersianDate(1390, 2, 17),
							"۳ جمادی الثانیة ۱۴۳۲ شهادت حضرت فاطمه زهرا سلام الله علیها (۱۱ ه ق)"),
					new Holiday(
							new PersianDate(1390, 3, 14),
							"رحلت حضرت امام خمینی (ره) رهبر کبیر انقلاب و بنیانگذار جمهوری اسلامی ایران (۱۳۶۸ ه ش)"),
					new Holiday(new PersianDate(1390, 3, 15),
							"قیام خونین ۱۵ خرداد (۱۳۴۲ ه ش)"),
					new Holiday(new PersianDate(1390, 3, 26),
							"خرداد ۱۳ رجب ۱۴۳۲ولادت حضرت امام علی علیه السلام (۲۳ سال قبل از هجرت)"),
					new Holiday(new PersianDate(1390, 4, 9),
							"۲۷ رجب ۱۴۳۲مبعث حضرت رسول اکرم صلی الله علیه و آله (۱۳سال قبل از هجرت)"),
					new Holiday(new PersianDate(1390, 4, 26),
							"۱۵ شعبان ۱۴۳۲ ولادت حضرت قائم عجل الله تعالی فرجه ( ۲۵۵ ه ق)"),
					new Holiday(new PersianDate(1390, 5, 30),
							"۲۱ رمضان ۱۴۳۲ شهادت حضرت علی علیه السلام ( ۴۰ ه ق)"),
					new Holiday(new PersianDate(1390, 6, 9),
							"۱ شوال ۱۴۳۲ عید سعید فطر"),
					new Holiday(new PersianDate(1390, 7, 2),
							"۲۵ شوال ۱۴۳۲شهادت حضرت امام جعفر صادق علیه السلام (۱۴۸ه ق)"),
					new Holiday(new PersianDate(1390, 8, 16),
							"۱۰ ذی الحجه ۱۴۳۲ عید سعید قربان"),
					new Holiday(new PersianDate(1390, 8, 24),
							"۱۸ ذی الحجه ۱۴۳۲ عید سعید غدیر خم (۱۰ ه ق)"),
					new Holiday(new PersianDate(1390, 9, 14),
							"۹ محرم ۱۴۳۳ تاسوعای حسینی"),
					new Holiday(new PersianDate(1390, 9, 15),
							"۱۰ محرم ۱۴۳۳ عاشورای حسینی"),
					new Holiday(new PersianDate(1390, 10, 24),
							"۲۰ صفر ۱۴۳۳ اربعین حسینی"),
					new Holiday(
							new PersianDate(1390, 11, 2),
							"۲۸ صفر ۱۴۳۳ رحلت حضرت رسول اکرم صلی الله علیه و آله (۱۱ ه ق) شهادت حضرت امام حسن مجتبی علیه السلام (۵۰ ه ق)"),
					new Holiday(new PersianDate(1390, 11, 4),
							"آخر صفر ۱۴۳۳ شهادت حضرت امام رضا علیهالسلام (۲۰۳ ه ق)"),
					new Holiday(
							new PersianDate(1390, 11, 21),
							"۱۷ ربیع الاول ۱۴۳۳ میلاد حضرت رسول اکرم صلی الله علیه و آله (۵۳سال قبل از هجرت) میلاد حضرت امام جعفر صادق علیهالسلام مؤسس مذهب جعفری ( ۸۳ ه ق)"),
					new Holiday(new PersianDate(1390, 11, 2),
							"پیروزی انقلاب اسلامی ایران و سقوط نظام شاهنشاهی (۱۳۵۷ ه ش)"),
					new Holiday(new PersianDate(1390, 12, 29),
							"روز ملی شدن صنعت نفت") });

	static public String getHolidayTitle(PersianDate day) {
		for (Holiday holiday : holidays) {
			if (holiday.getDate().equals(day)) {
				return holiday.getTitle();
			}
		}
		return null;
	}
}