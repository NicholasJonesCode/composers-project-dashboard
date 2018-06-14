package org.launchcode.projectmanager.models;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.launchcode.projectmanager.UniqueUsername;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue
    private int id;

    @NotNull
    @Size(min=3, max = 35)
    @UniqueUsername
    private String username;

    @NotNull
    @Size(min = 6, max = 255)
    private String password;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private byte[] avatarImage;

    private String dbxAccessToken;

    //One (User) to Many (projects)
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Project> projects = new ArrayList<>();

    //One (User) to Many (comments)
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private List<Comment> comments = new ArrayList<>();


    public User() {}

    public List<Project> getProjects() {
        return projects;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(byte[] avatarImage) {
        this.avatarImage = avatarImage;
    }

    public String getDbxAccessToken() {
        return dbxAccessToken;
    }

    public void setDbxAccessToken(String dbxAccessToken) {
        this.dbxAccessToken = dbxAccessToken;
    }
}
