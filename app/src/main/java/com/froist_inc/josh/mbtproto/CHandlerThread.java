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

class CHandlerThread<Type extends Utilities.BasicData> extends android.os.HandlerThread
{
    private Handler m_default_handler;
    private final Handler m_main_ui_handler;
    private Listener m_listener;

    public interface Listener
    {
        void OnAllTasksCompleted();
    }

    private static final String TAG = "HandlerThread";
    private static final int DATA_INITIALIZE = 0;

    private final Map<Type, String> m_data = Collections.synchronizedMap( new HashMap<Type, String>() );

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
                    Type info = ( Type ) msg.obj;
                    HandleMessage( info );
                }
            }
        };
    }

    private void HandleMessage( final Type subject )
    {
        try {
            byte [] data = GrabData( subject.course_data_url );
            subject.SetQuestionData ( new JsonParser().ParseObject( data ) );
            subject.SetIconData( GrabData( subject.GetIconUrl() ) );
            /* In case the handler itself or the looper associated with the handler is destroyed, don't do anything. */
            if( m_default_handler == null || m_default_handler.getLooper() == null ){
                return;
            }
            m_main_ui_handler.post( new Runnable() {
                @Override
                public void run() {
                    m_data.remove( subject );
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

    public void Prepare( final Type data )
    {
        if( m_default_handler.getLooper() != null ) {
            m_data.put( data, data.course_data_url );
            m_default_handler.obtainMessage( CHandlerThread.DATA_INITIALIZE, data ).sendToTarget();
        }
    }

    private byte[] GrabData( final String url ) throws IOException
    {
        return url == null ? null : NetworkManager.GetNetwork().GetData( url );
    }

    @Override
    public boolean quit() {
        m_data.clear();
        this.interrupt();
        return super.quit();
    }
}
