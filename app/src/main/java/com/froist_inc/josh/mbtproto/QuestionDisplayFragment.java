package com.froist_inc.josh.mbtproto;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuestionDisplayFragment extends Fragment
{
    private static final String QUESTION_INDEX = "Q_INDEX";
    private static final String TAG = "QuestionDisplayFrag";
    private int current_question_index = 0;

    private ExecutorService executors;
    private Handler     main_ui_thread;
    private ImageView   m_extracted_link_image;
    private TextView    m_extracted_link_text;
    private TextView    m_question_text_view;
    private TextView    m_question_index_text_view;

    private RadioButton m_first_option_radio;
    private RadioButton m_second_option_radio;
    private RadioButton m_third_option_radio;
    private RadioButton m_fourth_option_radio;
    private RadioGroup  m_option_group;
    private View        view = null;

    @Override
    public void onCreate( @Nullable Bundle saved_instance_state )
    {
        super.onCreate( saved_instance_state );
        current_question_index = getArguments().getInt( QUESTION_INDEX );
        executors = Executors.newSingleThreadExecutor();
        main_ui_thread = new Handler( this.getContext().getMainLooper() );
    }

    @Override
    public void onStop()
    {
        executors.shutdown();
        super.onStop();
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            saved_instance_state )
    {
        view = inflater.inflate( R.layout.question_page_fragment, container, false );
        InitializeView();
        return view;
    }

    private void UpdateQuestionView( int index )
    {
        if( view == null || StartExamActivity.GetQuestionList() == null ) return;
        QuestionManager current_question = StartExamActivity.GetQuestionList().get( index );
        final String data_address = current_question.GetLink();

        if( data_address != null ) {
            GetDataIntoFields( data_address );
        } else {
            m_extracted_link_image.setVisibility( View.INVISIBLE );
            m_extracted_link_text.setVisibility( View.INVISIBLE );
        }

        m_question_index_text_view.setText( getString( R.string.question_index, ( index + 1 ),
                StartExamActivity.GetQuestionList().size() ) );
        m_question_text_view.setText( current_question.GetQuestion().trim() );

        m_option_group.clearCheck();
        m_first_option_radio.setText( current_question.GetAvailableOptions().get( 0 ) );
        m_second_option_radio.setText( current_question.GetAvailableOptions().get( 1 ) );
        m_third_option_radio.setText( current_question.GetAvailableOptions().get( 2 ) );
        if( current_question.GetAvailableOptions().size() >= 4 ) {
            m_fourth_option_radio.setText( current_question.GetAvailableOptions().get( 3 ) );
            m_fourth_option_radio.setVisibility( View.VISIBLE );
        } else {
            m_fourth_option_radio.setVisibility( View.INVISIBLE );
        }
        ActivateOptions( current_question.GetChosenOption() );
    }

    private void GetDataIntoFields( final String data_address )
    {
        executors.execute( new Runnable() {
            @Override
            public void run() {
                try {
                    final byte [] data = NetworkManager.GetNetwork().GetData( data_address );
                    if( data != null ){
                        main_ui_thread.post( new Runnable() {
                            @Override
                            public void run() {
                                if( data_address.endsWith( ".png" ) ){
                                    Bitmap bitmap = BitmapFactory.decodeByteArray( data, 0, data.length );
                                    if( bitmap != null ) {
                                        m_extracted_link_image.setImageBitmap(bitmap);
                                        m_extracted_link_image.setVisibility( View.VISIBLE );
                                        m_extracted_link_text.setVisibility( View.INVISIBLE );
                                    } else {
                                        m_extracted_link_image.setImageResource( R.drawable.default_icon );
                                    }
                                } else if ( data_address.endsWith( ".txt" )){
                                    m_extracted_link_text.setText( Utilities.ByteArrayToString( data ));
                                    m_extracted_link_text.setVisibility( View.VISIBLE );
                                    m_extracted_link_image.setVisibility( View.INVISIBLE );
                                } else {
                                    m_extracted_link_text.setText( R.string.problem_fetching_data );
                                    m_extracted_link_text.setVisibility( View.VISIBLE );
                                    m_extracted_link_image.setVisibility( View.INVISIBLE );
                                }
                            }
                        });
                    } else {
                        HandleDataFetchError( getString( R.string.problem_fetching_data ) );
                    }
                } catch ( IOException except ){
                    HandleDataFetchError( except.getLocalizedMessage() );
                }
            }
        });
    }

    private void HandleDataFetchError( final String error_message )
    {
        Log.v( TAG, error_message );
        main_ui_thread.post( new Runnable() {
            @Override
            public void run() {
                m_extracted_link_text.setText( error_message );
                m_extracted_link_text.setVisibility( View.VISIBLE );
                m_extracted_link_image.setVisibility( View.INVISIBLE );
            }
        });
    }
    private void ActivateOptions( int i )
    {
        if( i == 0 ) m_first_option_radio.setChecked( true );
        else if ( i == 1 ) m_second_option_radio.setChecked( true );
        else if( i == 2 ) m_third_option_radio.setChecked( true );
        else if( i == 3 ) m_fourth_option_radio.setChecked( true );
    }

    private void InitializeView()
    {
        m_question_index_text_view = ( TextView ) view.findViewById( R.id.questionIndex );

        m_extracted_link_text = ( TextView ) view.findViewById( R.id.question_extracted_link_text );
        m_extracted_link_image = ( ImageView ) view.findViewById( R.id.question_extracted_link_image );

        m_question_text_view = ( TextView ) view.findViewById( R.id.question_textView );
        m_first_option_radio = ( RadioButton ) view.findViewById( R.id.question_option_oneRadioButton );
        m_second_option_radio = ( RadioButton ) view.findViewById( R.id.question_option_twoRadioButton );
        m_third_option_radio = ( RadioButton ) view.findViewById( R.id.question_option_threeRadioButton );
        m_fourth_option_radio = ( RadioButton ) view.findViewById( R.id.question_option_fourRadioButton );

        m_option_group = ( RadioGroup ) view.findViewById( R.id.radioGroup );
        m_option_group.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged( RadioGroup group, int checkedId ) {
                switch( checkedId ){
                    case R.id.question_option_oneRadioButton:
                        StartExamActivity.GetQuestionList().get( current_question_index ).SetChosenOption( 0 );
                        break;
                    case R.id.question_option_twoRadioButton:
                        StartExamActivity.GetQuestionList().get( current_question_index ).SetChosenOption( 1 );
                        break;
                    case R.id.question_option_threeRadioButton:
                        StartExamActivity.GetQuestionList().get( current_question_index ).SetChosenOption( 2 );
                        break;
                    case R.id.question_option_fourRadioButton:
                        StartExamActivity.GetQuestionList().get( current_question_index ).SetChosenOption( 3 );
                        break;
                    default:
                        StartExamActivity.GetQuestionList().get( current_question_index ).SetChosenOption( -1 );
                        break;
                }
            }
        });
        UpdateQuestionView( current_question_index );
    }

    public static Fragment NewInstance( final int question_index )
    {
        Bundle bundle = new Bundle();
        bundle.putInt( QUESTION_INDEX, question_index );
        QuestionDisplayFragment display_fragment = new QuestionDisplayFragment();
        display_fragment.setArguments( bundle );
        return display_fragment;
    }

    @Override
    public void onSaveInstanceState( Bundle out_state )
    {
        super.onSaveInstanceState( out_state );
        out_state.putInt( QUESTION_INDEX, current_question_index );
    }
}
