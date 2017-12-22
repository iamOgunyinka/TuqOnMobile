package com.froist_inc.josh.mbtproto;

import org.json.JSONObject;

import java.util.ArrayList;

public class SubjectInformation extends Utilities.BasicData
{
    private String       course_owner;
    private String       reply_to;
    private String       course_instructor;
    private ArrayList<String> subject_departments;
    private boolean      randomize;
    private int          duration_in_minutes;
    /**
     *
     * @param name name given to the subject, e.g. C++, Java, Analogy
     * @param code name of the root directory containing the necessary information regarding
     *             the subject. e.g. for Computer Programming, code could be CSC 321.
     * @param data_url name of the URL containing the resource for `name`
     */

    public SubjectInformation( final String name, final String code, final String data_url )
    {
        super( name, data_url, code );
        question_data = new JSONObject();
    }

    void SetDepartments( final ArrayList<String> department ){ subject_departments = department; }
    ArrayList<String> GetDepartments() { return subject_departments; }

    JSONObject GetQuestionData(){ return question_data; }
    
    public void SetReplyUrl( final String url ){ reply_to = url; }
    public String GetReplyUrl(){ return reply_to; }
    public void SetInstructor( final String instructor ){
        course_instructor = instructor;
    }

    public String GetInstructor() {
        return course_instructor;
    }

    public void SetCourseOwner( final String owner ){
        course_owner = owner;
    }

    public String GetCourseOwner() {
        return course_owner;
    }

    public boolean RandomizingQuestion() {
        return randomize;
    }

    public void SetRandomizingQuestion( final boolean randomize ) {
        this.randomize = randomize;
    }
    public void SetDurationInMinutes( final int duration )
    {
        duration_in_minutes = duration;
    }
    public int GetDuration()
    {
        return duration_in_minutes;
    }
}
