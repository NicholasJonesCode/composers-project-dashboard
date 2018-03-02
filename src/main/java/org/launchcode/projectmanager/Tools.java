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
    ProjectDoa projectDoa;

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
        if (makeSHA256HashString(password).equals(currentHashedPassword)) {
            return true;
        } return false;
    }

    public static List<Project> getLastProjectsUpTo3(List<Project> projectList) {
        if(projectList.isEmpty()) {

        }

        if(projectList.size() <= 3) {
            return projectList;
        }

        Project lastProject = projectList.get(projectList.size() - 1);
        Project secondToLastProject = projectList.get(projectList.size() - 2);
        Project thirdToLastProject = projectList.get(projectList.size() - 3);
        List<Project> finalList = new ArrayList<>();
        finalList.add(lastProject);
        finalList.add(secondToLastProject);
        finalList.add(thirdToLastProject);

        return finalList;

    }

}
