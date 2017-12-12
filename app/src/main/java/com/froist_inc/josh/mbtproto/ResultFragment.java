package com.froist_inc.josh.mbtproto;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class ResultFragment extends ListFragment
{
    @Override
    public void onCreate( @Nullable Bundle saved_instance_state )
    {
        super.onCreate( saved_instance_state );
        getActivity().setTitle( "My results" );
        GetResultFromServer();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        setEmptyText( "No result" );
    }

    private void GetResultFromServer()
    {
        ResultAsyncTask result_task = new ResultAsyncTask();
        result_task.execute( Utilities.Endpoints.result_url );
    }

    private class ResultListAdapter extends ArrayAdapter<Utilities.ResultData>
    {
        ResultListAdapter( ArrayList<Utilities.ResultData> results )
        {
            super( getActivity(), 0, results );
        }

        @Override
        public View getView( int position, View convert_view, ViewGroup parent )
        {
            if( convert_view == null ){
                convert_view = getActivity().getLayoutInflater().inflate( R.layout.result_fragment, parent, false );
            }
            Utilities.ResultData data = getItem( position );

            TextView course_name_text = ( TextView ) convert_view.findViewById( R.id.result_course_name_text );
            TextView date_taken_text = ( TextView ) convert_view.findViewById( R.id.result_date_taken_text );
            TextView score_text = ( TextView ) convert_view.findViewById( R.id.result_score_text );
            course_name_text.setText( data.course_name );
            date_taken_text.setText( data.date_taken );
            score_text.setText( getString( R.string.score, data.score, data.total_score ) );

            return convert_view;
        }
    }

    private class ResultAsyncTask extends AsyncTask<String, Void, ArrayList<Utilities.ResultData>>
    {
        String error_message;
        @Override
        protected ArrayList<Utilities.ResultData> doInBackground( String... params )
        {
            final String url = params[0];
            try {
                final byte[] network_data = NetworkManager.GetNetwork().GetData( url );
                final JSONObject network_response = new JsonParser().ParseObject( network_data );
                if( network_response == null ){
                    return null;
                }
                if( network_response.has( "status" ) && network_response.getInt( "status" ) == Utilities.Error ){
                    error_message = network_response.getString( "detail" );
                    return null;
                }
                JSONArray result_array = network_response.getJSONArray( "detail" );
                ArrayList<Utilities.ResultData> result_list = new ArrayList<>();
                for( int i = 0; i != result_array.length(); ++i ){
                    JSONObject result_object = result_array.getJSONObject( i );
                    Utilities.ResultData result = new Utilities.ResultData();
                    result.score = result_object.getInt( "score" );
                    result.total_score = result_object.getInt( "total" );
                    result.course_name = result_object.getString( "name" );
                    result.administrator = result_object.getString( "owner" );
                    result.course_code = result_object.getString( "code" );
                    result.date_taken = result_object.getString( "date" );
                    result_list.add( result );
                }
                return result_list;
            } catch ( JSONException | IOException except ){
                error_message = except.getLocalizedMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute( ArrayList<Utilities.ResultData> results ) {
            if( results == null ){
                AlertDialog error_dialog = new AlertDialog.Builder( ResultFragment.this.getContext() )
                        .setTitle( "Error" ).setMessage( error_message ).setPositiveButton( android.R.string.ok, null )
                        .create();
                error_dialog.show();
                return;
            }
            setListAdapter( new ResultListAdapter( results ) );
            (( ResultListAdapter ) getListAdapter() ).notifyDataSetChanged();
        }
    }
}
