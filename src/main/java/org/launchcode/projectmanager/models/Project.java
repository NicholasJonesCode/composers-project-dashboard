package org.launchcode.projectmanager.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
public class Project {

    @Id
    @GeneratedValue
    private int id;

    @NotNull
    @Size(min = 1, max = 500, message = "Title cannot be empty!")
    private String title;

    private String primary_music_key;

    //private String secondary_music_key;

    private String genre;

    private String primary_time_sig;

    //private String secondary_time_sig;

    private String starting_tempo;

    private String notes;

    //private List<String> file_paths;

    private boolean isPublic = false;

    public Project() {}


    //ID
    public int getId() {
        return id;
    }
    //TITLE
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    //PRIMARY MUSIC KEY
    public String getPrimary_music_key() {
        return primary_music_key;
    }

    public void setPrimary_music_key(String primary_music_key) {
        this.primary_music_key = primary_music_key;
    }
    //GENRE
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
    //PRIMARY TIME SIG
    public String getPrimary_time_sig() {
        return primary_time_sig;
    }

    public void setPrimary_time_sig(String primary_time_sig) {
        this.primary_time_sig = primary_time_sig;
    }
    //STARTING TEMPO
    public String getStarting_tempo() {
        return starting_tempo;
    }

    public void setStarting_tempo(String starting_tempo) {
        this.starting_tempo = starting_tempo;
    }
    //NOTES
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    //BOOLEAN "PUBLIC"
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}
