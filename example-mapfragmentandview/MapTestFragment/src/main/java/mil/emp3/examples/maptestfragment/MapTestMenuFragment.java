package mil.emp3.examples.maptestfragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapTestMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapTestMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapTestMenuFragment extends Fragment {
    private static String TAG = MapTestMenuFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnTestSelectedListener mListener;
    private List<MenuItem> items = new ArrayList<>();

    private LinearLayout userActions;
    private MoreHandler moreHandler;

    public MapTestMenuFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapTestMenuFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapTestMenuFragment newInstance(String param1, String param2) {
        MapTestMenuFragment fragment = new MapTestMenuFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
    }

    void toggleButtonSensitivity(boolean action) {
        if(null != items) {
            for(MenuItem item : items) {
                item.setEnabled(action);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_map_test_menu, container, false);

        userActions = (LinearLayout) view.findViewById(R.id.UserActions);
        for(int ii = 0; ii < userActions.getChildCount(); ii++) {
            final Button button = (Button) userActions.getChildAt(ii);

            if(button.getId() == R.id.More) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(null != moreHandler) {
                            moreHandler.showMenu(MapTestMenuFragment.this.getActivity(), button);
                        }
                    }
                });
            } else {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "UserAction " + ((Button) v).getText());
                        if (null != mListener) {
                            mListener.onUserAction(((Button) v).getText().toString());
                            toggleButtonSensitivity(false);
                        }
                    }
                });
            }
        }

        setHasOptionsMenu(true);
        return view;
    }

    public void updateSupportedUserActions(String []supportedUserActions, String[] moreUserActions) {

        if(null != supportedUserActions) {
            for (int ii = 0; ii < supportedUserActions.length; ii++) {
                if (ii < (userActions.getChildCount() - 1)) {
                    Button button = (Button) userActions.getChildAt(ii);
                    button.setText(supportedUserActions[ii]);
                    button.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "No more buttons left " + supportedUserActions[ii]);
                    throw new IllegalStateException();
                }
            }
        }

        if(null != moreUserActions) {
            moreHandler = new MoreHandler(moreUserActions);
            Button button = (Button) userActions.getChildAt(userActions.getChildCount()-1);
            button.setVisibility(View.VISIBLE);
        }
    }

    public void disableUserActions() {
        for(int ii = 0; ii < userActions.getChildCount(); ii++) {
            Button button = (Button) userActions.getChildAt(ii);
            button.setVisibility(View.INVISIBLE);
        }
        moreHandler = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTestSelectedListener) {
            mListener = (OnTestSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTestSelectedListener");
        }
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnTestSelectedListener) {
            mListener = (OnTestSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTestSelectedListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTestSelectedListener {
        // TODO: Update argument type and name
        void onTestSelected(String selectedTest);
        void onUserAction(String userAction);
    }

    public void testComplete(String testCompleted) {
        Log.d(TAG, "testCompleted " + testCompleted);
        if(this.getActivity() != null) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toggleButtonSensitivity(true);
                    disableUserActions();
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.action_menu, menu);
        for(int ii = 0; ii < menu.size(); ii++) {
            items.add(menu.getItem(ii));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        System.err.println("onOptionsItemSelected item.getTitle() " + item.getTitle());

        Log.d(TAG, "Execute " + item.getTitle());
        if(null != mListener) {
            mListener.onTestSelected(item.getTitle().toString());
            toggleButtonSensitivity(false);
            return true;
        }

        return false;
    }

    class MoreHandler implements PopupMenu.OnMenuItemClickListener {
        private String[] menuItems;
        MoreHandler(String[] actions) {
            this.menuItems = actions;
        }

        void showMenu(Context context, View v) {
            PopupMenu popup = new PopupMenu(context, v);

            // This activity implements OnMenuItemClickListener
            popup.setOnMenuItemClickListener(this);

            for(int ii = 0; ii < menuItems.length; ii++) {
                popup.getMenu().add(menuItems[ii]);
            }
            popup.show();
        }
        @Override
        public boolean onMenuItemClick(MenuItem item) {

            if (null != mListener) {
                mListener.onUserAction(item.getTitle().toString());
                toggleButtonSensitivity(false);
            }
            return true;
        }
    }
}
