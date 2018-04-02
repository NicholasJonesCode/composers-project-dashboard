package org.launchcode.projectmanager;

import org.launchcode.projectmanager.models.Project;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class Tools {


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


    //LOL MADE A NEW AND COOLER ONE XDDD;
    //update: I MADE ANOTHER ONE!!!!11 This one is different, and a bit easier to understand i think, but it takes a bit more code, for catching stuff. I'll keep the former one in comments tho.
    public static List<Project> getLastXProjects(List<Project> projectList, Integer x) {
        //if some turd puts in 0 or negative, return a nothing list
        if (x <= 0) {
            return new ArrayList<>();
        }
        //just return the list if none or 1 element
        if (projectList.isEmpty() || projectList.size() == 1) {
            return projectList;
        }

        List<Project> finalList = new ArrayList<>();

        for (int i = projectList.size() - 1; i >= 0; i--) {
            Project newProject = projectList.get(i);
            finalList.add(newProject);
        }

        Integer idx;
        if (finalList.size() < x) {
            idx = x - finalList.size() + 1;
        } else {
            idx = x;
        }

        return finalList.subList(0, idx);

//        This one does work, but is older:
//        if (projectList.isEmpty()) {
//        }
//
//        List<Project> finalList = new ArrayList<>();
//
//        if (projectList.size() <= x) {
//
//            for (int i = projectList.size() - 1; i >= 0; i--) {
//                Project newProject = projectList.get(i);
//                finalList.add(newProject);
//            }
//
//            return finalList;
//        }
//
//        for (int i = 1; i < (x+1); i++) {
//            Project newProject = projectList.get(projectList.size() - i);
//            finalList.add(newProject);
//        }
//
//        return finalList;
    }

}
