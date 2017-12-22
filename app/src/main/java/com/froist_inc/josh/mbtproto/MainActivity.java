package com.froist_inc.josh.mbtproto;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    public static final String FILENAME = "repositories.json";
    public static final String TAG = "MainActivity";
    private int current_nav_id = 0;
    private NavigationView navigation_view;

    enum FragmentType {
        CachedFrag,
        SubjectPresFrag,
        ResultFrag,
        SubmissionsFrag,
        MostRankedFrag,
        HelpFrag,
        RoyaltyFrag
    }

    @Override
    protected void onCreate( Bundle saved_instance_state )
    {
        super.onCreate( saved_instance_state );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        DrawerLayout drawer_layout = ( DrawerLayout ) findViewById( R.id.drawer_layout );
        ActionBarDrawerToggle drawer_toggle = new ActionBarDrawerToggle( this, drawer_layout, toolbar,
                android.R.string.copy, android.R.string.paste );

        assert drawer_layout != null;
        drawer_layout.addDrawerListener( drawer_toggle );
        drawer_toggle.syncState();

        navigation_view = ( NavigationView ) findViewById( R.id.nav_view );
        assert navigation_view != null;

        navigation_view.setNavigationItemSelectedListener( this );
        navigation_view.setCheckedItem( R.id.nav_cached_repo );
        new RepositoryUpdater().execute();
        SetFragment( FragmentType.SubjectPresFrag, R.id.nav_add_repo );
    }

    private void SetFragment( FragmentType page_type, int current_nav_id )
    {
        Fragment display_fragment = null;
        switch( page_type ){
            case SubjectPresFrag:
                display_fragment = SubjectPresenterFragment.GetInstance( null );
                break;
            case CachedFrag:
                display_fragment = new CachedRepoFragment();
                break;
            case ResultFrag:
                display_fragment = new ResultFragment();
                break;
            case HelpFrag:
                display_fragment = new DisplayHelpFragment();
                break;
            case MostRankedFrag:
                display_fragment = new MostRankedCoursesFragment();
                break;
            case RoyaltyFrag:
                display_fragment = new AppreciationFragment();
                break;
            case SubmissionsFrag:
                display_fragment = new SubmissionsFragment();
                break;
            default:
                break;
        }
        PlaceFragment( display_fragment, current_nav_id );
    }

    public void PlaceFragment( Fragment display_fragment, int current_nav_id )
    {
        navigation_view.setCheckedItem( current_nav_id );
        this.current_nav_id = current_nav_id;
        FragmentManager frag_manager = getSupportFragmentManager();
        frag_manager.beginTransaction().replace( R.id.drawer_mainLayout, display_fragment ).commit();
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = ( DrawerLayout ) findViewById( R.id.drawer_layout );

        if ( drawer != null && drawer.isDrawerOpen( GravityCompat.START )) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings( "StatementWithEmptyBody" )
    @Override
    public boolean onNavigationItemSelected( MenuItem item )
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if( current_nav_id != id ){
            switch ( id ){
                case R.id.nav_add_repo:
                    SetFragment( FragmentType.SubjectPresFrag, id );
                    break;
                case R.id.nav_cached_repo:
                    SetFragment( FragmentType.CachedFrag, id );
                    break;
                case R.id.nav_result:
                    SetFragment( FragmentType.ResultFrag, id );
                    break;
                case R.id.nav_ranked_courses:
                    SetFragment( FragmentType.MostRankedFrag, id );
                    break;
                case R.id.nav_submission_error:
                    SetFragment( FragmentType.SubmissionsFrag, id );
                    break;
                case R.id.nav_help:
                    SetFragment( FragmentType.HelpFrag, id );
                    break;
                case R.id.nav_appreciation: default:
                    SetFragment( FragmentType.RoyaltyFrag, id );
                    break;
            }
        }
        DrawerLayout drawer = ( DrawerLayout ) findViewById( R.id.drawer_layout );
        assert drawer != null;
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }

    private class RepositoryUpdater extends AsyncTask<Void,Void, Integer >
    {
        String error_message;
        @Override
        protected Integer doInBackground( Void... params )
        {
            try {
                byte[] raw_data = NetworkManager.GetNetwork().GetData( Utilities.UserInformation.endpoint_link );
                JSONObject result = new JsonParser().ParseObject( raw_data );
                if( result == null ){
                    throw new IOException( "Unable to get necessary information from the server." );
                }
                boolean successful_result = result.has( "status" );
                if( !successful_result ) throw new IOException( "Invalid result obtained from server" );
                if( result.getInt( "status" ) == Utilities.Error ){
                    throw new IOException( result.getString( "detail" ) );
                }
                JSONObject endpoints = result.getJSONObject( "detail" );
                
                Utilities.Endpoints.result_url = endpoints.getString( "result" );
                Utilities.Endpoints.ranking_url = endpoints.getString( "ranking" );
                Utilities.Endpoints.course_info_url = endpoints.getString( "course_info" );
                
                return Utilities.Success; // just return something that isn't null
            } catch( JSONException | IOException except ){
                error_message = except.getLocalizedMessage();
                return Utilities.Error;
            }
        }

        @Override
        protected void onPostExecute( Integer s )
        {
            if( s == Utilities.Error ){
                Log.v( TAG, error_message );
                MainActivity.this.finish();
            }
        }
    }
}
