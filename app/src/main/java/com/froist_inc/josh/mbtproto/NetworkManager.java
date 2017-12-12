package com.froist_inc.josh.mbtproto;

import android.support.v4.util.Pair;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import info.guardianproject.netcipher.NetCipher;

public class NetworkManager
{
    private static Network s_network;
    private static final String https_prefix = "https://";
    private static final String http_prefix = "http://";

    public static Network GetNetwork()
    {
        if ( s_network == null )
        {
            CookieManager cookie_manager = new CookieManager();
            CookieHandler.setDefault( cookie_manager );
            s_network = new Network();
        }
        return s_network;
    }

    public static class Network
    {
        private Network()
        {
        }

        boolean IsSecureHttp( final String address )
        {
            return address.startsWith( https_prefix );
        }

        String NormalizeAddress( final String address )
        {
            if( IsSecureHttp( address ) || address.startsWith( http_prefix )) {
                return address;
            }
            return https_prefix + address;
        }

        public byte[] GetData( String address ) throws IOException
        {
            if( address == null ) return null;
            
            URL url;
            final int fifteen_seconds = 15_000;
            address = NormalizeAddress( address );
            HttpURLConnection url_connection = null;

            try
            {
                url = new URL( address );
                if( IsSecureHttp( address ) ){
                    //~ url_connection = ( HttpsURLConnection ) url.openConnection();
                    url_connection = NetCipher.getHttpsURLConnection( url );
                } else {
                    url_connection = ( HttpURLConnection ) url.openConnection();
                }
                url_connection.setRequestProperty( "from", "TuqOnMobile" );
                url_connection.setConnectTimeout( fifteen_seconds );
                ByteArrayOutputStream byte_array_output_stream = new ByteArrayOutputStream();
                InputStream in = url_connection.getInputStream();

                if ( url_connection.getResponseCode() != HttpURLConnection.HTTP_OK )
                {
                    return null;
                }
                int bytes_read;
                byte[] buffer = new byte[1024];
                while ( ( bytes_read = in.read( buffer ) ) > 0 )
                {
                    byte_array_output_stream.write( buffer, 0, bytes_read );
                }
                byte_array_output_stream.close();
                return byte_array_output_stream.toByteArray();
            }
            finally
            {
                if ( url_connection != null )
                {
                    url_connection.disconnect();
                }
            }
        }

        public String PostData( final String address, final String buffer, final ArrayList<Pair<String, String>> headers )
                throws IOException
        {
            if( address == null ) return null;
            URL url_address;
            HttpURLConnection network_connection = null;
            try
            {
                url_address = new URL( address );
                if( IsSecureHttp( address )) {
                    network_connection = NetCipher.getHttpsURLConnection( url_address );
                    //~ network_connection = ( HttpsURLConnection ) url_address.openConnection();
                } else {
                    network_connection = ( HttpURLConnection ) url_address.openConnection();
                }
                network_connection.setRequestMethod( "POST" );
                network_connection.setDoOutput( true );
                network_connection.setDoInput( true );
                network_connection.setConnectTimeout( 15_000 );
                for ( int index = 0; index != headers.size(); ++index )
                {
                    network_connection.setRequestProperty( headers.get( index ).first, headers.get( index ).second );
                }
                DataOutputStream out_stream = new DataOutputStream( network_connection.getOutputStream() );
                out_stream.writeBytes( buffer );
                out_stream.flush();
                out_stream.close();

                BufferedReader in = new BufferedReader( new InputStreamReader( network_connection.getInputStream()));
                StringBuilder response = new StringBuilder();

                String input_line;
                while (( input_line = in.readLine() ) != null ) {
                    response.append( input_line );
                }
                return response.toString();
            }
            finally
            {
                if ( network_connection != null )
                {
                    network_connection.disconnect();
                }
            }
        }
    }
}
