package com.froist_inc.josh.mbtproto;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ResendConfirmationActivity extends AppCompatActivity
{
    private static final String TAG = "ConfirmEmailActivity";
    public static final String CONFIRM_ADDRESS_TAG = "CONFIRM_ADD";
    public static final String RESEND_EMAIL_RESULT = "RESEND_RESULT";

    private String signup_address;
    private String confirm_email_address;

    @Bind( R.id.confirm_btn_sendit ) Button sendit_button;
    @Bind( R.id.confirm_input_email ) EditText email_address_text;
    @Bind( R.id.confirm_link_signup ) TextView open_signup_text;
    @Bind( R.id.confirm_link_login ) TextView open_login_text;

    @Override
    public void onCreate( Bundle saved_instance_state )
    {
        super.onCreate( saved_instance_state );
        setContentView( R.layout.activity_resend_confirmation );
        ButterKnife.bind( this );

        signup_address = getIntent().getStringExtra( SignupActivity.SIGNUP_ADDRESS_INFO );
        confirm_email_address = getIntent().getStringExtra( ResendConfirmationActivity.CONFIRM_ADDRESS_TAG );

        sendit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_confirmation_email();
            }
        });
        open_login_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( NavUtils.getParentActivityName( ResendConfirmationActivity.this ) != null ){
                    NavUtils.navigateUpFromSameTask( ResendConfirmationActivity.this );
                } else {
                    ResendConfirmationActivity.this.onBackPressed();
                }
            }
        });
        open_signup_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( getApplicationContext(), SignupActivity.class );
                intent.putExtra( SignupActivity.SIGNUP_ADDRESS_INFO, signup_address );
                startActivity( intent );
                finish();
                overridePendingTransition( R.anim.push_left_in, R.anim.push_left_out );
            }
        });

    }

    private void send_confirmation_email()
    {
        if( !validate() ){
            onSendConfirmationEmailFailed( "Invalid email" );
            return;
        }
        sendit_button.setEnabled( false );
        final ProgressDialog progress_dialog = new ProgressDialog( ResendConfirmationActivity.this,
                R.style.AppTheme_Dark_Dialog );
        progress_dialog.setIndeterminate( true );
        progress_dialog.setMessage( "Sending..." );
        progress_dialog.show();

        final String email_address = email_address_text.getText().toString();
        final Handler main_ui_looper = new Handler( getApplicationContext().getMainLooper() );
        Thread login_thread = new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject post_information = new JSONObject();
                    post_information.put( "email", email_address );

                    final int content_length = post_information.toString().length();
                    final String response = NetworkManager.GetNetwork().PostData( confirm_email_address,
                            post_information.toString(), Utilities.GetHeaders( content_length ));
                    JSONObject result = null;
                    if( response != null ) {
                        result = new JsonParser().ParseObject( response );
                    }
                    final String message = result != null ? result.getString( "detail" ) :
                            "Could not get any data from the server";
                    progress_dialog.cancel();
                    if ( result != null && ( result.getInt( "status" ) == Utilities.Success )) {
                        main_ui_looper.post( new Runnable() {
                            @Override
                            public void run() {
                                onEmailSentSuccessfully( message );
                            }
                        });
                    } else {
                        main_ui_looper.post(new Runnable() {
                            @Override
                            public void run() {
                                onSendConfirmationEmailFailed( message );
                            }
                        });
                    }
                } catch ( final JSONException | IOException except ) {
                    Log.v( TAG, except.getLocalizedMessage() );
                    main_ui_looper.post(new Runnable() {
                        @Override
                        public void run() {
                            progress_dialog.cancel();
                            onSendConfirmationEmailFailed( except.getMessage() );
                        }
                    });
                }
            }
        });
        login_thread.start();
    }

    public void onEmailSentSuccessfully( final String message )
    {
        Intent delivery_intent = new Intent();
        delivery_intent.putExtra( ResendConfirmationActivity.RESEND_EMAIL_RESULT, message );
        setResult( RESULT_OK, delivery_intent );
        finish();
        overridePendingTransition( R.anim.push_left_in, R.anim.push_left_out );
    }

    public void onSendConfirmationEmailFailed( final String message )
    {
        sendit_button.setEnabled( true );
        AlertDialog dialog = new AlertDialog.Builder( ResendConfirmationActivity.this ).setTitle( "Failed to send mail" )
                .setMessage( message ).setPositiveButton( android.R.string.ok, null ).create();
        dialog.show();
    }

    public boolean validate()
    {
        boolean valid = true;

        final String email = email_address_text.getText().toString();
        if ( email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher( email ).matches() ) {
            email_address_text.setError( "enter a valid email address" );
            valid = false;
        } else {
            email_address_text.setError( null );
        }
        return valid;
    }
}
