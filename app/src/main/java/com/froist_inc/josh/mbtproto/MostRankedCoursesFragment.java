package com.froist_inc.josh.mbtproto;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MostRankedCoursesFragment extends Fragment
{
    @Override
    public void onCreate( @Nullable Bundle saved_instance_state )
    {
        super.onCreate( saved_instance_state );
        getActivity().setTitle( "Most ranked courses" );
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle saved_instance_state )
    {
        return inflater.inflate( R.layout.ranked_courses_fragment, container, false );
    }
}
