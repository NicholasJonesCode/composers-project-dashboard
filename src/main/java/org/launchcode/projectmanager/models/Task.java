package org.launchcode.projectmanager.models;

import org.aspectj.lang.annotation.DeclareError;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Entity
public class Task {

    @Id
    @GeneratedValue
    private int id;

    @NotNull
    @Size(min = 1, message = "Description can't be empty")
    private String description;


    private LocalDate dueDate;

    //Many(tasks)to One(project)
    @ManyToOne(fetch = FetchType.EAGER)
    private Project project;

    public Task() { }


    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
