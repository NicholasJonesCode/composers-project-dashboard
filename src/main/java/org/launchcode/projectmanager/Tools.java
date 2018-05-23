package org.launchcode.projectmanager;

import org.apache.commons.io.FileUtils;
import org.launchcode.projectmanager.models.CloudConvertAPI.CCAPI_Implement;
import org.launchcode.projectmanager.models.Project;
import org.pegdown.PegDownProcessor;
import org.springframework.web.client.HttpClientErrorException;
import org.tautua.markdownpapers.parser.ParseException;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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

    public static List<Project> sortProjectsNewestToOldest(List<Project> projectList) {

        List<Project> finalList = new ArrayList<>();

        for (int i = projectList.size() - 1; i >= 0; i--) {
            Project newProject = projectList.get(i);
            finalList.add(newProject);
        }

        return finalList;
    }


    public static String getRemoteReadmeAndConvertToHTMLString() throws IOException, ParseException {

        URL url = new URL("https://raw.githubusercontent.com/NicholasJonesCode/composers-project-dashboard/master/README.md");
        String outputPath = System.getProperty("user.dir") + File.separator + "src\\main\\resources\\files\\index.md";
        File fileInput = new File(outputPath);

        FileUtils.copyURLToFile(url, fileInput, 10000, 10000);

        String mdString = FileUtils.readFileToString(fileInput, "UTF-8");

        PegDownProcessor pegDownProcessor = new PegDownProcessor();
        String result = pegDownProcessor.markdownToHtml(mdString);

        return result;
    }

    public static String getReadmeHtmlAllMethods() throws IOException, ParseException {
        //This method will try the API with the first key, then the second key, then with the local method

        String htmlReadmeString;
        try {       //1. try this key first
            htmlReadmeString = CCAPI_Implement.getHTMLString("Nw_KX8DDBah89cWmFDL00xl3sAMp-idcCGGkcoe9iluM2eywWpLSNRrXVx1F0DJVfmv8Lpu8KWm1KvgV02xEiQ");
        } catch (HttpClientErrorException e) {
            try {             //2. use my secondary key if the first one doesn't work lol
                htmlReadmeString = CCAPI_Implement.getHTMLString("6Z5LV1mfoLKGS6LeYQgRro5k_mj5qzBM9F7EQ6pECtVe3B-9nwuu0Dy6Fvq5eQmyCm9RcJknaZXd0BG8NTmGig");
            } catch (HttpClientErrorException e2) {   //3. if the api is being a piece of crap, show the info why and implement local md>html conversion method
                htmlReadmeString = "429 null Cloud Convert API, too many requests, or if 402 null, then I used up all my conversion minutes: " + e2.toString() +
                        " ...<a href=\"https://cloudconvert.com/api/conversions#bestpractices\">Click here for more info on that</a> <br/> " +
                        " ...Below is the same file from the same source, grabbed from the remote then converted locally instead of with the CC API: <br/>" +
                        Tools.getRemoteReadmeAndConvertToHTMLString();
            }
        }

        return htmlReadmeString;
    }
}
