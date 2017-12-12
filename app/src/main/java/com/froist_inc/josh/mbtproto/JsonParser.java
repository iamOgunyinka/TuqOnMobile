package com.froist_inc.josh.mbtproto;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

class JsonParser {

    JsonParser(){}

    JSONObject ParseObject( final String data )
    {
        if( data == null ) return null;
        try {
            return ( JSONObject ) new JSONTokener( data ).nextValue();
        } catch ( JSONException except ){
            except.printStackTrace();
            return null;
        }
    }
    JSONObject ParseObject( byte [] data )
    {
        if( data == null ) return null;
        return ParseObject( Utilities.ByteArrayToString( data ) );
    }
    
}
