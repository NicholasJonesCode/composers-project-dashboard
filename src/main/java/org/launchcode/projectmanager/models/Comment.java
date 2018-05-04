package org.launchcode.projectmanager.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Comment {

    @Id
    @GeneratedValue
    private int id;

    @NotNull
    @Size(min = 1, max = 2000)
    private String contents;

    //Many(comments) to One(User)
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    //Many(comments)to One(project)
    @ManyToOne(fetch = FetchType.EAGER)
    private Project project;


    public Comment() {}


    public int getId() {
        return id;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
