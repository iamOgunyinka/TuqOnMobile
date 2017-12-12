package com.froist_inc.josh.mbtproto;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class LayoutExamActivity extends AppCompatActivity
{
    Fragment current_fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.lay_out_exam_activity );

        int paper_index = getIntent().getIntExtra( SubjectPresenterFragment.PAPER_INDEX_TAG, -1 );

        Toolbar toolbar = ( Toolbar ) findViewById( R.id.question_display_toolbar );
        setSupportActionBar( toolbar );

        ActionBar action_bar = getSupportActionBar();
        assert action_bar != null;

        action_bar.setDisplayHomeAsUpEnabled( true );
        action_bar.setHomeButtonEnabled( true );
        ShowFragment( paper_index );
    }

    private void ShowFragment( int paper_index )
    {
        FragmentManager frag_manager = getSupportFragmentManager();
        current_fragment = frag_manager.findFragmentById( R.id.drawer_mainLayout );
        if( current_fragment == null ) {
            current_fragment = LayoutQuestionViewpagerFragment.NewInstance( paper_index );
            frag_manager.beginTransaction().replace( R.id.drawer_mainLayout, current_fragment ).commit();
        }
    }

    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder( this )
                .setNegativeButton( android.R.string.no, null )
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        if( NavUtils.getParentActivityName( LayoutExamActivity.this ) != null ){
                            if( current_fragment != null ) {
                                ((LayoutQuestionViewpagerFragment) current_fragment).StopTimer();
                            }
                            NavUtils.navigateUpFromSameTask( LayoutExamActivity.this );
                        } else {
                            LayoutExamActivity.super.onBackPressed();
                        }
                    }
                }).setMessage( "Leaving means you forfeit doing this examination, are you sure you want to continue?" )
                .setTitle( android.R.string.dialog_alert_title ).create().show();
    }

}
