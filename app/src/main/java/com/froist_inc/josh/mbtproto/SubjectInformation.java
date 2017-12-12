package com.froist_inc.josh.mbtproto;

import org.json.JSONObject;

import java.util.ArrayList;

public class SubjectInformation
{
    private final String course_title;
    private final String course_code;
    private final String course_data_url;
    private String       course_icon_url;
    private String       course_owner;
    private String       reply_to;
    private String       course_instructor;
    private JSONObject   question_data;
    private ArrayList<String> subject_departments;
    public byte []       icon_data;
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
        course_title = name;
        course_code = code;
        course_data_url = data_url;
        question_data = new JSONObject();
    }

    public String GetSubjectName() {
        return course_title;
    }

    public String GetSubjectCode() {
        return course_code;
    }

    public String GetSubjectDataUrl() {
        return course_data_url;
    }

    public String GetSubjectIconUrl() {
        return course_icon_url;
    }
    void SetDepartments( final ArrayList<String> department ){ subject_departments = department; }
    ArrayList<String> GetDepartments() { return subject_departments; }

    void SetQuestionData( final JSONObject data ){
        question_data = data;
    }
    JSONObject GetQuestionData(){ return question_data; }
    
    public void SetReplyUrl( final String url ){ reply_to = url; }
    public String GetReplyUrl(){ return reply_to; }
    public void SetIconAddress( String icon_url ) {
        course_icon_url = icon_url;
    }

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
