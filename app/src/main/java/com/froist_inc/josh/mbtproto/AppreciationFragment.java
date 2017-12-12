package com.froist_inc.josh.mbtproto;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AppreciationFragment extends Fragment
{
    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle saved_instance_state )
    {
        return inflater.inflate( R.layout.appreciation_fragment, container, false );
    }

    @Override
    public void onCreate( @Nullable Bundle saved_instance_state )
    {
        super.onCreate( saved_instance_state );
        getActivity().setTitle( R.string.appreciation );
    }
    //
//    @Override
//    public void onResume()
//    {
//        super.onResume();
//        InputStream in_stream = Resources.getSystem().openRawResource( R.raw.special_thanks );
//        BufferedReader reader = null;
//        try {
//            reader = new BufferedReader( new InputStreamReader( in_stream ) );
//            StringBuilder buffer = new StringBuilder();
//            String lines;
//            while ((lines = reader.readLine()) != null) {
//                buffer.append(lines);
//            }
//            appreciation_text_view.setText( buffer.toString() );
//        } catch ( IOException except ){
//            appreciation_text_view.setText( R.string.appr_to_all );
//        } finally {
//            if( reader != null ) try {
//                reader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
