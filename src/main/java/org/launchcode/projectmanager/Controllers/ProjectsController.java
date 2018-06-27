package org.launchcode.projectmanager.Controllers;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import org.launchcode.projectmanager.DesktopApi;
import org.launchcode.projectmanager.Tools;
import org.launchcode.projectmanager.models.DropboxAPI.DropboxMethods;
import org.launchcode.projectmanager.models.Project;
import org.launchcode.projectmanager.models.Task;
import org.launchcode.projectmanager.models.User;
import org.launchcode.projectmanager.models.data.ProjectDao;
import org.launchcode.projectmanager.models.data.TaskDao;
import org.launchcode.projectmanager.models.data.UserDao;
import org.launchcode.projectmanager.models.enums.Mode;
import org.launchcode.projectmanager.models.enums.MusicKeyType;
import org.launchcode.projectmanager.models.enums.TimeSignatureDenominator;
import org.launchcode.projectmanager.models.enums.TimeSignatureNumerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("project")
public class ProjectsController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private TaskDao taskDao;

    //dropbox config for use in this controller
    private DbxRequestConfig config = DbxRequestConfig.newBuilder("Composer's Project Dashboard/1.0").build();

    /*
    * SECTIONS:
    * 1. DASHBOARD
    * 2. PROJECT CREATION
    * 3. PROJECT EDIT
    * 4. PROJECT EDIT NOTE
    * 5. PROJECT OVERVIEW
    * 6. ALL PROJECT LIST VIEW
    * 7. TASK CREATION
    * 8. TASK DELETE
    * 9. FILES, ADD PATH
    * 10. FILES, OPEN PATH
    * 11. FILES DELETE PATH
    * 12. DROPBOX METHODS
    */

    // ----------------- 1. DASHBOARD -------------------
    @RequestMapping(value = "dashboard", method = RequestMethod.GET)
    public String displayDashboard(Model model, HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUserObj");
        User theUser = userDao.findOne(currentUser.getId());
        List<Project> last3projects = Tools.getLastXProjects(theUser.getProjects(), 3);

        model.addAttribute("title", currentUser.getUsername() + "'s Dashboard");
        model.addAttribute("projectList", last3projects);

        return "project/dashboard";
    }

    // ----------------- 2. PROJECT CREATION -------------------
    @RequestMapping(value = "create-project", method = RequestMethod.GET)
    public String displayCreateProject(Model model) {

        if (!model.containsAttribute("project")) {
            model.addAttribute(new Project());
        }
        model.addAttribute("title", "Add Composition");
        model.addAttribute("allModes", Mode.values());
        model.addAttribute("allMusicKeyTypes", MusicKeyType.values());
        model.addAttribute("allTimeSigNum", TimeSignatureNumerator.values());
        model.addAttribute("allTimeSigDen", TimeSignatureDenominator.values());

        return "project/create-project";
    }

    @RequestMapping(value = "create-project", method = RequestMethod.POST)
    public ModelAndView processCreateProject(@ModelAttribute @Valid Project newProject, Errors errors, Model model, @RequestParam String isPublicPrivate,
                                              final RedirectAttributes redirectAttributes, BindingResult bindingResult, HttpSession session) {

        //Project name must be unique
        if (Tools.projectTitleExists(newProject.getTitle())) {
            redirectAttributes.addFlashAttribute("project", newProject);
            redirectAttributes.addFlashAttribute("errorMessage", "You already have a project by that name");
            return new ModelAndView("redirect:/project/create-project");
        }

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.project", bindingResult);
            redirectAttributes.addFlashAttribute("project", newProject);
            return new ModelAndView("redirect:/project/create-project");
        }

        if (isPublicPrivate.equals("public")) {
            newProject.setPublic(true);
        } else {
            newProject.setPublic(false);
        }

        User currentUser = (User) session.getAttribute("currentUserObj");
        User theUser = userDao.findOne(currentUser.getId());
        newProject.setUser(theUser);
        projectDao.save(newProject);

        redirectAttributes.addFlashAttribute("actionMessage", String.format("The project '%s' has been created", newProject.getTitle()));

        return new ModelAndView("redirect:/project/project-overview/" + newProject.getId());
    }

    // ----------------- 3. PROJECT EDIT -------------------
    @RequestMapping(value = "edit-project/{projectId}", method = RequestMethod.GET)
    public String editProject(@PathVariable int projectId, Model model, HttpSession session, final RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUserObj");

        if (currentUser.getId() != projectDao.findOne(projectId).getUser().getId()) {
            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to change this project");
            return "redirect:/project/dashboard";
        }

        if (!model.containsAttribute("project")) {
            model.addAttribute("project", new Project());
        }
        model.addAttribute("currentProject", projectDao.findOne(projectId));
        model.addAttribute("title", "Edit Composition");
        model.addAttribute("allModes", Mode.values());
        model.addAttribute("allMusicKeyTypes", MusicKeyType.values());
        model.addAttribute("allTimeSigNum", TimeSignatureNumerator.values());
        model.addAttribute("allTimeSigDen", TimeSignatureDenominator.values());

        return "project/edit-project";
    }

    @RequestMapping(value = "edit-project/{projectId}", method = RequestMethod.POST)
    public ModelAndView processEditProject(@Valid @ModelAttribute("project") Project editedProject, Errors errors, @PathVariable int projectId, @RequestParam String isPublicPrivate,
                                           final RedirectAttributes redirectAttributes, BindingResult bindingResult, HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUserObj");
        Project projectToEdit = projectDao.findOne(projectId);
        String redirectToOverview = "redirect:/project/project-overview/" + projectId;
        String redirectToEditMode = "redirect:/project/edit-project/" + projectId;

        if (currentUser.getId() != projectDao.findOne(projectId).getUser().getId()) {
            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to change this project");
            return new ModelAndView("redirect:/project/dashboard");
        }

        if (Tools.projectTitleExists(editedProject.getTitle()) && Tools.projectTitleExists(projectToEdit.getTitle()) && !projectToEdit.getTitle().equals(editedProject.getTitle())) {
            redirectAttributes.addFlashAttribute("project", editedProject);
            redirectAttributes.addFlashAttribute("uniqueTitleError", "You already have a project by that name");
            return new ModelAndView(redirectToEditMode);
        }


        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.project", bindingResult);
            redirectAttributes.addFlashAttribute("project", editedProject);
            return new ModelAndView(redirectToEditMode);
        }

        //Rename the Dropbox project folder as well if they have one
        DbxClientV2 dbxClient = new DbxClientV2(config, currentUser.getDbxAccessToken());
        try {
            if (!projectToEdit.getTitle().equals(editedProject.getTitle())  &&
                    currentUser.getDbxAccessToken() != null  &&
                        DropboxMethods.dbxFolderExists("/" + projectToEdit.getTitle(), dbxClient)) {

                try {  //DROPBOX API CALL
                    DropboxMethods.renameDbxProjectFolder(dbxClient, projectToEdit.getTitle(), editedProject.getTitle());
                } catch (DbxException e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("actionMessage", "Error renaming your Dropbox project folder: " + e.toString() + ". <br/> All other editing was successful");
                    return new ModelAndView(redirectToOverview);
                }
            }
        } catch (DbxException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("actionMessage", "Error finding your Dropbox folder in order to rename it: " + e.toString() + ". All other editing was successful");
            return new ModelAndView(redirectToOverview);
        }

        projectToEdit.setTitle(editedProject.getTitle());
        projectToEdit.setSubtitle(editedProject.getSubtitle());
        projectToEdit.setLyricist(editedProject.getLyricist());
        projectToEdit.setPrimary_music_key(editedProject.getPrimary_music_key());
        projectToEdit.setSecondary_music_key(editedProject.getSecondary_music_key());
        projectToEdit.setMode(editedProject.getMode());
        projectToEdit.setGenre(editedProject.getGenre());
        projectToEdit.setPrimary_time_sig_num(editedProject.getPrimary_time_sig_num());
        projectToEdit.setPrimary_time_sig_den(editedProject.getPrimary_time_sig_den());
        projectToEdit.setSecondary_time_sig_num(editedProject.getSecondary_time_sig_num());
        projectToEdit.setSecondary_time_sig_den(editedProject.getSecondary_time_sig_den());
        projectToEdit.setInstruments(editedProject.getInstruments());
        projectToEdit.setNotes(editedProject.getNotes());
        if (isPublicPrivate.equals("public")) {
            projectToEdit.setPublic(true);
        } else {
            projectToEdit.setPublic(false);
        }

        projectDao.save(projectToEdit);
        redirectAttributes.addFlashAttribute("actionMessage", "Successfully Edited Info");

        return new ModelAndView(redirectToOverview);
    }

    // ----------------- 4. PROJECT EDIT NOTES -------------------
    @RequestMapping(value = "update-notes/{projectId}", method = RequestMethod.POST)
    public String updateNotes(@PathVariable int projectId, @RequestParam String notes, final RedirectAttributes redirectAttributes, HttpSession session) {

        Project theProject = projectDao.findOne(projectId);
        User currentUser = (User) session.getAttribute("currentUserObj");

        if (currentUser.getId() != theProject.getUser().getId()) {
            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to change this project");
            return "redirect:/project/dashboard";
        }

        theProject.setNotes(notes);

        projectDao.save(theProject);

        redirectAttributes.addFlashAttribute("actionMessage", "Saved changes");
        return "redirect:/project/project-overview/" + projectId;
    }

    // ----------------- 5. PROJECT OVERVIEW -------------------
    @RequestMapping(value = "project-overview/{projectId}", method = RequestMethod.GET)
    public String projectOverview(@PathVariable int projectId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        Project theProject = projectDao.findOne(projectId);
        User currentUser = (User) session.getAttribute("currentUserObj");

        //if it's private and there's no user, return login
        if (currentUser == null && !theProject.isPublic()) {
            return "redirect:/user/login";
        }
        //if there's a user, and that user is not the owner of the project, and the project is private, send them back to their dashboard
        if (currentUser != null && currentUser.getId() != theProject.getUser().getId() && !theProject.isPublic()) {
            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to view that, rawr");
            return "redirect:/project/dashboard";
        }

        if (!model.containsAttribute("task")) {
            model.addAttribute("task", new Task());
        }

        model.addAttribute("project", theProject);
        model.addAttribute("tasks", theProject.getTasks());
        model.addAttribute("allPaths", theProject.getPaths());
        model.addAttribute("title", "Overview");

        return "project/project-overview";
    }

    // ----------------- 6. ALL PROJECTS LIST VIEW -------------------
    @RequestMapping(value = "all-projects", method = RequestMethod.GET)
    public String displayAllProjects(Model model, HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUserObj");

        model.addAttribute("title", "All Projects");
        model.addAttribute("allProjects", projectDao.findByUserId(currentUser.getId()));

        return "project/all-projects";
    }

    // PROJECT - DELETE
    @RequestMapping(value = "delete-project/{projectId}", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView deleteProject(@PathVariable int projectId, HttpSession session, final RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUserObj");

        if (currentUser.getId() != projectDao.findOne(projectId).getUser().getId()) {

            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to change this project");
            return new ModelAndView("redirect:/project/dashboard");
        }

        String projectName = projectDao.findOne(projectId).getTitle();
        projectDao.delete(projectId);

        redirectAttributes.addFlashAttribute("actionMessage", String.format("The project '%s' has been removed permanently", projectName));

        return new ModelAndView("redirect:/project/dashboard");
    }

    // ----------------- 7. TASK CREATION -------------------
    @RequestMapping(value = "create-task/{projectId}", method = RequestMethod.POST)
    public ModelAndView createTask(@Valid @ModelAttribute("task") Task task,
                                   Errors errors,
                                   @PathVariable int projectId,
                                   RedirectAttributes redirectAttributes,
                                   final BindingResult bindingResult,
                                   HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUserObj");

        if (currentUser.getId() != projectDao.findOne(projectId).getUser().getId()) {

            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to change this project");
            return new ModelAndView("redirect:/project/dashboard");
        }

        String path = "redirect:/project/project-overview/" + projectId;

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.task", bindingResult);
            redirectAttributes.addFlashAttribute("task", task);
            return new ModelAndView(path);
        }

        Project theProject = projectDao.findOne(projectId);
        task.setProject(theProject);
        taskDao.save(task);

        return new ModelAndView(path);
    }

    // ----------------- 8. TASK DELETE -------------------
    @RequestMapping(value = "delete-task/{taskId}/{projectId}", method = RequestMethod.POST)
    public ModelAndView deleteTask(@PathVariable int taskId, @PathVariable int projectId, HttpSession session, RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUserObj");

        if (currentUser.getId() != projectDao.findOne(projectId).getUser().getId()) {
            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to change this project");
            return new ModelAndView("redirect:/project/dashboard");
        }

        taskDao.delete(taskId);

        return new ModelAndView("redirect:/project/project-overview/" + projectId);
    }

    // ----------------- 9. FILES, ADD PATH -------------------
    @RequestMapping(value = "add-path/{projectId}", method = {RequestMethod.GET, RequestMethod.POST})
    public String addPath(@PathVariable int projectId, @RequestParam String pathString, final RedirectAttributes redirectAttributes, HttpSession session) {

        Project theProject = projectDao.findOne(projectId);
        File fileToAdd = new File(pathString);
        User currentUser = (User) session.getAttribute("currentUserObj");
        String redirectToProject = "redirect:/project/project-overview/" + projectId;

        if (currentUser.getId() != theProject.getUser().getId()) {
            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to change this project");
            return "redirect:/project/dashboard";
        }

        if (!fileToAdd.exists()) {
            redirectAttributes.addFlashAttribute("actionMessage", "Path doesn't exist");
            return redirectToProject;
        }

        if (projectDao.findOne(projectId).getPaths().contains(fileToAdd.toPath())) {
            redirectAttributes.addFlashAttribute("actionMessage", "You've already added this path");
            return redirectToProject;
        }

        theProject.addPathString(pathString);
        projectDao.save(theProject);
        redirectAttributes.addFlashAttribute("actionMessage", String.format("The path, '%s' was successfully added to your Files/Directories list", pathString));

        return redirectToProject;
    }

    // ----------------- 10. FILES, OPEN PATH -------------------
    @RequestMapping(value = "open-path", method = {RequestMethod.GET, RequestMethod.POST})
    public String openFile(@RequestParam(required = false) String path, @RequestParam(required = false) String redirectPath,
                           RedirectAttributes redirectAttributes, HttpServletRequest request) {

        //if some cheeki breeki tries to go to that path...
        if (request.getMethod().equals("GET")) {
            redirectAttributes.addFlashAttribute("actionMessage", "That URL is not available right now.");
            return "redirect:/project/dashboard";
        }

        File fileToOpen = new File(path);
        DesktopApi.open(fileToOpen);

        return "redirect:" + redirectPath;
    }

    // ----------------- 11. FILES, DELETE PATH -------------------
    @RequestMapping(value = "delete-path/{projectId}", method = RequestMethod.POST)
    public String deletePath(@PathVariable int projectId, @RequestParam Path path, final RedirectAttributes redirectAttributes, HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUserObj");
        Project currentProject = projectDao.findOne(projectId);

        if (currentUser.getId() != currentProject.getUser().getId()) {
            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to change this project");
            return "redirect:/project/dashboard";
        }

        currentProject.deleteFilePath(path);
        projectDao.save(currentProject);

        redirectAttributes.addFlashAttribute("actionMessage", String.format("Path '%s' has been removed", path.toString()));
        return "redirect:/project/project-overview/" + projectId;
    }

    // ---------------------------------- 12. DROPBOX METHODS --------------------------------------------

        //----------------- 12.1 DROPBOX UTILS PAGE -----------------
    @RequestMapping(value = "dropbox-utils/{projectId}", method = RequestMethod.GET)
    public String displayProjectDropboxUtilsPage(@PathVariable int projectId, Model model, HttpSession session, RedirectAttributes redirectAttributes)  {

        Project theProject = projectDao.findOne(projectId);
        User currentUser = (User) session.getAttribute("currentUserObj");

        //if there's a user, and that user is not the owner of the project, send them back to their dashboard
        if (currentUser != null && currentUser.getId() != theProject.getUser().getId()) {
            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to view that, rawr");
            return "redirect:/project/dashboard";
        }

        //if dropbox connected, pass in appropriate info, else redirect with message back to overview
        if (currentUser.getDbxAccessToken() != null) {
            //DROPBOX API CALLS
            DbxClientV2 dbxClient = new DbxClientV2(config, currentUser.getDbxAccessToken());

            //make a project folder if not already one
            try {
                DropboxMethods.createProjectFolderIfDoesntExist(theProject.getTitle(), dbxClient);
            } catch (DbxException de) {
                de.printStackTrace();
                model.addAttribute("actionMessage", String.format("An error occurred making a folder for your project: <br/> %s <br/> %s", de.getMessage(), de.toString()));
                return "project/dropbox-utils";
            }

            //get any files from already existing project folder
            List<FileMetadata> dbxProjectFiles = new ArrayList<>(); //this list will still pass into the template regardless, empty or not, so as not to cause an error...
            try {
                dbxProjectFiles.addAll(DropboxMethods.getOnlyFilesInPath("/" + theProject.getTitle(), dbxClient));
            } catch (DbxException e) {
                e.printStackTrace();
                model.addAttribute("actionMessage", String.format("An error occurred files in the folder for your project: <br/> %s <br/> %s", e.getMessage(), e.toString()));
                return "project/dropbox-utils";
            }
            model.addAttribute("dbxProjectFiles", dbxProjectFiles); //...like I said

            try { //Pass in their account's free space for the user to see:
                model.addAttribute("freeSpace", DropboxMethods.getTotalFreeSpaceInBytes(dbxClient));
            } catch (DbxException e) {
                e.printStackTrace();   //don't hold it up if you cant get it, just display as-is
                model.addAttribute("freeSpace", "Error in getting your info!!: " + e.toString());
            }

        } else  {
            redirectAttributes.addFlashAttribute("actionMessage", "You have not connected your Dropbox account yet. Go to \"Profiles and Settings\" to do that.");
            return "redirect:/project/project-overview/" + projectId;
        }

        model.addAttribute("project", theProject);
        model.addAttribute("allProjectFilePaths", theProject.getFilePathsOnly());
        model.addAttribute("title", "Dropbox Utility");

        return "project/dropbox-utils";
    }

        //--------------12.2 ACTION RESPONSE PAGE---------------
    @RequestMapping(value = "dropbox-action-response", method = RequestMethod.GET)
    public String dropboxUploadResponse(Model model) {

        return "project/dropbox-action-response";
    }

        // ------------- 12.3 UPLOAD SELECTED, or UPLOAD ALL ----------
    @RequestMapping(value = "upload-files-to-dbx/{projectId}", method = RequestMethod.POST)
    public String uploadFilesToDbx(@PathVariable int projectId, @RequestParam(name = "projectFilesToUpload", required = false) List<Path> selectedProjectFilesToUpload,
                                   @RequestParam(name = "action") String actionValue, final RedirectAttributes redirectAttributes, HttpSession session, Model model) {

        Project theProject = projectDao.findOne(projectId);
        User currentUser = (User) session.getAttribute("currentUserObj");
        String redirectToDbxUtils = "redirect:/project/dropbox-utils/" + projectId;

        //Immediately, if wrong user
        if (currentUser.getId() != theProject.getUser().getId()) {
            redirectAttributes.addFlashAttribute("actionMessage", "You do not have permission to change this project");
            return "redirect:/project/dashboard";
        }

        //first checks if the action button pressed is Upload Selected, or Upload All, and decides what is uploaded based on that
        List<Path> projectFilesToUpload = new ArrayList<>();

        if (actionValue.equals("Upload Selected")) {

            if (selectedProjectFilesToUpload == null) {
                redirectAttributes.addFlashAttribute("actionMessage", "You did not select any files");
                return redirectToDbxUtils;
            } else {

                projectFilesToUpload.addAll(selectedProjectFilesToUpload);
            }

        } else if (actionValue.equals("Upload All Files")) {

            if (theProject.getFilePathsOnly() == null || theProject.getFilePathsOnly().isEmpty()) {

                redirectAttributes.addFlashAttribute("actionMessage", "There are no files to upload");
                return redirectToDbxUtils;
            } else {

                projectFilesToUpload.addAll(theProject.getFilePathsOnly());
            }
        }

        //Initialize Dropbox Client
        DbxClientV2 dbxClient = new DbxClientV2(config, currentUser.getDbxAccessToken());
        //As files are uploaded and errors occur, build a response page
        StringBuilder htmlUploadResponse = new StringBuilder();

        try {
            DropboxMethods.createProjectFolderIfDoesntExist(theProject.getTitle(), dbxClient);
        } catch (DbxException de) {
            de.printStackTrace();
            redirectAttributes.addFlashAttribute("actionMessage", String.format("An error occurred making a folder for your project: <br/> %s <br/> %s", de.getMessage(), de.toString()));
            return redirectToDbxUtils;
        }

        //Establish title and give button to go the dbx folder
        htmlUploadResponse.append(String.format("<h1> Uploading from Computer to Dropbox project folder \"%s\" </h1>", theProject.getTitle()));
        htmlUploadResponse.append(String.format("<form action=\"https://www.dropbox.com/home/Apps/Composer's Dashboard App Files/%s\" method=\"get\" target=\"_blank\"> "+
                                                    "<input class=\"dbx-button\" type=\"submit\" value=\"Go To Folder\" /> " +
                                                "</form> <br/>", theProject.getTitle()));

        for (Path localFilePath : projectFilesToUpload) {

            htmlUploadResponse.append(String.format("<h3> %s </h3>", localFilePath.getFileName().toString())); //file name header

            try {
                //determine if it exists first, THEN upload it, then recall again on the 'if' down there
                boolean fileExistedInProjectFolder = DropboxMethods.fileExistsInProjectFolder(dbxClient, localFilePath, theProject.getTitle());

                FileMetadata result = DropboxMethods.uploadToProjectFolderWithOverwriteMode(localFilePath, dbxClient, theProject.getTitle());
                System.out.println(String.format("FILE UPLOADED TO DROPBOX PATH IN APP FOLDER %s for User %s : ",
                        result.getPathDisplay(), dbxClient.users().getCurrentAccount().getName().getDisplayName())); //DBX API call to user account here
                System.out.println(result.toStringMultiline());

                if (fileExistedInProjectFolder) {

                    htmlUploadResponse.append(   //Custom upload success state for file-already-exists....
                                "<div style=\"color:Yellow\">  Success (with note)  <br/> " +
                                        "Note: File Already Exists in Dropbox project folder: The local file you tried to upload " +
                                        "conflicted with a file with the same name already in your Dropbox project folder. <br/> " +
                                        "The file in your Dropbox was overwritten by the local one, as stated in the usage notes. " +
                                "</div> <br/> ");
                } else {
                                            //....or not ^_^
                    htmlUploadResponse.append("<div style=\"color:LimeGreen\">  Complete success  </div> <br/> ");
                }

                htmlUploadResponse.append(
                                "Full local path: " + localFilePath.toAbsolutePath().toString() + " <br/> " +
                                "Size on Computer: " + Tools.readableFileSize(localFilePath.toFile().length()) + " <br/> " +
                                "Full file path from your Dropbox root: Apps/Composer's Dashboard App Files" + result.getPathDisplay() + " <br/> " +
                                "Size in Dropbox: " + Tools.readableFileSize(result.getSize()) + " <br/> " +
                                "=============================================================  "
                );

            } catch (FileNotFoundException fnfe) {
                System.out.println(fnfe.toString());
                fnfe.printStackTrace();

                htmlUploadResponse.append(String.format(
                    "<div style=\"color:Red\">Failure: <br/> " +
                        "FileNotFoundException: The file at the path \"%s\" Does not exist anymore, or was possibly renamed to something else "+
                    "</div> <br/> ============================================================= ", localFilePath.toAbsolutePath().toString()
                ));

            } catch (IOException ioe) {
                System.out.println(ioe.toString());
                ioe.printStackTrace();

                htmlUploadResponse.append(String.format(
                        "<div style=\"color:Red\">Failure: <br/>" +
                                "Input/Output Exception at path \"%s\" <br/>" +
                                " %s <br/> %s <br/> "+
                        "</div> <br/> ============================================================= " ,localFilePath.toAbsolutePath().toString(), ioe.getMessage(), ioe.toString()
                ));

            } catch (UploadErrorException upe) {
                //DBX UPLOAD ERROR
                System.out.println(upe.errorValue.toStringMultiline());
                upe.printStackTrace();

                htmlUploadResponse.append(String.format(
                        "<div style=\"color:Red\">Failure: <br/>" +
                            "Dropbox has given an Upload Error. Full message: %s " +
                        "</div> <br/>", upe.errorValue.toString()
                ));

                //Assess the cause, possibly
                long freeSpaceInBytes = 0;

                try {
                    freeSpaceInBytes = DropboxMethods.getTotalFreeSpaceInBytes(dbxClient);
                } catch (DbxException e) {
                    e.printStackTrace();
                    htmlUploadResponse.append(
                            "<div style=\"Aqua\"> In trying to assess a possibility of a lack of free space on your Dropbox, Dropbox threw the following error: <br/>" +
                            e.toString() + "<br/>" + "The upload process will end beyond this point. </div> <br/> "
                    );
                }

                if (localFilePath.toFile().length() > freeSpaceInBytes) {
                    htmlUploadResponse.append(
                            "<div style=\"Aqua\"> According to analysis, this file's size is bigger than your total free space, which is the mostly likely the cause of the error. <br/>" +
                            "Free up some space on your dropbox and try again. The upload process will end beyond this point. </div> <br/> " +
                            "============================================================= "
                    );
                    //Break off the uploading because there's no more space
                    break;

                } else {
                    htmlUploadResponse.append("============================================================= ");
                }

            } catch (DbxException de) {
                System.out.println(de.toString());
                de.printStackTrace();

                htmlUploadResponse.append(String.format(
                        "<div style=\"color:Red\">Failure: <br/>" +
                            "Dropbox has given an Upload Error. Full message: %s " +
                        "</div> <br/> =============================================================", de.getMessage()
                ));
            }
        }

        //Finally, list all the files the user tried to upload
        htmlUploadResponse.append(" <br/><br/>   <b><u> All local files you attempted to upload: </u></b>   <br/>");
        for (Path localFilePath : projectFilesToUpload) {
            htmlUploadResponse.append(localFilePath.toAbsolutePath().toString() + "<br/>");
        }
        htmlUploadResponse.append("<br/><br/>"); //couple of breaks to space from the back button

        redirectAttributes.addFlashAttribute("htmlResponse", htmlUploadResponse);
        redirectAttributes.addFlashAttribute("project", theProject);
        return "redirect:/project/dropbox-action-response";
    }

        //---------------12.4 PROCESS REMOTE DBX FILES (DOWNLOAD SELECTED/ALL, DELETE SELECTED, ALL) -------------------
    @RequestMapping(value = "process-dbx-remote-files/{projectId}")
    public String downloadDeleteProcessDbxRemoteFiles(@RequestParam(name = "action") String actionValue,
                                                      @RequestParam String openFolderAfterDownload,
                                                      @RequestParam(required = false) String localPathToDownloadTo,
                                                      @RequestParam(required = false) List<String> selectedRemoteDbxProjectFilesToProcess,
                                                      @PathVariable int projectId,                                      //Strings in this list are: pathLower fields of FileMetadata objects
                                                      final RedirectAttributes redirectAttributes,
                                                      HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUserObj");
        Project theProject = projectDao.findOne(projectId);
        String redirectToOverview = "redirect:/project/project-overview/" + theProject.getId();
        String redirectToDbxUtils = "redirect:/project/dropbox-utils/" + theProject.getId();
        StringBuilder finalHtmlResponse = new StringBuilder();

        if (currentUser.getDbxAccessToken() == null) {
            redirectAttributes.addFlashAttribute("actionMessage", "You need to connect your Dropbox account first");
            return redirectToOverview;
        }

        //DROPBOX API CLIENT INITIALIZE
        DbxClientV2 dbxClient = new DbxClientV2(config, currentUser.getDbxAccessToken());

        //Decide which files to process based upon action button
        List<FileMetadata> finalRemoteDbxFilesToProcess = new ArrayList<>();
        if (actionValue.contains("All")) {

            try {
                //DROPBOX API CALL
                finalRemoteDbxFilesToProcess.addAll(DropboxMethods.getOnlyFilesInProjectFolder(theProject.getTitle(), dbxClient));

            } catch (DbxException e) {
                e.printStackTrace();
                System.out.println(e.toString());
                redirectAttributes.addFlashAttribute("actionMessage","Dropbox gave a generic Dropbox Error: " + e.toString());
                return redirectToDbxUtils;
            }

        } else if (actionValue.contains("Selected")) {

            if (selectedRemoteDbxProjectFilesToProcess != null) {

                try {
                    finalRemoteDbxFilesToProcess.addAll(DropboxMethods.convertDbxPathLowerStringsListIntoFileMetadataList(selectedRemoteDbxProjectFilesToProcess, dbxClient));
                } catch (DbxException e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("actionMessage", "Dropbox error trying to retrieve the files you selected: " + e.toString());
                    return redirectToDbxUtils;
                }

            } else {
                redirectAttributes.addFlashAttribute("actionMessage", "You did not choose any files to download");
                return redirectToDbxUtils;
            }
        }

        //Then take the appropriate action, such as Download...
        if (actionValue.contains("Download")) {

            StringBuilder htmlDownloadResponse = new StringBuilder();

            //Check if valid folder first
            if (!Tools.isDirectory(localPathToDownloadTo) || localPathToDownloadTo.isEmpty() || localPathToDownloadTo == null) {
                redirectAttributes.addFlashAttribute("actionMessage", "That is not a valid folder path to download to, or it doesnt exist");
                return redirectToDbxUtils;
            }

            //abs path for better usage
            Path localAbsoluteFilePathToDownloadTo = Paths.get(localPathToDownloadTo).toAbsolutePath();

            htmlDownloadResponse.append(String.format("<h1> Downloading from Dropbox project folder to: %s</h1>", localAbsoluteFilePathToDownloadTo.toString()));
            htmlDownloadResponse.append(String.format("<form action=\"https://www.dropbox.com/home/Apps/Composer's Dashboard App Files/%s\" method=\"get\" target=\"_blank\"> "+
                                                            "<input class=\"dbx-button\" type=\"submit\" value=\"Go To Dropbox Folder\" /> " +
                                                      "</form>", theProject.getTitle()));

            for (FileMetadata file : finalRemoteDbxFilesToProcess) {

                htmlDownloadResponse.append(String.format("<h3> %s </h3>", file.getName())); //Header with filename for each listing

                try {

                    boolean existsInLocalPath = Tools.isFile(localPathToDownloadTo + File.separator + file.getName());

                    //DROPBOX API CALL
                    FileMetadata fileFromDropbox = DropboxMethods.downloadFileFromDropbox(localPathToDownloadTo, file, dbxClient);

                    if (existsInLocalPath) {

                        htmlDownloadResponse.append(   //Custom upload success state for file-already-exists....
                                "<div style=\"color:Yellow\">  Success (with note)  <br/> " +
                                        "Note: File Already Exists in the Folder you tried to download the file to, but was overwritten, as stated in the usage notes. " +
                                "</div> <br/> ");
                    } else {
                        htmlDownloadResponse.append("<div style=\"color:LimeGreen\">  Complete success  </div> <br/> ");
                    }

                    htmlDownloadResponse.append(
                            "Full file path from your Dropbox root: Apps/Composer's Dashboard App Files" + fileFromDropbox.getName() + " <br/> " +
                            "Size in Dropbox: " + Tools.readableFileSize(fileFromDropbox.getSize()) + " <br/> " +
                            "File name in download folder: " + localAbsoluteFilePathToDownloadTo.getFileName() + " <br/> " +
                            "Size in download folder: " + Tools.readableFileSize(localAbsoluteFilePathToDownloadTo.toFile().length()) + " <br/> " +
                            "=============================================================  "
                    );

                } catch (IOException e) {
                    e.printStackTrace();

                    htmlDownloadResponse.append(String.format(
                            "<div style=\"color:Red\">Possible Failure: <br/>" +
                                "In trying to access the folder you provided, or trying to write the file to your computer, and input/output error was thrown: <br/> " +
                                    "%s " +
                            "</div> <br/> =============================================================", e.toString()
                    ));

                } catch (DbxException e) {
                    e.printStackTrace();

                    htmlDownloadResponse.append(String.format(
                            "<div style=\"color:Red\">Failure: <br/>" +
                                "Dropbox has given an generic error: %s " +
                            "</div> <br/> =============================================================", e.toString()
                    ));
                }
            }

            //Finally, list all the files the user tried to download
            htmlDownloadResponse.append(" <br/><br/>   <b><u> All local files you attempted to download: </u></b>   <br/>");
            for (FileMetadata file : finalRemoteDbxFilesToProcess) {
                htmlDownloadResponse.append(file.getName() + "<br/>");
            }
            htmlDownloadResponse.append("<br/><br/>"); //couple of breaks to space from the back buttons


            //the finished document goes out to the template
            finalHtmlResponse.append(htmlDownloadResponse);

            //open the folder if they wanted
            if (openFolderAfterDownload.contains("true")) {

                DesktopApi.open(localAbsoluteFilePathToDownloadTo.toFile());
            }

            //...or Delete
        } else if (actionValue.contains("Delete")) {

            StringBuilder htmlDeleteResponse = new StringBuilder();

            htmlDeleteResponse.append(String.format("<h1> <span style=\"color:Red\">Deleting</span> from Dropbox project folder: %s</h1>", theProject.getTitle()));
            htmlDeleteResponse.append(String.format("<form action=\"https://www.dropbox.com/home/Apps/Composer's Dashboard App Files/%s\" method=\"get\" target=\"_blank\"> "+
                                                            "<input class=\"dbx-button\" type=\"submit\" value=\"Go To Folder\" /> " +
                                                      "</form> <br/>", theProject.getTitle()));

            for (FileMetadata file : finalRemoteDbxFilesToProcess) {

                htmlDeleteResponse.append(String.format("<h3> %s </h3>", file.getName())); //Header with filename for each listing

                try {
                    DropboxMethods.deleteFileInProjectFolder(dbxClient, theProject.getTitle(), file.getName());

                    htmlDeleteResponse.append("<div style=\"color:Red\">Deleted!</div> <br/>");
                    htmlDeleteResponse.append("=============================================================");

                } catch (DbxException e) {
                    e.printStackTrace();
                    htmlDeleteResponse.append(String.format("<div style=\"color:LightGray\">There was a problem in deleting this file: %s </div> <br/>", e.toString()));
                    htmlDeleteResponse.append("=============================================================");
                }
            }

            //Finally, list all the files the user tried to delete
            htmlDeleteResponse.append(" <br/><br/>   <b><u> All local files you attempted to delete: </u></b>   <br/>");
            for (FileMetadata file : finalRemoteDbxFilesToProcess) {
                htmlDeleteResponse.append(file.getName() + "<br/>");
            }
            htmlDeleteResponse.append("<br/><br/>"); //couple of breaks to space from the back button

            //the finished document goes out to the template
            finalHtmlResponse.append(htmlDeleteResponse);

        }


        redirectAttributes.addFlashAttribute("htmlResponse", finalHtmlResponse);
        redirectAttributes.addFlashAttribute("project", theProject);
        return "redirect:/project/dropbox-action-response";
    }


}