package org.launchcode.projectmanager;

import com.dropbox.core.DbxException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectManagerApplication {

	public static void main(String[] args) throws DbxException {

		SpringApplication.run(ProjectManagerApplication.class, args);

        //TESTING

//        DbxRequestConfig config = DbxRequestConfig.newBuilder("Composer's Project Dashboard/1.0").build();
//        DbxClientV2 client = new DbxClientV2(config, "3ig-1SuT0YAAAAAAAAAASMDaXdgQ3qhd4Xpeu9K6SLjKkT2h_eydu8eBha4_ShpM");
//
//        System.out.println(Tools.dateObjectToReadableDateTimeString( ((FileMetadata) client.files().getMetadata("/newTestdbxfolder1RENAME2/1967.jpg")).getClientModified()  ));
//
//        FullAccount currentAccount = client.users().getCurrentAccount();
//        System.out.println(currentAccount.toStringMultiline());
//
//        ListFolderResult folderResult = client.files().listFolder("");
//        List<Metadata> appRootData = folderResult.getEntries();
//        DeleteResult folder = client.files().deleteV2("/testfoldercreate");
//
//        System.out.println("All data for app folder: ");
//        for (Metadata entry : appRootData) {
//            if (DropboxMethods.isFile(entry)) {
//                System.out.println("I found a file! - " + entry.getName());
//            } else if (DropboxMethods.isFolder(entry)) {
//                System.out.println("I found a folder! - " + entry.getName());
//            }
//        }
//
//        ListFileRequestsResult listFileRequestsResult = client.fileRequests().list();
//        System.out.println("ListFileRequestsResult listFileRequestsResult = client.fileRequests().list() ::");
//        System.out.println(listFileRequestsResult.toStringMultiline());
//
//        URI folderPath = new URI("https://www.dropbox.com/home/Apps/Composer's%20Dashboard%20App%20Files");
//        DesktopApi.browse(folderPath);
//
//        System.out.println(Tools.isFile(Paths.get("F:\\test\\test-doc.rtf")));
//
//        Path testPath = Paths.get("F:\\Documents\\f10606678.rtf");
//
//        String size = FileUtils.byteCountToDisplaySize(testPath.toFile().length());
//        System.out.println("teh flac size in cool formate!!! (Apache): " + size);
//
//        String size2 = Tools.readableFileSize(testPath.toFile().length());
//        System.out.println("using stack method: " + size2);
//
//        System.out.println("It appears that the method from stack overflow is better!! (so far)");
//
//
//        SpaceUsage spaceUsage = client.users().getSpaceUsage();
//
//        long allocated;
//        if (spaceUsage.getAllocation().isTeam()) {
//            System.out.println("Team allocation:");
//            allocated = spaceUsage.getAllocation().getTeamValue().getAllocated();
//        } else {
//            System.out.println("Individual allocation:");
//            allocated = spaceUsage.getAllocation().getIndividualValue().getAllocated();
//        }
//
//        System.out.println("Total: " + allocated);
//        long used = spaceUsage.getUsed();
//        System.out.println("Used: " + used);
//        System.out.println("Free: " + (allocated - used));
//
//
//        String stop = "stoppage for debugging";
	}
}
