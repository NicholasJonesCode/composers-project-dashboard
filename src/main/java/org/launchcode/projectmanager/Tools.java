package org.launchcode.projectmanager;

import org.launchcode.projectmanager.models.Project;
import org.launchcode.projectmanager.models.data.ProjectDoa;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tools {

    @Autowired
    public ProjectDoa projectDoa;

    public static String makeSHA256HashString(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(message.getBytes("UTF-8"));
            return DatatypeConverter.printHexBinary(hash);
        } catch (Exception ex) {
            ex.printStackTrace();
        } return null;
    }

    public static boolean checkPassword (String password, String currentHashedPassword) {
        return makeSHA256HashString(password).equals(currentHashedPassword);
    }


    //LOL MADE A NEW AND COOLER ONE XDDD
    public static List<Project> getLastXProjects(List<Project> projectList, Integer x) {
        if (projectList.isEmpty()) {
        }

        if (projectList.size() <= x) {

            List<Project> finalList = new ArrayList<>();

            for (int i = 1; i <= (projectList.size()); i++) {
                Project newProject = projectList.get(projectList.size() - i);
                finalList.add(newProject);
            }

            return finalList;
        }

        List<Project> finalList = new ArrayList<>();

        for (int i = 1; i < (x+1); i++) {
            Project newProject = projectList.get(projectList.size() - i);
            finalList.add(newProject);
        }

        return finalList;
    }

}
