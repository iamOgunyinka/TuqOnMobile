package com.froist_inc.josh.mbtproto;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@SuppressWarnings("ALL")
class FileManager
{
    private final Context m_context;
    private static final String TAG = "FileManager";

    FileManager( Context context )
    {
        m_context = context;
    }

    boolean FileExists( String filename )
    {
        return new File( m_context.getFilesDir(), filename ).exists();
    }

    boolean FileExists( String filename, String parent_directory ) throws IOException
    {
        File parent_directory_file = new File( m_context.getFilesDir(), parent_directory );
        File file = new File( parent_directory_file.getCanonicalPath(), filename );
        return file.exists() && !file.isDirectory();
    }

    boolean CreateDirectory( String directory_name )
    {
        File directory = m_context.getDir( directory_name, Context.MODE_PRIVATE );
        return directory != null;
    }

    public String ReadDataFromFile( String filename ) throws IOException
    {
        BufferedReader reader = null;
        FileInputStream file_input_stream = null;

        try {
            file_input_stream = new FileInputStream( filename );
            reader = new BufferedReader( new InputStreamReader( file_input_stream ) );
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while( ( line = reader.readLine() ) != null ){
                stringBuilder.append( line );
            }
            return stringBuilder.toString();
        } finally {
            if( reader != null ) reader.close();
            if( file_input_stream != null ) file_input_stream.close();
        }
    }

    public void SaveDataToFile( byte [] data, String filename, String parent_directory ) throws IOException
    {
        FileOutputStream stream = null;
        File new_file;
        try {
            File files_dir = m_context.getFilesDir();
            if( parent_directory != null ) {
                File parent_path = new File( files_dir, parent_directory );
                if( !parent_path.exists() ){
                    if( !parent_path.mkdirs() ){
                        throw new IOException( "Unable to create parent directory." );
                    }
                }
                new_file = new File( parent_path, filename );
            } else {
                new_file = new File( files_dir, filename );
            }
            if( !new_file.exists() ){
                if( !new_file.createNewFile() ){
                    throw new IOException( "Unable to create new file." );
                }
            }
            stream = new FileOutputStream( new_file );
            stream.write( data );
        } finally {
            if( stream != null ) stream.close();
        }
    }
}
