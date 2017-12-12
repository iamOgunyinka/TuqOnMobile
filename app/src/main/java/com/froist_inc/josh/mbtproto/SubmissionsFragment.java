package com.froist_inc.josh.mbtproto;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class SubmissionsFragment extends ListFragment
{
    public static final String SD_FILENAME = "sd_file.dat";
    private static final String TAG = "SubmissionsFragment";
    private Handler main_ui_handler;

    @Override
    public void onCreate( @Nullable Bundle saved_instance_state )
    {
        super.onCreate( saved_instance_state );
        getActivity().setTitle( "My submission attempts" );
        ReadSubmissionAttemptFile();
    }

    private void ReadSubmissionAttemptFile()
    {
        new SubmissionAsyncTask().execute();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if( main_ui_handler == null ) main_ui_handler = new Handler( Looper.getMainLooper() );
        setEmptyText( "No failed submissions found" );
    }

    private class SubmissionListAdapter extends ArrayAdapter<Utilities.SubmissionData>
    {
        SubmissionListAdapter( ArrayList<Utilities.SubmissionData> data_list )
        {
            super( getActivity(), 0, data_list );
        }

        @Override
        public View getView( final int position, View convert_view, ViewGroup parent )
        {
            if( convert_view == null ){
                convert_view = getActivity().getLayoutInflater().inflate( R.layout.submissions_fragment, parent, false );
            }

            final JSONObject data = getItem( position ).GetData();

            TextView course_name_text = ( TextView ) convert_view.findViewById( R.id.submission_course_name_text );
            TextView date_taken_text = ( TextView ) convert_view.findViewById( R.id.submission_date_taken_text );
            final Button resend_it_button = ( Button ) convert_view.findViewById( R.id.submission_resend_button );
            resend_it_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( View v )
                {
                    OnResendButtonClicked( data, position, resend_it_button );
                }
            });

            try {
                course_name_text.setText( data.getString( "course_name" ));
            } catch( JSONException except ){
                course_name_text.setText( R.string.unknown_subject );
            }
            try {
                date_taken_text.setText( data.getString( "date_taken" ) );
            } catch ( JSONException except ){
                date_taken_text.setText( R.string.unknown_date );
            }
            return convert_view;
        }
    }

    private void OnResendButtonClicked( final JSONObject data, final int position, final Button resend_it_button )
    {
        resend_it_button.setEnabled( false );
        try {
            final String reply_to = data.getString( "reply_to" );
            Utilities.BackgroundSubmissionThread submission_thread =
                    new Utilities.BackgroundSubmissionThread( reply_to, main_ui_handler );
            submission_thread.SetData( data );
            submission_thread.SetContext( SubmissionsFragment.this.getContext() );
            submission_thread.SetCallback(new Utilities.IExamSubmissionListener() {
                @Override
                public void OnErrorOccurred( final JSONObject data ) {
                }

                @Override
                public void OnErrorCompletion( final JSONObject data, final String message ){
                    main_ui_handler.post( new Runnable() {
                        @Override
                        public void run() {
                            resend_it_button.setEnabled( true );
                            Toast.makeText( SubmissionsFragment.this.getContext(), message, Toast.LENGTH_LONG ).show();
                        }
                    });
                }

                @Override
                public void OnExit()
                {
                    main_ui_handler.post(new Runnable() {
                        @Override
                        public void run() {
                            resend_it_button.setEnabled( true );
                        }
                    });
                }

                @Override
                public void OnSuccess() {
                    main_ui_handler.post(new Runnable() {
                        @Override
                        public void run() {
                            SubmissionListAdapter adapter = ( SubmissionListAdapter ) getListAdapter();
                            adapter.remove( adapter.getItem( position ) );
                            adapter.notifyDataSetChanged();
                        }
                    });
                    // TODO: 26-Nov-17 Do actual implementation in Utilities.java
                    try {
                        Utilities.RemoveSubmissionsData(SubmissionsFragment.this.getContext(), position,
                                Utilities.UserInformation.username, SD_FILENAME);
                    } catch ( JSONException | IOException exception ){
                        Log.v( TAG, exception.getLocalizedMessage() );
                    }
                }
            });
            submission_thread.start();
        } catch ( JSONException exc ){
            Log.v( TAG, exc.getLocalizedMessage() );
        }
    }

    private class SubmissionAsyncTask extends AsyncTask<Void, Void, ArrayList<Utilities.SubmissionData>>
    {
        String error_message;
        @Override
        protected ArrayList<Utilities.SubmissionData> doInBackground( Void... params )
        {
            try {
                return Utilities.ReadSubmissionsData(
                        SubmissionsFragment.this.getContext(), SD_FILENAME, Utilities.UserInformation.username );
            } catch ( JSONException | IOException except ){
                error_message = except.getLocalizedMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute( ArrayList<Utilities.SubmissionData> results ) {
            if( results == null ){
                AlertDialog error_dialog = new AlertDialog.Builder( SubmissionsFragment.this.getContext() )
                        .setTitle( "Error" ).setMessage( error_message ).setPositiveButton( android.R.string.ok, null )
                        .create();
                error_dialog.show();
                setListAdapter( null );
                return;
            }
            setListAdapter( new SubmissionListAdapter( results ) );
            (( SubmissionListAdapter ) getListAdapter() ).notifyDataSetChanged();
        }
    }
}
