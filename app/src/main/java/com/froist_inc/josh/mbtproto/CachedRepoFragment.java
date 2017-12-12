package com.froist_inc.josh.mbtproto;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class CachedRepoFragment extends ListFragment
{
    private static final String TAG = "CachedRepoFrgament";
    @Override
    public void onCreate( @Nullable Bundle saved_instance_state )
    {
        super.onCreate( saved_instance_state );
        getActivity().setTitle( R.string.saved_repos );
        setRetainInstance( true );

        try {
            if( SubjectsLaboratory.GetRepositories().isEmpty() ) {
                SubjectsLaboratory.LoadCachedRepositories( getContext(), MainActivity.FILENAME );
            }
        } catch ( IOException exception ){
            exception.printStackTrace();
        }
        setListAdapter( new RepositoryAdapter( SubjectsLaboratory.GetRepositories() ));
    }

    @Override
    public void onPause()
    {
        SyncRepository();
        super.onPause();
    }

    @Override
    public void onResume() {
        if( getListAdapter().isEmpty() ) {
            this.setEmptyText( getString( R.string.empty_cached_repository ) );
        }
        super.onResume();
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle saved_instance_state )
    {
        View view = super.onCreateView( inflater, container, saved_instance_state );
        view.setBackgroundResource( R.drawable.background_activated );

        ListView list_view = ( ListView ) view.findViewById( android.R.id.list );
        list_view.setChoiceMode( ListView.CHOICE_MODE_MULTIPLE_MODAL );
        list_view.setMultiChoiceModeListener( new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged( ActionMode mode, int position, long id, boolean checked )
            {
                final int selected_item_count = getListView().getCheckedItemCount();
                mode.setTitle( String.valueOf( selected_item_count ) + " items selected" );

                Menu menu = mode.getMenu();
                MenuItem edit_menu_item = menu.findItem( R.id.edit_cached_item ),
                        delete_menu_item = menu.findItem( R.id.delete_cached_item );

                assert edit_menu_item != null;
                assert delete_menu_item != null;

                edit_menu_item.setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS );
                delete_menu_item.setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS );

                edit_menu_item.setVisible( selected_item_count == 1 );
            }

            @Override
            public boolean onCreateActionMode( ActionMode mode, Menu menu )
            {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate( R.menu.cached_repo_menu, menu );
                mode.setTitle( R.string.saved_repos );
                return true;
            }

            @Override
            public boolean onPrepareActionMode( ActionMode mode, Menu menu )
            {
                return false;
            }

            @Override
            public boolean onActionItemClicked( final ActionMode mode, MenuItem item )
            {
                final int selected_item_count = getListView().getCheckedItemCount();
                if ( selected_item_count == 0 ){
                    mode.finish();
                    return true;
                }

                switch( item.getItemId() )
                {
                    case R.id.delete_cached_item:
                        AlertDialog delete_dialog = new AlertDialog.Builder( CachedRepoFragment.this.getContext() )
                                .setMessage( "Are you sure you want to delete " + selected_item_count + " items?" )
                                .setNegativeButton( android.R.string.no, null )
                                .setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick( DialogInterface dialog, int which ) {
                                        ArrayList<Pair<String, String>> repositories = SubjectsLaboratory.GetRepositories();
                                        RepositoryAdapter adapter = ( RepositoryAdapter ) getListAdapter();
                                        for( int i = adapter.getCount() - 1; i >= 0 ; --i ){
                                            if( getListView().isItemChecked( i ) ){
                                                repositories.remove( adapter.getItem( i ) );
                                            }
                                        }
                                        mode.finish();
                                        adapter.notifyDataSetChanged();
                                    }
                                }).create();
                        delete_dialog.show();
                        SyncRepository();
                        return true;
                    case R.id.edit_cached_item:
                        final RepositoryAdapter adapter = ( RepositoryAdapter ) getListAdapter();
                        if( selected_item_count == 1 ){
                            int item_index = adapter.getCount() - 1;
                            for( ; item_index >= 0; ++item_index ){
                                if( getListView().isItemChecked( item_index ) ) break;
                            }
                            Pair<String, String> repository = SubjectsLaboratory.GetRepositoryAtIndex( item_index );

                            View view = CachedRepoFragment.this.getLayoutInflater( null ).inflate( R.layout.edit_repository_layout, null );
                            final EditText repository_name_edit = ( EditText ) view.findViewById( R.id.repo_name_edit_text );
                            final EditText repository_url_edit = ( EditText ) view.findViewById( R.id.repo_url_edit_text );

                            repository_name_edit.setText( repository.first );
                            repository_url_edit.setText( repository.second );

                            final int final_item_index = item_index;
                            AlertDialog edit_dialog = new AlertDialog.Builder( CachedRepoFragment.this.getContext() ).setView( view )
                                    .setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick( DialogInterface dialog, int which ) {
                                            Pair<String, String> repo = new Pair<>( repository_name_edit.getText().toString(),
                                                    repository_url_edit.getText().toString() );
                                            if( SubjectsLaboratory.SetRepositoryAtIndex( final_item_index, repo ) ){
                                                adapter.notifyDataSetChanged();
                                            } else {
                                                Toast.makeText( CachedRepoFragment.this.getContext(), "Repository with that alias already exist",
                                                        Toast.LENGTH_SHORT ).show();
                                            }
                                        }
                                    }).setNegativeButton( android.R.string.cancel, null ).create();
                            edit_dialog.show();
                            SyncRepository();
                        }
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode( ActionMode mode )
            {
            }
        });
        return view;
    }

    @Override
    public void onListItemClick( ListView list, View view, int position, long id )
    {
        final String address = SubjectsLaboratory.GetRepositoryAtIndex( position ).second;
        Fragment fragment = SubjectPresenterFragment.GetInstance( address );
        ( ( MainActivity ) getActivity() ).PlaceFragment( fragment, R.id.nav_add_repo );
    }

    private class RepositoryAdapter extends ArrayAdapter<Pair<String, String>>
    {
        RepositoryAdapter( ArrayList<Pair<String, String>> repositories )
        {
            super( CachedRepoFragment.this.getContext(), 0, repositories );
        }

        @Override
        public View getView( int position, View convert_view, ViewGroup parent )
        {
            if( convert_view == null )
            {
                convert_view = getActivity().getLayoutInflater().inflate( R.layout.cachedrepo_adapter_layout, parent,
                        false );
            }
            Pair<String, String> item = getItem( position );
            TextView repository_name_textview = ( TextView ) convert_view.findViewById( R.id.crepo_name );
            TextView repository_url_textview = ( TextView ) convert_view.findViewById( R.id.crepo_url );

            repository_name_textview.setText( item.first );
            repository_url_textview.setText( item.second );
            return convert_view;
        }
    }

    private void SyncRepository()
    {
        try {
            SubjectsLaboratory.SynchronizeRepositories( getContext(), MainActivity.FILENAME );
        } catch ( IOException except ){
            except.printStackTrace();
        }
    }
}
