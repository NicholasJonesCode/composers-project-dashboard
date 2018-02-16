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

    public boolean checkPassword (String password, String hashedPassword) {
        if (makeSHA256HashString(password) == hashedPassword) {
            return true;
        } return false;
    }

}
