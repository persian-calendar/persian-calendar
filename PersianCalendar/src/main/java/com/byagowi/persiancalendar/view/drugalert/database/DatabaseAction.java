package com.byagowi.persiancalendar.view.drugalert.database;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public interface DatabaseAction<T> {
	long insert(T type);
	void update(T type);
	void remove(long id);
	T get(long id);
	T[] getAll();
}
