package com.byagowi.persiancalendar.view.drugalert.fragment;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.drugalert.application.DrugAlertApplication;
import com.byagowi.persiancalendar.view.drugalert.constants.Constants;
import com.byagowi.persiancalendar.view.drugalert.model.DrugAdapter;
import com.byagowi.persiancalendar.view.drugalert.model.DrugDetails;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DrugFragment extends Fragment {

	private DrugAlertApplication application;
	private ListView listView;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		View view = inflater.inflate(R.layout.fragment_drug, container, false);

		application = (DrugAlertApplication) getActivity().getApplication();
		
		listView = view.findViewById(R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				EditDrugFragment toEditEvent = new EditDrugFragment();
				Bundle args = new Bundle();
				args.putLong(Constants.EVENT_ID, id);
				toEditEvent.setArguments(args);
				FragmentManager EventFragmentMgr = getActivity().getSupportFragmentManager();
				EventFragmentMgr.beginTransaction()
						.replace(R.id.fragment_holder_drug, toEditEvent)
						.addToBackStack(null)
						.commit();
			}
		});
		return  view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.drug_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add:
				Fragment EventFragment = new EditDrugFragment();
				FragmentManager EventFragmentMgr = getActivity().getSupportFragmentManager();
				EventFragmentMgr.beginTransaction()
						.replace(R.id.fragment_holder_drug, EventFragment)
						.addToBackStack(null)
						.commit();
				break;
			default:
				break;
		}
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
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

	public void refresh() {
		DrugDetails[] data = application.getDatabaseManager().getAllEvents();
		if (data == null)
			data = new DrugDetails[0];
		DrugAdapter adapter = new DrugAdapter(getActivity(), R.layout.fragment_item, data);
		listView.setAdapter(adapter);
	}

}
