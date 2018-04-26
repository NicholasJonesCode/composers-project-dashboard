package org.launchcode.projectmanager;

import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

@Component
public class isDirOrFile {

    public static boolean isDirectory(Path path) {
        File file = new File(path.toString());
        return file.isDirectory();
    }

    public static boolean isFile(Path path) {
        File file = new File(path.toString());
        return file.isFile();
    }
}
