package com.froist_inc.josh.mbtproto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class CHandlerThread extends android.os.HandlerThread
{
    private Handler m_default_handler;
    private final Handler m_main_ui_handler;
    private Listener m_listener;

    public interface Listener
    {
        void OnSubjectCodeDataObtained( SubjectInformation subject_information );
        void OnAllTasksCompleted();
    }

    private static final String TAG = "HandlerThread";
    private static final int DATA_INITIALIZE = 0;

    private final Map<SubjectInformation, String> m_data =
            Collections.synchronizedMap( new HashMap<SubjectInformation, String>() );

    public CHandlerThread( Context context, Handler main_ui_handler )
    {
        super( TAG );
        m_main_ui_handler = main_ui_handler;
    }

    public void setListener( Listener listener )
    {
        m_listener = listener;
    }

    @SuppressLint({"", "HandlerLeak"})
    @Override
    protected void onLooperPrepared() {
        m_default_handler = new Handler(){
            @Override
            public void handleMessage( Message msg )
            {
                if( msg.what == CHandlerThread.DATA_INITIALIZE ){
                    @SuppressWarnings( "unchecked" )
                    SubjectInformation info = ( SubjectInformation ) msg.obj;
                    HandleMessage( info );
                }
            }
        };
    }

    private void HandleMessage( final SubjectInformation subject )
    {
        try {
            byte [] data = GrabData( subject.GetSubjectDataUrl() );
            JsonParser json_parser = new JsonParser();
            subject.SetQuestionData ( json_parser.ParseObject( data ) );
            subject.icon_data = GrabData( subject.GetSubjectIconUrl() );
            /* In case the handler itself or the looper associated with the handler is destroyed, don't do anything. */
            if( m_default_handler == null || m_default_handler.getLooper() == null ){
                return;
            }
            m_main_ui_handler.post( new Runnable() {
                @Override
                public void run() {
                    m_data.remove( subject );
                    //m_listener.OnSubjectCodeDataObtained( subject );
                    if( m_data.size() == 0 && m_listener != null ){
                        m_listener.OnAllTasksCompleted();
                    }
                }
            });
        } catch ( IOException exception ){
            Log.d( TAG, exception.getLocalizedMessage(), exception );
        } finally {
            m_data.remove( subject );
            if( m_data.size() == 0 && m_listener != null ){
                m_listener.OnAllTasksCompleted();
            }
        }
    }

    public void Prepare( final SubjectInformation subject_information )
    {
        if( m_default_handler.getLooper() != null ) {
            m_data.put( subject_information, subject_information.GetSubjectName() );
            m_default_handler.obtainMessage( CHandlerThread.DATA_INITIALIZE, subject_information ).sendToTarget();
        }
    }

    private byte[] GrabData( final String url ) throws IOException
    {
        if( url != null ){
            return NetworkManager.GetNetwork().GetData( url );
        }
        return null;
    }

    @Override
    public boolean quit() {
        m_data.clear();
        this.interrupt();
        return super.quit();
    }
}
