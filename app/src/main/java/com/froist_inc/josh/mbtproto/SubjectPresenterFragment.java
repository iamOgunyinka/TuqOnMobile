package com.froist_inc.josh.mbtproto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SubjectPresenterFragment extends Fragment
{
    private View          m_loading_view;
    private GridView      m_grid_view;
    private TextView      m_status_text_view;
    private CHandlerThread m_handler_thread = null;
    private String        current_address;
    private FloatingActionButton fab;

    private static final int     ADD_REPO_REQUEST_CODE = 1;
    private static final String  TAG = "SubjectPresenter";
    public static final String   PAPER_INDEX_TAG = "paper_index_tag";

    private Handler main_ui_handler;

    public static Fragment GetInstance( final String address )
    {
        Fragment fragment = new SubjectPresenterFragment();
        if( address != null ){
            Bundle bundle = new Bundle();
            bundle.putString( "address", address );
            fragment.setArguments( bundle );
        }
        return fragment;
    }

    @Override
    public void onCreate( @Nullable Bundle saved_instance_state )
    {
        super.onCreate( saved_instance_state );
        getActivity().setTitle( R.string.available_subjects );

        setHasOptionsMenu( true );
        Bundle args = getArguments();
        if( args != null ){
            current_address = args.getString( "address" );
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if( main_ui_handler == null ){
            main_ui_handler = new Handler( this.getActivity().getMainLooper() );
        }
        if( current_address != null ){
            GetInitializeUrl( current_address );
        } else {
            final ArrayList<SubjectInformation> subjects = SubjectsLaboratory.Get(getActivity()).GetSubjects();
            if ( subjects != null && subjects.size() != 0 ) {
                m_grid_view.setAdapter( new SubjectAdapter( subjects ) );
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable final ViewGroup container,
                              @Nullable Bundle saved_instance_state )
    {
        View layout_view = inflater.inflate( R.layout.subject_display_fragment, container, false );
        m_grid_view = ( GridView ) layout_view.findViewById( R.id.subject_choose_gridView );
        View empty_view = layout_view.findViewById( R.id.empty );
        m_grid_view.setEmptyView( empty_view );
        m_grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, final int position, long id ) {
                current_address = null;
                StartNewExam( position );
            }
        });

        m_loading_view = layout_view.findViewById( R.id.subject_chooser_layoutMain );
        m_loading_view.setVisibility( View.INVISIBLE );
        m_status_text_view = ( TextView ) m_loading_view.findViewById( R.id.status_report_text_view );

        fab = ( FloatingActionButton ) layout_view.findViewById(R.id.fab);
        assert fab != null;

        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnAddRepositoryClicked();
            }
        });

        return layout_view;
    }

    private void StartNewExam( int position )
    {
        Intent intent = new Intent( this.getContext(), StartExamActivity.class );
        intent.putExtra( PAPER_INDEX_TAG, position );
        startActivity( intent );
        this.getActivity().overridePendingTransition( R.anim.push_left_in, R.anim.push_left_out );
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        inflater.inflate( R.menu.main, menu );
        super.onCreateOptionsMenu( menu, inflater );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if( id == R.id.action_help ){
            OnDisplayHelpClicked();
            return true;
        } else if( id == R.id.action_add_repository ){
            OnAddRepositoryClicked();
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    private void OnDisplayHelpClicked()
    {
        Fragment fragment = new DisplayHelpFragment();
        ( ( MainActivity ) getActivity() ).PlaceFragment( fragment, R.id.nav_help );
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if( m_handler_thread != null ){
            m_handler_thread.quit();
            m_handler_thread = null;
        }
    }

    private class SubjectAdapter extends ArrayAdapter<SubjectInformation>
    {
        final ArrayList<SubjectInformation> m_list;

        SubjectAdapter( ArrayList<SubjectInformation> information_list )
        {
            super( getActivity(), 0, information_list );
            m_list = information_list;
            m_loading_view.setVisibility( View.INVISIBLE );
        }

        @Override
        public int getCount() {
            return m_list.size();
        }

        @Override
        public View getView( int position, View convert_view, ViewGroup parent )
        {
            SubjectInformation subject_information = m_list.get( position );
            if( convert_view == null ){
                convert_view = getActivity().getLayoutInflater().inflate( R.layout.element_layout_fragment, parent, false );
            }
            ImageView image_view = ( ImageView ) convert_view.findViewById( R.id.subject_item_imageView );
            TextView text_caption = ( TextView ) convert_view.findViewById( R.id.subject_item_captionTextView );
            text_caption.setText( subject_information.GetSubjectName() );

            if( subject_information.icon_data == null )
            {
                image_view.setImageResource( R.drawable.course );
            } else {
                Bitmap bitmap_icon = BitmapFactory.decodeByteArray( subject_information.icon_data, 0,
                        subject_information.icon_data.length );
                if( bitmap_icon != null ){
                    image_view.setImageBitmap( bitmap_icon );
                } else {
                    image_view.setImageResource( R.drawable.course );
                }
            }
            return convert_view;
        }
    }

    private void UpdateMainThreadInformation()
    {
        if( SubjectsLaboratory.Get( getActivity() ).GetSubjects().size() == 0 ) {
            AlertDialog alert = new AlertDialog.Builder( SubjectPresenterFragment.this.getContext() )
                    .setTitle( android.R.string.dialog_alert_title )
                    .setMessage( "No paper(s) was found matching the criteria or found slated for the day." )
                    .create();
            alert.show();
            m_grid_view.setAdapter( new SubjectAdapter( new ArrayList<SubjectInformation>() ) );
        } else {
            if( m_handler_thread != null ){
                for ( int i = 0; i < SubjectsLaboratory.Get( getActivity() ).GetSubjects().size(); ++i ) {
                    m_handler_thread.Prepare( SubjectsLaboratory.Get( getActivity() ).GetSubjectItem( i ));
                }
            }
        }
    }

    private void OnAddRepositoryClicked()
    {
        fab.setVisibility( View.INVISIBLE );
        FragmentManager fragment_manager = getActivity().getSupportFragmentManager();
        AddRepositoryDialog dialog = new AddRepositoryDialog();
        dialog.setTargetFragment( SubjectPresenterFragment.this, ADD_REPO_REQUEST_CODE );
        dialog.show( fragment_manager, AddRepositoryDialog.TAG );
    }

    @Override
    public void onActivityResult( int request_code, int result_code, Intent intent )
    {
        if ( result_code != Activity.RESULT_OK ){
            fab.setVisibility( View.VISIBLE );
            return;
        }
        if( request_code == ADD_REPO_REQUEST_CODE ) {
            final String address = intent.getStringExtra( AddRepositoryDialog.ADD_REPO_RESULT_DATA );
            if( address.isEmpty() ) return;
            GetInitializeUrl( address );
        }
    }

    private void GetInitializeUrl( final String address )
    {
        m_loading_view.setVisibility( View.VISIBLE );
        LoadStartupAddress( address );
        InitializeHandler();
    }

    private void LoadStartupAddress( final String address )
    {
        final Utilities.LoadAddressTask address_task = new Utilities.LoadAddressTask();
        m_status_text_view.setText( R.string.try_loading_url );
        address_task.SetUiThreadOnPostExecuteListener( new Utilities.LoadAddressTask.MainUiThreadListener() {
            @Override
            public void UiThreadOnPostExecute( JSONObject json_result ) {
                Log.v( TAG, new Date().toString() );
                if( json_result == null || !json_result.has( "url" ) ){
                    m_loading_view.setVisibility( View.INVISIBLE );
                    try {
                        final String message = json_result == null ? getString( R.string.error_grabbing_url ) :
                                json_result.getString( "detail" );
                        AlertDialog error_dialog = new AlertDialog.Builder( SubjectPresenterFragment.this.getContext() )
                                .setTitle( android.R.string.dialog_alert_title )
                                .setPositiveButton( android.R.string.ok, null )
                                .setMessage( message ).create();
                        error_dialog.show();
                    } catch ( JSONException except ){
                        except.printStackTrace();
                    }
                    fab.setVisibility( View.VISIBLE );
                    return;
                }
                try {
                    LoadQuestionsUrl( json_result.getString( "url" ), address );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        address_task.execute( address );
    }

    private void LoadQuestionsUrl( final String url, final String initial_address )
    {
        m_status_text_view.setText( R.string.try_loading_questions );
        final Utilities.LoadAddressTask task_loader = new Utilities.LoadAddressTask();
        task_loader.SetUiThreadOnPostExecuteListener(new Utilities.LoadAddressTask.MainUiThreadListener() {
            @Override
            public void UiThreadOnPostExecute(JSONObject result) {
                m_status_text_view.setText( R.string.analyze_question );
                try {
                    if ( result == null || ( ( result.has( "status" ) && result.getInt( "status" ) == Utilities.Error )))
                    {
                        m_loading_view.setVisibility( View.INVISIBLE );
                        fab.setVisibility( View.VISIBLE );

                        AlertDialog new_dialog = new AlertDialog.Builder( SubjectPresenterFragment.this.getContext() )
                                .setTitle( android.R.string.dialog_alert_title )
                                .setMessage( result == null ? "Unable to get information from the URL"
                                        : result.getString( "detail" ) )
                                .create();
                        new_dialog.show();
                    } else {
                        JSONArray array_of_questions = result.getJSONArray( "exams" );
                        ArrayList<SubjectInformation> question_info_list = new ArrayList<>();
                        for ( int i = 0; i != array_of_questions.length(); ++i ) {
                            JSONObject question_object = array_of_questions.getJSONObject( i );
                            try {
                                final String course_title = question_object.getString( "paper_name" ),
                                        course_code = question_object.getString( "paper_code" ),
                                        data_url = question_object.getString( "url" );
                                SubjectInformation info = new SubjectInformation( course_title, course_code, data_url );

                                info.SetReplyUrl( question_object.getString( "reply_to" ) );
                                info.SetRandomizingQuestion( question_object.getBoolean( "randomize" ) );
                                info.SetCourseOwner( question_object.getString( "owner" ) );
                                JSONArray departments_array = question_object.getJSONArray( "departments" );
                                ArrayList<String> departments = new ArrayList<>();
                                for ( int index = 0; index != departments_array.length(); ++index ) {
                                    departments.add( departments_array.getString( index ) );
                                }
                                info.SetDepartments( departments );
                                info.SetDurationInMinutes( question_object.getInt( "duration" ) );
                                info.SetInstructor( question_object.getString( "instructor" ) );
                                if( question_object.has( "icon_url" ) ){
                                    info.SetIconAddress( question_object.getString( "icon_url" ) );
                                }
                                question_info_list.add( info );
                            } catch ( JSONException exception ) {
                                exception.printStackTrace();
                            }
                        }
                        SubjectsLaboratory.Get( SubjectPresenterFragment.this.getContext() )
                                .SetSubjects( question_info_list );
                        if( result.has( "cacheable" ) ){
                            int size = SubjectsLaboratory.GetRepositories().size() + 1;
                            SubjectsLaboratory.AddRepository( new Pair<>( String.valueOf( size ), initial_address ) );
                        }

                        UpdateMainThreadInformation();
                    }
                } catch ( JSONException exception ){
                    exception.printStackTrace();
                }
            }
        });
        // TODO: 22-Jul-17 In the future, there should be a setting to include date-range
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get( Calendar.YEAR )
                //, month = calendar.get( Calendar.MONTH ),
                //day = calendar.get( Calendar.DAY_OF_MONTH )
                        ;
        final String start_date = "" + ( year - 1 ) + "-01-01";
        final String address = url + "&date_from=" + start_date;
        Log.v( TAG, address );
        task_loader.execute( address );
    }

    private void InitializeHandler()
    {
        if( m_handler_thread == null ){
            m_handler_thread = new CHandlerThread( getActivity(), new Handler() );
        }
        m_handler_thread.setListener( new CHandlerThread.Listener() {
            @Override
            public void OnSubjectCodeDataObtained( SubjectInformation subjectInformation )
            {
            }

            @Override
            public void OnAllTasksCompleted()
            {
                if( m_loading_view != null ){
                    main_ui_handler.post( new Runnable(){
                        @Override
                        public void run() {
                            m_grid_view.setAdapter( new SubjectAdapter( SubjectsLaboratory.Get( getActivity() ).GetSubjects() ));
                            m_loading_view.setVisibility( View.INVISIBLE );
                        }
                    });
                }
            }
        });
        if( !m_handler_thread.isAlive() ){
            m_handler_thread.start();
            m_handler_thread.getLooper();
        }
    }
}
