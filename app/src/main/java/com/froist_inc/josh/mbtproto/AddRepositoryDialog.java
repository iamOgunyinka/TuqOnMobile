package com.froist_inc.josh.mbtproto;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;


public class AddRepositoryDialog extends DialogFragment {

    public static final String TAG = "AddRepositoryDialog";
    public static final String ADD_REPO_RESULT_DATA = "Data";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate( R.layout.add_repo_layout, null );
        final EditText address_text_view = ( EditText ) view.findViewById( R.id.add_repo_text_edit );

        return new AlertDialog.Builder( getActivity() )
                .setView( view )
                .setTitle( R.string.add_repo )
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendDataToTargetFragment( address_text_view.getText().toString(), Activity.RESULT_OK );
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendDataToTargetFragment( null, Activity.RESULT_CANCELED );
                    }
                })
                .create();
    }

    private void sendDataToTargetFragment( @Nullable final String address, int result_code ) {
        Fragment target_fragment = getTargetFragment();
        if ( target_fragment == null ) return;

        Intent intent = new Intent();
        intent.putExtra( ADD_REPO_RESULT_DATA, address );
        target_fragment.onActivityResult( getTargetRequestCode(), result_code, intent);
    }
}
