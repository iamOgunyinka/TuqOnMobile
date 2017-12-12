package com.froist_inc.josh.mbtproto;

import java.util.ArrayList;

class QuestionManager
{
    private String                  question;
    private String                  external_data_address;
    private int                     chosen_option;
    private final ArrayList<String> available_options;

    public QuestionManager()
    {
        available_options = new ArrayList<>();
        chosen_option = -1;
    }

    public void SetChosenOption( int option ) {
        chosen_option = option;
    }

    public int GetChosenOption(){ return chosen_option; }

    public String GetQuestion() {
        return question;
    }

    public void SetQuestion( String question ) {
        this.question = question;
    }

    public ArrayList<String> GetAvailableOptions() {
        return available_options;
    }

    public void AddOptions( final String option ) {
        available_options.add( option );
    }

    public void SetExternalAddress( final String address ){
        external_data_address = address;
    }

    public String GetLink(){
        return external_data_address;
    }
}
