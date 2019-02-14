package com.byagowi.persiancalendar.view.reminder.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import dagger.android.support.DaggerFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.view.reminder.constants.Constants;
import com.byagowi.persiancalendar.view.reminder.database.DatabaseManager;
import com.byagowi.persiancalendar.view.reminder.model.ReminderAdapter;
import com.byagowi.persiancalendar.view.reminder.model.ReminderDetails;

import java.util.Objects;

import javax.inject.Inject;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class ReminderFragment extends DaggerFragment {

	@Inject
	MainActivityDependency mainActivityDependency;
	private ListView listView;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mainActivityDependency.getMainActivity().setTitleAndSubtitle(
				getString(R.string.reminder), "");

		setHasOptionsMenu(true);
		View view = inflater.inflate(R.layout.fragment_reminder, container, false);


		listView = view.findViewById(R.id.list);
		listView.setOnItemClickListener((parent, view1, position, id) -> {
			listView.setVisibility(View.GONE);
			EditReminderFragment toEditEvent = new EditReminderFragment();
			Bundle args = new Bundle();
			args.putLong(Constants.EVENT_ID, id);
			toEditEvent.setArguments(args);
			FragmentManager EventFragmentMgr = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
			EventFragmentMgr.beginTransaction()
					.replace(R.id.fragment_holder_reminder, toEditEvent)
					.addToBackStack(null)
					.commit();
		});
		return  view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.reminder_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add:
				listView.setVisibility(View.GONE);
				Fragment EventFragment = new EditReminderFragment();
				FragmentManager EventFragmentMgr = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
				EventFragmentMgr.beginTransaction()
						.replace(R.id.fragment_holder_reminder, EventFragment)
						.addToBackStack(null)
						.commit();
				break;
			default:
				break;
		}
		return true;
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		refresh();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	private void refresh() {
		DatabaseManager databaseManager = new DatabaseManager(getActivity());
		ReminderDetails[] data = databaseManager.getAllEvents();
		if (data == null)
			data = new ReminderDetails[0];
		ReminderAdapter adapter = new ReminderAdapter(getActivity(), R.layout.reminder_adapter_item, data);
		listView.setAdapter(adapter);
	}

}
