package com.froist_inc.josh.mbtproto;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LayoutQuestionViewpagerFragment extends Fragment
{
    private static final String PAPER_EXTRA_INDEX = "EXTRA_INDEX";
    private Fragment current_fragment;

    private ViewPager root_view_pager;
    private View loading_view;

    private int subject_index;
    private int paper_duration_minutes;
    private int current_question_displayed;
    private String course_owner;
    private String course_code;
    private String course_name;

    private CountDownTimer timer;
    private MenuItem    forfeit_menu_item;
    private MenuItem    resubmit_menu_item;

    private SubjectProcessingTask background_task;

    @Override
    public void onCreate( @Nullable Bundle saved_instance_state )
    {
        super.onCreate( saved_instance_state );
        subject_index = getArguments().getInt( PAPER_EXTRA_INDEX );
        setHasOptionsMenu( true );
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            saved_instance_state )
    {
        View root_view = inflater.inflate( R.layout.layout_exam_viewpager, container, false );
        root_view_pager = ( ViewPager ) root_view.findViewById( R.id.question_view_pager );
        loading_view = root_view.findViewById( R.id.question_chooser_layout );
        loading_view.setVisibility( View.VISIBLE );

        background_task = new SubjectProcessingTask();
        background_task.execute();
        return root_view;
    }

    private void InitializeViewPagerAdapter( final int count )
    {
        root_view_pager.setAdapter( new FragmentStatePagerAdapter( getChildFragmentManager() ) {
            @Override
            public Fragment getItem( int position )
            {
                current_question_displayed = position;
                current_fragment = QuestionDisplayFragment.NewInstance( current_question_displayed );
                return current_fragment;
            }

            @Override
            public int getCount()
            {
                return count;
            }
        });
    }

    public void StopTimer()
    {
        if( timer != null ) {
            timer.cancel();
            timer = null;
        }
        if( background_task != null ){
            background_task.cancel( true );
            background_task = null;
        }
    }

    private class SubjectProcessingTask extends AsyncTask<Void, Void, ArrayList<QuestionManager> >
    {
        @Override
        protected ArrayList<QuestionManager> doInBackground( Void... params )
        {
            if( subject_index == -1 ) return null;
            SubjectInformation subject_information = SubjectsLaboratory.Get( getActivity() )
                    .GetSubjectItem( subject_index );
            course_code = subject_information.GetSubjectCode();
            course_owner = subject_information.GetCourseOwner();
            course_name = subject_information.GetSubjectName();
            paper_duration_minutes = subject_information.GetDuration();

            try {
                final JSONArray question_list = subject_information.GetQuestionData().getJSONArray( "items" );
                ArrayList<QuestionManager> questions = new ArrayList<>();
                for( int i = 0; i != question_list.length(); ++i ){
                    JSONObject question = question_list.getJSONObject( i );
                    QuestionManager question_manager = new QuestionManager();
                    question_manager.SetQuestion( question.getString( "question" ) );

                    JSONArray available_options = question.getJSONObject( "options" ).getJSONArray( "items" );

                    for( int x = 0; x != available_options.length(); ++x ){
                        question_manager.AddOptions( available_options.getString( x ) );
                    }
                    if( question.has( "url" )){
                        question_manager.SetExternalAddress( question.getString( "url" ) );
                    }
                    questions.add( question_manager );
                }

                //noinspection StatementWithEmptyBody
                if( subject_information.RandomizingQuestion() ){
                    // TODO: 25-Aug-17 Randomize question
                }
                return questions;
            } catch ( JSONException exception ) {
                exception.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute( ArrayList<QuestionManager> questions )
        {
            StartExamActivity.SetQuestionList( questions );
            UpdateMainUIView();
        }
    }
    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        super.onCreateOptionsMenu( menu, inflater );
        inflater.inflate( R.menu.question_page_menu, menu );
        forfeit_menu_item = menu.findItem( R.id.menu_question_page_forfeit );
        resubmit_menu_item = menu.findItem( R.id.menu_question_page_resubmit );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch ( item.getItemId() ){
            case R.id.menu_question_page_forfeit:case android.R.id.home:
                ProcessForfeiting();
                return true;
            case R.id.menu_question_page_resubmit:
                DoFinalSubmission();
                return true;
            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void DoFinalSubmission()
    {
        if( timer != null ) timer.cancel();

        forfeit_menu_item.setEnabled( false );
        resubmit_menu_item.setEnabled( false );

        Toast.makeText( this.getContext(), R.string.submitting_text, Toast.LENGTH_LONG ).show();
        final String reply_to_url = SubjectsLaboratory.Get( getActivity() )
                .GetSubjectItem( subject_index ).GetReplyUrl();
        loading_view.setVisibility( View.VISIBLE );
        final Handler ui_thread = new Handler( getContext().getMainLooper() );

        final Utilities.BackgroundSubmissionThread result_submission_thread =
                new Utilities.BackgroundSubmissionThread( reply_to_url, ui_thread );
        result_submission_thread.SetContext( this.getContext() );
        try {
            final JSONObject data = StartExamActivity.GetAnswers( reply_to_url, course_owner, course_code, course_name );
            result_submission_thread.SetData( data );
        } catch ( JSONException except ){
            Toast.makeText( this.getContext(), "Unable to accumulate your answers", Toast.LENGTH_SHORT ).show();
            LeaveActivity();
        }
        result_submission_thread.SetCallback( new Utilities.IExamSubmissionListener(){
            @Override
            public void OnErrorOccurred( final JSONObject data) {
                Utilities.CacheDataForFailure( LayoutQuestionViewpagerFragment.this.getContext(), data );
            }

            @Override
            public void OnSuccess() {
            }

            @Override
            public void OnErrorCompletion( final JSONObject data, final String message)
            {
                OnErrorOccurred( data );
                ShowErrorAndExit( ui_thread, message );
            }

            @Override
            public void OnExit()
            {
                ui_thread.post(new Runnable() {
                    @Override
                    public void run() {
                        LeaveActivity();
                    }
                });
            }
        });
        result_submission_thread.start();
    }

    private void ShowErrorAndExit( final Handler ui_handler, final String message )
    {
        ui_handler.post( new Runnable() {
            @Override
            public void run() {
                Toast.makeText( LayoutQuestionViewpagerFragment.this.getContext(), message, Toast.LENGTH_LONG ).show();
                LeaveActivity();
            }
        });
    }

    private void ProcessForfeiting()
    {
        final AlertDialog exit_dialog = new AlertDialog.Builder( this.getContext() )
                .setTitle( android.R.string.dialog_alert_title )
                .setMessage( R.string.submit_prompt )
                .setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        LeaveActivity();
                    }
                }).setNegativeButton( android.R.string.cancel, null ).create();
        exit_dialog.show();
    }

    private void LeaveActivity()
    {
        StopTimer();
        if( NavUtils.getParentActivityName( getActivity() ) != null ){
            NavUtils.navigateUpFromSameTask( getActivity() );
        }
    }

    private void UpdateMainUIView()
    {
        final ArrayList<QuestionManager> question_list = StartExamActivity.GetQuestionList();
        if( question_list == null || question_list.size() < 1 ){
            Toast.makeText( getActivity(), "Could not process questions, please contact your administrator",
                    Toast.LENGTH_LONG ).show();
            LeaveActivity();
            return;
        }

        InitializeViewPagerAdapter( question_list.size() );
        loading_view.setVisibility( View.INVISIBLE );

        timer = new CountDownTimer( paper_duration_minutes * 60_000, 1_000 ) {
            @Override
            public void onTick( long millis_until_finished ) {
                final String time = String.format( Locale.US, "%d H, %d M, %d",
                        TimeUnit.MILLISECONDS.toHours( millis_until_finished ),
                        TimeUnit.MILLISECONDS.toMinutes( millis_until_finished ) % 60 ,
                        ( millis_until_finished / 1000 ) % 60  );
                // // TODO: 07-Oct-17 Revisit this title setting
                Activity hosting_activity = getActivity();
                if( hosting_activity != null ) {
                    getActivity().setTitle(time);
                }
            }

            @Override
            public void onFinish() {
                if( getActivity() != null ) {
                    DoFinalSubmission();
                }
            }
        };
        timer.start();
    }

    public static Fragment NewInstance( final int paper_index )
    {
        Bundle bundle = new Bundle();
        bundle.putInt( PAPER_EXTRA_INDEX, paper_index );
        Fragment display_fragment = new LayoutQuestionViewpagerFragment();
        display_fragment.setArguments( bundle );
        return display_fragment;
    }
}
