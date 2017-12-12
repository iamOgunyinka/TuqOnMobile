package com.froist_inc.josh.mbtproto;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class StartExamActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private static ArrayList<QuestionManager> question_list;
    private int paper_index;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected( MenuItem item ) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = ( DrawerLayout ) findViewById( R.id.start_exam_drawer );
        assert drawer != null;

        drawer.closeDrawer( GravityCompat.START );
        return true;
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = ( DrawerLayout ) findViewById( R.id.start_exam_drawer );

        if ( drawer != null && drawer.isDrawerOpen( GravityCompat.START )) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate( Bundle saved_instance_state ) {
        super.onCreate( saved_instance_state );
        setContentView( R.layout.start_exam_activity );

        Toolbar toolbar = ( Toolbar ) findViewById( R.id.start_exam_toolbar );
        setSupportActionBar( toolbar );

        DrawerLayout drawer_layout = ( DrawerLayout ) findViewById( R.id.start_exam_drawer );
        ActionBarDrawerToggle drawer_toggle = new ActionBarDrawerToggle( this, drawer_layout, toolbar,
                android.R.string.copy, android.R.string.paste );

        assert drawer_layout != null;
        drawer_layout.addDrawerListener( drawer_toggle );
        drawer_toggle.syncState();

        NavigationView navigationView = ( NavigationView ) findViewById( R.id.nav_start_exam_view );
        assert navigationView != null;

        navigationView.setNavigationItemSelectedListener( this );
        paper_index = this.getIntent().getIntExtra( SubjectPresenterFragment.PAPER_INDEX_TAG, 0 );

        Button start_exam_button = ( Button ) findViewById( R.id.start_exam_button );
        assert start_exam_button != null;
        start_exam_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Intent intent = new Intent( StartExamActivity.this, LayoutExamActivity.class );
                intent.putExtra( SubjectPresenterFragment.PAPER_INDEX_TAG, paper_index );
                startActivity( intent );
                overridePendingTransition( R.anim.push_left_in, R.anim.push_left_out );
            }
        });
        TextView instruction_text_view = ( TextView ) findViewById( R.id.start_exam_instruction_text_view );
        TextView instructor_name_text_view = ( TextView ) findViewById( R.id.start_exam_instructor_text_view );
        TextView departments_text_view = ( TextView ) findViewById( R.id.start_exam_departments );

        assert instruction_text_view != null;
        assert instructor_name_text_view != null;
        assert departments_text_view != null;

        try {
            SubjectInformation info = SubjectsLaboratory.Get( this.getApplicationContext() ).GetSubjectItem( paper_index );
            final String instruction = info.GetQuestionData().getString( "instructions" ),
                    instructor_name = info.GetInstructor() == null ? "No instructor specified" : info.GetInstructor();
            StringBuilder buffer = new StringBuilder();
            ArrayList<String> departments = info.GetDepartments();
            for( int i = 0; i != departments.size(); ++i ){
                if( i != departments.size() - 1 ){
                    buffer.append( departments.get( i ) ).append( ", " );
                } else {
                    buffer.append( departments.get( i ) );
                }
            }
            departments_text_view.setText( buffer.toString() );
            instruction_text_view.setText( getString( R.string.exam_instructions, instruction ) );
            instructor_name_text_view.setText( instructor_name );
        } catch ( JSONException except ){
            instruction_text_view.setText( getString( R.string.exam_instructions, "No instructions available" ) );
            except.printStackTrace();
        }
    }

    public static ArrayList<QuestionManager> GetQuestionList()
    {
        return question_list;
    }

    public static void SetQuestionList( ArrayList<QuestionManager> question_manager )
    {
        question_list = question_manager;
    }

    public static JSONObject GetAnswers( final String reply_to_url,
                                         final String owner,
                                         final String course_code,
                                         final String course_name ) throws JSONException
    {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get( Calendar.YEAR ), month = calendar.get( Calendar.MONTH ),
                day = calendar.get( Calendar.DAY_OF_MONTH );

        JSONObject root = new JSONObject();
        
        root.put( "owner", owner );
        root.put( "course_id", course_code );
        root.put( "course_name", course_name );
        root.put( "reply_to", reply_to_url );
        root.put( "date_taken", String.format( Locale.US, "%d-%d-%d", year, month, day ) );
        JSONArray answers = new JSONArray();
        final ArrayList<QuestionManager> question_list = StartExamActivity.GetQuestionList();
        for( int i = 0; i != question_list.size(); ++i ){
            final int get_chosen_option = question_list.get( i ).GetChosenOption();
            answers.put( get_chosen_option );
        }
        root.put( "answers", answers );
        return root;
    }
}
