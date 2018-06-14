package org.launchcode.projectmanager.models.DropboxAPI;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DropboxMethods {

    public static boolean folderExists(String pathWithAppFolderAsRoot, DbxClientV2 dbxClient) throws DbxException {

        try {
            dbxClient.files().getMetadata(pathWithAppFolderAsRoot);
        }
        catch (GetMetadataErrorException e) {
            if (e.errorValue.isPath()) {
                LookupError le = e.errorValue.getPathValue();
                if (le.isNotFound()) {
                    System.out.println("Path doesn't exist in the 'Composer's Dashboard App Files' folder: " + pathWithAppFolderAsRoot);
                    return false;
                }
            }
        }
        return true;
    }


    public static void createProjectFolder(String projectTitle, DbxClientV2 dbxClient) {

        try {
            dbxClient.files().createFolderV2("/" + projectTitle);
            System.out.println("Created project folder in app folder: " + projectTitle);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }


    public static void createProjectFolderIfDoesntExist(String projectTitle, DbxClientV2 dbxClient) throws DbxException {

        if (!folderExists("/" + projectTitle, dbxClient)) {
            createProjectFolder(projectTitle, dbxClient);
            System.out.println("Created project folder in app folder: " + projectTitle);
        }
    }


    public static boolean isFile(Metadata metadata) {

        return metadata instanceof FileMetadata;
    }


    public static boolean isFolder(Metadata metadata) {

        return metadata instanceof FolderMetadata;
    }


    public static List<FileMetadata> getOnlyFilesInPath(String path, DbxClientV2 client) throws DbxException {

        ListFolderResult folderResult = client.files().listFolder(path);
        List<Metadata> appRootData = folderResult.getEntries();

        List<FileMetadata> files = new ArrayList<>();

        for (Metadata metadata : appRootData) {
            if (DropboxMethods.isFile(metadata)) {
                files.add((FileMetadata) metadata);
            }
        }

        return files;
    }


    public static FileMetadata uploadToDropboxPathWithOverwriteMode(Path localFilePath, DbxClientV2 client, String dbxPathAndFilename) throws IOException, DbxException {

        File fileToUpload = new File(localFilePath.toAbsolutePath().toString());
        FileInputStream input = new FileInputStream(fileToUpload);

        FileMetadata fileMetadata = client.files().uploadBuilder(dbxPathAndFilename)
                .withMode(WriteMode.OVERWRITE)
                .withClientModified(new Date(fileToUpload.lastModified()))

                    .uploadAndFinish(input);

        return fileMetadata;
    }

    public static FileMetadata uploadToProjectFolderWithOverwriteMode(Path localFilePath, DbxClientV2 client, String projectFolder) throws IOException, DbxException {

        return uploadToDropboxPathWithOverwriteMode(localFilePath, client, String.format("/%s/%s", projectFolder, localFilePath.getFileName()));
    }


    public static boolean fileExistsInProjectFolder(DbxClientV2 dbxClient, Path localFilePath, String projectFolder) throws DbxException {

        List<FileMetadata> files = getOnlyFilesInPath("/" + projectFolder, dbxClient);

        for (FileMetadata file : files) {
            if(localFilePath.toFile().getName() == file.getName()) {
                return true;
            }
        }
        return false;
    }

    public static long getTotalFreeSpaceInBytes(DbxClientV2 dbxClient) throws DbxException {

        long totalSpace = dbxClient.users().getSpaceUsage().getAllocation().getIndividualValue().getAllocated();
        long usedSpace = dbxClient.users().getSpaceUsage().getUsed();

        return totalSpace - usedSpace;
    }

}
