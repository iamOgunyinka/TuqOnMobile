package com.froist_inc.josh.mbtproto;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MostRankedCoursesFragment extends Fragment
{
    private ListView list_view_left;
    private ListView list_view_right;
    private View loading_view;
    private TextView status_text;

    int[] left_views_height;
    int[] right_views_height;

    private Handler main_handler;
    private CHandlerThread<Utilities.RankedCoursesData> background_handler;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        getActivity().setTitle( R.string.most_ranked );
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle saved_instance_state )
    {
        View main_view = inflater.inflate( R.layout.ranked_courses_fragment, container, false );
        list_view_left = (ListView) main_view.findViewById(R.id.list_view_left);
        list_view_right = (ListView) main_view.findViewById(R.id.list_view_right);

        list_view_left.setOnTouchListener(touch_listener);
        list_view_right.setOnTouchListener(touch_listener);
        list_view_left.setOnScrollListener(scroll_listener);
        list_view_right.setOnScrollListener(scroll_listener);

        list_view_right.setOnLongClickListener( long_click );
        list_view_left.setOnLongClickListener( long_click );

        View empty_view = main_view.findViewById( R.id.ranking_empty_view );
        list_view_left.setEmptyView( empty_view );

        loading_view = main_view.findViewById( R.id.ranking_loading_view );
        loading_view.setVisibility( View.INVISIBLE );
        status_text = ( TextView )loading_view.findViewById( R.id.ranking_status_report_text );

        if( main_handler == null ){
            main_handler = new Handler( this.getActivity().getMainLooper() );
        }
        if( background_handler == null ){
            background_handler = new CHandlerThread<>( getActivity(), new Handler() );
        }
        return main_view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        final ArrayList<Utilities.RankedCoursesData> ranked_courses = SubjectsLaboratory.GetRankedCourses();
        if( ranked_courses.size() == 0 ){
            LoadCourseRankingFromServer();
            ExecuteHandler();
        } else {
            FillListViews( ranked_courses );
        }
    }

    private void LoadCourseRankingFromServer()
    {
        if( Utilities.Endpoints.ranking_url == null ){
            loading_view.setVisibility( View.INVISIBLE );
            ShowMessage( "Error getting the address for course ranking" );
            return;
        }
        loading_view.setVisibility( View.VISIBLE );
        status_text.setText( R.string.loading );
        final String url = Utilities.Endpoints.ranking_url + "?limit=40";

        Utilities.LoadAddressTask loading_task = new Utilities.LoadAddressTask();
        loading_task.SetUiThreadOnPostExecuteListener( new Utilities.LoadAddressTask.MainUiThreadListener() {
            @Override
            public void UiThreadOnPostExecute( JSONObject result )
            {
                try {
                    if( result == null || result.getInt( "status" ) != Utilities.Success ){
                        final String message = result == null ? getString( R.string.error_grabbing_url ) :
                                result.getString( "detail" );
                        loading_view.setVisibility( View.INVISIBLE );
                        ShowMessage( message );
                        return;
                    }
                    final JSONArray success_response_data = result.getJSONArray( "detail" );
                    ArrayList<Utilities.RankedCoursesData> data = new ArrayList<>();
                    for( int i = 0; i != success_response_data.length(); ++i ){
                        final JSONObject data_item = success_response_data.getJSONObject( i );
                        final String paper_title = data_item.getString( "paper_name" );
                        final String course_id = data_item.getString( "id" );
                        final String icon_url = data_item.optString( "icon" );
                        final String owner = data_item.optString( "owner" );

                        final String data_url = Utilities.Endpoints.course_info_url + "?course_token=" + course_id +
                                "&owner=" + owner;
                        Utilities.RankedCoursesData course_data = new Utilities.RankedCoursesData( paper_title,
                                course_id, data_url );
                        course_data.SetIconAddress( icon_url );
                        data.add( course_data );
                    }
                    SubjectsLaboratory.SetRankedCourses( data );
                    UpdateMainUi();
                } catch ( JSONException except ){
                    Log.v( "MostRankedCourseFrag", except.getLocalizedMessage() );
                    ShowMessage( "Unable to parse result" );
                }
            }
        });
        loading_task.execute( url );
    }

    private void ExecuteHandler()
    {
        if( background_handler == null ){
            background_handler = new CHandlerThread<>( getActivity(), new Handler() );
        }
        background_handler.setListener( new CHandlerThread.Listener() {
            @Override
            public void OnAllTasksCompleted() {
                main_handler.post(new Runnable() {
                    @Override
                    public void run() {
                        FillListViews( SubjectsLaboratory.GetRankedCourses() );
                    }
                });
            }
        });

        if( !background_handler.isAlive() ){
            background_handler.start();
            background_handler.getLooper();
        }
    }

    void ShowMessage( final String message )
    {
        loading_view.setVisibility( View.INVISIBLE );
        new AlertDialog.Builder( MostRankedCoursesFragment.this.getContext() )
                .setTitle(android.R.string.dialog_alert_title ).setMessage( message )
                .setPositiveButton( android.R.string.ok, null ).create().show();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if( background_handler != null ){
            background_handler.quit();
            background_handler = null;
        }
    }

    private void UpdateMainUi()
    {
        final ArrayList<Utilities.RankedCoursesData> ranked_courses = SubjectsLaboratory.GetRankedCourses();
        if( ranked_courses.size() == 0 ){
            ShowMessage( getString( R.string.no_ranking ) );
            return;
        }
        if( background_handler != null ){
            for( int i = 0; i != ranked_courses.size(); ++i ){
                background_handler.Prepare( ranked_courses.get( i ) );
            }
        }
    }

    private void FillListViews( final ArrayList<Utilities.RankedCoursesData> ranked_courses )
    {
        ArrayList<Utilities.RankedCoursesData> second_half = new ArrayList<>();
        final int size = ranked_courses.size(), half = size / 2;

        list_view_left.setVisibility( View.VISIBLE );
        list_view_right.setVisibility( View.VISIBLE );

        if ( size >= 2 ) {
            ArrayList<Utilities.RankedCoursesData> first_half = new ArrayList<>();
            for( int index = 0; index != half; ++index ) first_half.add( ranked_courses.get( index ) );
            for( int index = half; half != size; ++index ) second_half.add( ranked_courses.get( index ) );

            list_view_left.setAdapter( new RankedCoursesAdapter( this.getActivity(), R.layout.ranking_item_layout,
                    first_half ) );
        } else if( size == 1 ){
            list_view_left.setAdapter( new RankedCoursesAdapter( this.getActivity(), R.layout.ranking_item_layout,
                    ranked_courses ) );
        }
        list_view_right.setAdapter( new RankedCoursesAdapter( getActivity(), R.layout.ranking_item_layout,
                second_half ));
        left_views_height = new int[size >= 2 ? half : size];
        if( second_half.size() > 0 ) {
            right_views_height = new int[second_half.size()];
        } else {
            list_view_right.setVisibility( View.INVISIBLE );
            right_views_height = null;
        }
        loading_view.setVisibility( View.INVISIBLE );
    }

    View.OnLongClickListener long_click = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick( View v )
        {
            return false;
        }
    };

    // Passing the touch event to the opposite list
    View.OnTouchListener touch_listener = new View.OnTouchListener()
    {
        boolean dispatched = false;

        @Override
        public boolean onTouch( View v, MotionEvent event )
        {
            if (v.equals(list_view_left) && !dispatched) {
                dispatched = true;
                list_view_right.dispatchTouchEvent(event);
            } else if (v.equals(list_view_right) && !dispatched) {
                dispatched = true;
                list_view_left.dispatchTouchEvent(event);
            }

            dispatched = false;
            return false;
        }
    };

    /**
     * Synchronizing scrolling
     * Distance from the top of the first visible element opposite list:
     * sum_heights(opposite invisible screens) - sum_heights(invisible screens) + distance from top of the first visible child
     */
    AbsListView.OnScrollListener scroll_listener = new AbsListView.OnScrollListener()
    {
        @Override
        public void onScrollStateChanged(AbsListView v, int scrollState) {
        }

        @Override
        public void onScroll( AbsListView view, int first_visible_item,
                             int visible_item_count, int total_item_count ) {

            if (view.getChildAt(0) != null) {
                if (view.equals(list_view_left) ){
                    left_views_height[view.getFirstVisiblePosition()] = view.getChildAt(0).getHeight();

                    int h = 0;
                    for (int i = 0; i < list_view_right.getFirstVisiblePosition(); i++) {
                        h += right_views_height[i];
                    }

                    int hi = 0;
                    for (int i = 0; i < list_view_left.getFirstVisiblePosition(); i++) {
                        hi += left_views_height[i];
                    }

                    int top = h - hi + view.getChildAt(0).getTop();
                    list_view_right.setSelectionFromTop(list_view_right.getFirstVisiblePosition(), top);
                } else if (view.equals(list_view_right)) {
                    right_views_height[view.getFirstVisiblePosition()] = view.getChildAt(0).getHeight();

                    int h = 0;
                    for (int i = 0; i < list_view_left.getFirstVisiblePosition(); i++) {
                        h += left_views_height[i];
                    }

                    int hi = 0;
                    for (int i = 0; i < list_view_right.getFirstVisiblePosition(); i++) {
                        hi += right_views_height[i];
                    }

                    int top = h - hi + view.getChildAt(0).getTop();
                    list_view_left.setSelectionFromTop(list_view_left.getFirstVisiblePosition(), top);
                }

            }

        }
    };
}
