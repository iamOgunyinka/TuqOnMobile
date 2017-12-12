package com.froist_inc.josh.mbtproto;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    public static final String SIGNUP_ADDRESS_INFO = "ADDR_INFO";
    public static final String SIGNUP_RESULT = "SIGNUP_RESULT";

    private String signup_url;
    private ProgressDialog progressDialog;

    @Bind(R.id.input_name) EditText name_text;
    @Bind(R.id.input_address) EditText address_text;
    @Bind(R.id.input_email) EditText email_text;
    @Bind(R.id.input_username) EditText username_text_edit;
    @Bind(R.id.input_mobile) EditText phone_text;
    @Bind(R.id.input_password) EditText password_text;
    @Bind(R.id.input_reEnterPassword) EditText re_enter_password_text;
    @Bind(R.id.btn_signup) Button sign_up_button;
    @Bind(R.id.link_login) TextView login_link;

    @Override
    public void onCreate( Bundle saved_instance_state )
    {
        super.onCreate( saved_instance_state );
        setContentView( R.layout.activity_signup );
        ButterKnife.bind( this );

        signup_url = getIntent().getStringExtra( SIGNUP_ADDRESS_INFO );
        sign_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        login_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( getApplicationContext(),LoginActivity.class );
                startActivity( intent );
                finish();
                overridePendingTransition( R.anim.push_left_in, R.anim.push_left_out );
            }
        });
        progressDialog = new ProgressDialog( this, R.style.AppTheme_Dark_Dialog );
    }

    public void signup()
    {
        if ( !validate() ) {
            onSignupFailed( "Invalid data" );
            return;
        }

        sign_up_button.setEnabled( false );

        progressDialog.setIndeterminate( true );
        progressDialog.setMessage( "Creating Account..." );
        progressDialog.show();

        final String name = name_text.getText().toString();
        final String address = address_text.getText().toString();
        final String email = email_text.getText().toString();
        final String mobile = phone_text.getText().toString();
        final String password = password_text.getText().toString();
        final String username = username_text_edit.getText().toString();

        JSONObject signup_information = new JSONObject();
        try {
            signup_information.put( "full_name", name );
            signup_information.put( "address", address );
            signup_information.put( "email", email );
            signup_information.put( "username", username );
            signup_information.put( "phone", mobile );
            signup_information.put( "password", password );
            final Handler ui_thread = new Handler( this.getApplicationContext().getMainLooper() );
            DoSignup( signup_information, ui_thread );
        } catch ( JSONException exception ){
            Log.v(TAG, exception.getLocalizedMessage() );
            sign_up_button.setEnabled( true );
            AlertDialog alert_dialog = new AlertDialog.Builder( this ).setTitle( "Signup" )
                    .setMessage( "Unable to create signup information" )
                    .setPositiveButton( android.R.string.ok, null ).create();
            alert_dialog.show();
        }
    }

    private void DoSignup( final JSONObject signup_information, final Handler ui_thread )
    {
        Thread signup_thread = new Thread( new Runnable() {
            @Override
            public void run()
            {
                String signup_info_string = signup_information.toString();
                final int content_length = signup_info_string.length();
                try {
                    final String network_result = NetworkManager.GetNetwork().PostData( signup_url, signup_info_string,
                            Utilities.GetHeaders( content_length ));

                    JSONObject parsed_result = new JsonParser().ParseObject( network_result );
                    final String message = parsed_result == null ? "Network error" :
                            parsed_result.getString( "detail" );
                    final int status = parsed_result == null ? Utilities.Error : parsed_result.getInt( "status" );
                    ui_thread.post( new Runnable() {
                        @Override
                        public void run() {
                            if( status == Utilities.Error ){
                                onSignupFailed( message );
                            } else {
                                onSignupSuccess( message );
                            }
                        }
                    });
                } catch ( JSONException | IOException exception ){
                    Log.v( TAG, exception.getLocalizedMessage() );
                    ui_thread.post( new Runnable() {
                        @Override
                        public void run() {
                            onSignupFailed( exception.getLocalizedMessage() );
                        }
                    });
                }
            }
        });
        signup_thread.start();
    }

    public void onSignupSuccess( String message )
    {
        sign_up_button.setEnabled( true );
        progressDialog.cancel();
        Intent delivery_intent = new Intent();
        delivery_intent.putExtra( SignupActivity.SIGNUP_RESULT, message );
        setResult( RESULT_OK, delivery_intent );
        finish();
    }

    public void onSignupFailed( final String display_message )
    {
        progressDialog.cancel();
        AlertDialog alert = new AlertDialog.Builder( this ).setPositiveButton(android.R.string.ok, null )
                .setTitle( "Signup failed" ).setMessage( display_message ).create();
        alert.show();
        sign_up_button.setEnabled( true );
    }

    public boolean validate()
    {
        boolean valid = true;

        String name = name_text.getText().toString();
        String address = address_text.getText().toString();
        String email = email_text.getText().toString();
        String mobile = phone_text.getText().toString();
        String password = password_text.getText().toString();
        String reEnterPassword = re_enter_password_text.getText().toString();

        if ( name.isEmpty() || name.length() < 3) {
            name_text.setError( "at least 3 characters" );
            valid = false;
        } else {
            name_text.setError( null );
        }

        if ( address.isEmpty() ) {
            address_text.setError( "Enter Valid Address" );
            valid = false;
        } else {
            address_text.setError( null );
        }


        if ( email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher( email ).matches() ) {
            email_text.setError( "enter a valid email address" );
            valid = false;
        } else {
            email_text.setError( null );
        }

        if ( !mobile.isEmpty() ) {
            if ( mobile.length() < 10) {
                phone_text.setError( "Enter a valid mobile number or leave it empty" );
                valid = false;
            }
        }

        if ( password.isEmpty() || password.length() < 4 || password.length() > 10 ) {
            password_text.setError( "between 4 and 10 alphanumeric characters" );
            valid = false;
        } else {
            password_text.setError( null );
        }

        if ( reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10
                || !( reEnterPassword.equals( password ) )) {
            re_enter_password_text.setError( "Password do not match" );
            valid = false;
        } else {
            re_enter_password_text.setError( null );
        }
        return valid;
    }
}
