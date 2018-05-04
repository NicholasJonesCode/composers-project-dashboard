package org.launchcode.projectmanager.models;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.launchcode.projectmanager.models.enums.Mode;
import org.launchcode.projectmanager.models.enums.MusicKeyType;
import org.launchcode.projectmanager.models.enums.TimeSignatureDenominator;
import org.launchcode.projectmanager.models.enums.TimeSignatureNumerator;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Project {

    @Id
    @GeneratedValue
    private int id;

    @Size(min = 1, message = "Title cannot be empty!")
    private String title;

    @Size(max = 1000)
    private String subtitle;

    @Size(max = 1000)
    private String lyricist;

    private MusicKeyType primary_music_key;

    private MusicKeyType secondary_music_key;

    private Mode mode;

    @Size(max = 1000)
    private String genre;

    private TimeSignatureNumerator primary_time_sig_num;

    private TimeSignatureDenominator primary_time_sig_den;

    private TimeSignatureNumerator secondary_time_sig_num;

    private TimeSignatureDenominator secondary_time_sig_den;

    //private String starting_tempo;

    //private String secondary_temp

    @Size(max = 1000000)
    private String instruments; //text area

    @Size(max = 1500000000)
    private String notes; //text area

    private boolean isPublic;

    //Collection of Paths
    @ElementCollection(targetClass = Path.class)
    @Convert(converter = PathConverter.class)
    private List<Path> file_paths = new ArrayList<>();

    //Many(projects)to One(user)
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    //One(project)to Many(tasks)
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Task> tasks = new ArrayList<>();

    //One(project)to Many(comments)
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Comment> comments = new ArrayList<>();


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

    //SUBTITLE
    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    //LYRICIST
    public String getLyricist() {
        return lyricist;
    }

    public void setLyricist(String lyricist) {
        this.lyricist = lyricist;
    }

    //PRIMARY MUSIC KEY
    public MusicKeyType getPrimary_music_key() {
        return primary_music_key;
    }

    public void setPrimary_music_key(MusicKeyType primary_music_key) {
        this.primary_music_key = primary_music_key;
    }

    //SECONDARY MUSIC KEY
    public MusicKeyType getSecondary_music_key() {
        return secondary_music_key;
    }

    public void setSecondary_music_key(MusicKeyType secondary_music_key) {
        this.secondary_music_key = secondary_music_key;
    }

    //MODE
    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    //GENRE
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    //PRIMARY TIME SIG (NUM and DEN)
    public TimeSignatureNumerator getPrimary_time_sig_num() {
        return primary_time_sig_num;
    }

    public void setPrimary_time_sig_num(TimeSignatureNumerator primary_time_sig_num) {
        this.primary_time_sig_num = primary_time_sig_num;
    }

    public TimeSignatureDenominator getPrimary_time_sig_den() {
        return primary_time_sig_den;
    }

    public void setPrimary_time_sig_den(TimeSignatureDenominator primary_time_sig_den) {
        this.primary_time_sig_den = primary_time_sig_den;
    }

    //SECONDARY TIME SIG (NUM and DEN)
    public TimeSignatureNumerator getSecondary_time_sig_num() {
        return secondary_time_sig_num;
    }

    public void setSecondary_time_sig_num(TimeSignatureNumerator secondary_time_sig_num) {
        this.secondary_time_sig_num = secondary_time_sig_num;
    }

    public TimeSignatureDenominator getSecondary_time_sig_den() {
        return secondary_time_sig_den;
    }

    public void setSecondary_time_sig_den(TimeSignatureDenominator secondary_time_sig_den) {
        this.secondary_time_sig_den = secondary_time_sig_den;
    }

    //INSTRUMENTS
    public String getInstruments() {
        return instruments;
    }

    public void setInstruments(String instruments) {
        this.instruments = instruments;
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

    //FILE PATHS LIST
    public List<Path> getFile_paths() {
        //return file_paths.stream().map(Paths::get).collect(Collectors.toList()); //idek what this is for lol
        return file_paths;
    }

    public void setFile_paths(List<Path> file_paths) {
        this.file_paths = file_paths;
    }

    public void addFile_pathString(String path) {
        Path newPath = Paths.get(path);
        file_paths.add(newPath);
    }

    public void addFile_pathPath(Path path) {
        file_paths.add(path);
    }

    public void deleteFilePath(Path path) {
        file_paths.remove(path);
    }

    //MANY TO ONE USER
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    //MANY TASKS TO ONE USER

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }

    //MANY COMMENTS TO ONE USER

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
}
