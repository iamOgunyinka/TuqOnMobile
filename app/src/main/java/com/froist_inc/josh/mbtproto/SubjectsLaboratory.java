package com.froist_inc.josh.mbtproto;

import android.content.Context;
import android.support.v4.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class SubjectsLaboratory
{
    private static SubjectsLaboratory subjects_lab_instance;
    private static ArrayList<Pair<String, String>> cached_repositories;
    private static ArrayList<Utilities.RankedCoursesData> ranked_courses;
    private ArrayList<SubjectInformation> items;

    public static ArrayList<Utilities.RankedCoursesData> GetRankedCourses()
    {
        if( ranked_courses == null ){
            ranked_courses = new ArrayList<>();
        }
        return ranked_courses;
    }

    public static void SetRankedCourses( final ArrayList<Utilities.RankedCoursesData> ranked_courses )
    {
        SubjectsLaboratory.ranked_courses = ranked_courses;
    }

    public static Utilities.RankedCoursesData GetRankedCourseAtIndex( final int index )
    {
        return SubjectsLaboratory.ranked_courses.get( index );
    }

    public static SubjectsLaboratory Get( Context context )
    {
        if( subjects_lab_instance == null ){
            subjects_lab_instance = new SubjectsLaboratory( context );
        }
        return subjects_lab_instance;
    }

    public static ArrayList<Pair<String, String>> GetRepositories()
    {
        if( cached_repositories == null ){
            cached_repositories = new ArrayList<>();
        }
        return cached_repositories;
    }

    public static Pair<String, String> GetRepositoryAtIndex( int index )
    {
        return cached_repositories.get( index );
    }

    public ArrayList<SubjectInformation> GetSubjects()
    {
        return items;
    }

    public SubjectInformation GetSubjectItem( int index )
    {
        return items.get( index );
    }

    public void SetSubjects( ArrayList<SubjectInformation> subjects )
    {
        items = subjects;
    }

    @SuppressWarnings( "unused" )
    private SubjectsLaboratory( Context context )
    {
        items = new ArrayList<>();
    }

    public static void SetRepositories( ArrayList<Pair<String, String>> repositories )
    {
        cached_repositories = repositories;
    }

    public static void LoadCachedRepositories( Context context, final String filename ) throws IOException {
        ArrayList<Pair<String, String>> repositories = new ArrayList<>();
        BufferedReader reader = null;
        try {
            InputStream input_stream = context.openFileInput( filename );
            reader = new BufferedReader( new InputStreamReader( input_stream ));
            StringBuilder buffer = new StringBuilder();
            String each_line;
            while( ( each_line = reader.readLine() ) != null ){
                buffer.append( each_line );
            }
            JSONObject root_object = new JSONObject( buffer.toString() );
            JSONArray array_of_repositories = root_object.getJSONArray( "items" );
            for( int i = 0; i != array_of_repositories.length(); ++i )
            {
                JSONObject repository = array_of_repositories.getJSONObject( i );
                repositories.add( new Pair<>( repository.getString( "name" ), repository.getString( "url" )) );
            }
            SubjectsLaboratory.SetRepositories( repositories );
        } catch( JSONException | IOException exception ) {
            exception.printStackTrace();
        } finally {
            if( reader != null ) reader.close();
        }
    }

    public static void SynchronizeRepositories( Context context, final String filename ) throws IOException
    {
        Writer writer = null;
        try {
            JSONArray items = new JSONArray();
            ArrayList<Pair<String, String>> repositories = GetRepositories();
            for( int i = 0; i != repositories.size(); ++i ){
                Pair<String, String> item = repositories.get( i );
                JSONObject obj = new JSONObject();
                obj.put( "name", item.first );
                obj.put( "url", item.second );
                items.put( obj );
            }
            JSONObject root_object = new JSONObject();
            root_object.put( "items", items );
            FileOutputStream output_stream = context.openFileOutput(filename, Context.MODE_PRIVATE );
            writer = new OutputStreamWriter( output_stream );
            writer.write( root_object.toString() );
        } catch ( JSONException except ){
            except.printStackTrace();
        } finally {
            if( writer != null ) writer.close();
        }
    }

    public static void AddRepository( Pair<String, String> repository )
    {
        for( int i = 0; i != GetRepositories().size(); ++i ){
            if( GetRepositoryAtIndex( i ).second.equals( repository.second )) return;
        }
        GetRepositories().add( repository );
    }

    public static boolean SetRepositoryAtIndex( int index, Pair<String, String> repository )
    {
        for( int i = 0; i != GetRepositories().size(); ++i ){
            if( i != index &&
                    ( GetRepositoryAtIndex( i ).first.equals( repository.first ) ||
                            GetRepositoryAtIndex( i ).second.equals( repository.second ) )){
                return false;
            }
        }
        GetRepositories().set( index, repository );
        return true;
    }
}
