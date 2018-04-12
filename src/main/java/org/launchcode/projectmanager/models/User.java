package org.launchcode.projectmanager.models;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
    @Size(min=3, max = 25)
    @Column(unique = true)
    private String username;

    @NotNull
    @Size(min = 6, max = 256)
    private String password;

    //One (User) to Many (projects)
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Project> projects = new ArrayList<>();

    //One (User) to Many (comments)
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private List<Comment> comments = new ArrayList<>();


    public User() { }

    public void addProject(Project project) {
        this.projects.add(project);
    }

    public List<Project> getProjects() {
        return projects;
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
}
