
package edu.vanderbilt.vm.guide.ui;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import edu.vanderbilt.vm.guide.R;
import edu.vanderbilt.vm.guide.db.GuideDBConstants;
import edu.vanderbilt.vm.guide.db.GuideDBOpenHelper;
import edu.vanderbilt.vm.guide.ui.adapter.TourAdapter;

@SuppressLint("NewApi")
public class TourFragment extends Fragment {

    private GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tour, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGridView = (GridView)getView().findViewById(R.id.tour_grid_view);

        GuideDBOpenHelper helper = new GuideDBOpenHelper(getActivity());
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] projection = {
                GuideDBConstants.TourTable.ID_COL, GuideDBConstants.TourTable.NAME_COL,
                GuideDBConstants.TourTable.ICON_LOC_COL
        };
        String orderBy = GuideDBConstants.TourTable.NAME_COL + " ASC";
        final Cursor tourCursor = db.query(GuideDBConstants.TourTable.TOUR_TABLE_NAME, projection,
                null, null, null, null, orderBy);

        mGridView.setAdapter(new TourAdapter(getActivity(), tourCursor, helper));

        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TourDetailer.open(getActivity(), id);
            }
        });
    }

}
