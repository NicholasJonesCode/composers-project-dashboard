package org.launchcode.projectmanager;

import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.util.HashMap;

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
        if (makeSHA256HashString(password).equals(currentHashedPassword)) {
            return true;
        } return false;
    }

}
