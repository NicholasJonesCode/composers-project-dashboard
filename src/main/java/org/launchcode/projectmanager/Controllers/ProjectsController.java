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

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
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

        if (currentUser.getId() != projectDao.findOne(projectId).getUser().getId()) {
            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to change this project");
            return new ModelAndView("redirect:/project/dashboard");
        }

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.project", bindingResult);
            redirectAttributes.addFlashAttribute("project", editedProject);
            return new ModelAndView("redirect:/project/edit-project/" + projectId);
        }

        Project projectToEdit = projectDao.findOne(projectId);

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
        return new ModelAndView("redirect:/project/project-overview/" + projectId);
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
    public String projectOverview(@PathVariable int projectId, Model model, HttpSession session, RedirectAttributes redirectAttributes) throws DbxException {

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
    @RequestMapping(value = "open-path/{projectId}", method = RequestMethod.POST)
    public String openFile(@PathVariable int projectId, @RequestParam String path) {

        File fileToOpen = new File(path);

        DesktopApi.open(fileToOpen);

        return "redirect:/project/project-overview/" + projectId;
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

    // ----------------- 12. DROPBOX METHODS -------------------
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

        } else  {
            redirectAttributes.addFlashAttribute("actionMessage", "You have not connected your Dropbox account yet. Go to \"Profiles and Settings\" to do that.");
            return "redirect:/project/project-overview/" + projectId;
        }

        model.addAttribute("project", theProject);
        model.addAttribute("allProjectFilePaths", theProject.getFilePathsOnly());
        model.addAttribute("title", "Dropbox Utility");

        return "project/dropbox-utils";
    }

        // ------------- 12.1 UPLOAD SELECTED, or UPLOAD ALL ----------
    @RequestMapping(value = "upload-files-to-dbx/{projectId}", method = RequestMethod.POST)
    public String uploadFilesToDbx(@PathVariable int projectId, @RequestParam(name = "projectFilesToUpload", required = false) List<Path> selectedProjectFilesToUpload,
                                   @RequestParam(name = "action") String actionValue, final RedirectAttributes redirectAttributes, HttpSession session, Model model) throws DbxException {

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

        for (Path localFilePath : projectFilesToUpload) {
            try {

                FileMetadata result = DropboxMethods.uploadToProjectFolderWithOverwriteMode(localFilePath, dbxClient, theProject.getTitle());
                System.out.println(String.format("FILE UPLOADED TO DROPBOX PATH IN APP FOLDER %s for User %s : ",
                        result.getPathDisplay(), dbxClient.users().getCurrentAccount().getName().getDisplayName())); //API call to user account here
                System.out.println(result.toStringMultiline());

                if (DropboxMethods.fileExistsInProjectFolder(dbxClient, localFilePath, theProject.getTitle())) {

                    htmlUploadResponse.append(
                            String.format( //Custom upload success state for file-already-exists....
                                    "<b> <h3> File Upload - Success (with note) </h3> </b> " +
                                            "Note: File Already Exists in Dropbox project folder: The local file you tried to upload, \"%s\" " +
                                            "conflicted with a file with the same name already in your Dropbox project folder. <br/> " +
                                            "The file in your Dropbox was overwritten by the local one, as stated in the usage notes. <br/> <br/> ", localFilePath.getFileName())
                    );
                } else {
                                            //....or not ^_^
                    htmlUploadResponse.append("<b> <h3> File Upload - Complete Success </h3> </b> ");
                }

                htmlUploadResponse.append(
                        //Local file info first
                        "<u> Local file info: </u> <br/> " +
                                "Filename: " + localFilePath.getFileName() + " <br/> " +
                                "Full Path: " + localFilePath.toAbsolutePath().toString() + " <br/> " +
                                "Size: " + Tools.readableFileSize(localFilePath.toFile().length()) + " <br/> <br/> " +
                                //Dropbox file info next
                                "<u> Dropbox file info: </u> <br/> " +
                                "Full file path from application folder: " + result.getPathDisplay() + " <br/> " +
                                "Size: " + Tools.readableFileSize(result.getSize()) + " <br/> " +
                                "=============================================================  "
                );

                continue;

            } catch (FileNotFoundException fnfe) {
                System.out.println(fnfe.toString());
                fnfe.printStackTrace();

                htmlUploadResponse.append(
                        "<b> <h3> File Upload - Failure <h3/> </b> <br/> <br/> " +
                                "FileNotFoundException: <br/>" +
                                "The file at the path \"" + localFilePath.toAbsolutePath().toString() + "\" Does not exist anymore, or was possibly renamed to something else <br/>" +
                                "============================================================= "
                );

                continue;

            } catch (IOException ioe) {
                System.out.println(ioe.toString());
                ioe.printStackTrace();

                htmlUploadResponse.append(
                        "<b> <h3> File Upload - Failure </h3> </b> <br/> <br/> " +
                                "Input/Output Exception at path \"" + localFilePath.toAbsolutePath().toString() + "\" <br/>" +
                                ioe.getMessage() + "<br/>" + ioe.toString() + "<br/>" +
                                "============================================================= "
                );

            } catch (UploadErrorException upe) {
                //DBX UPLOAD ERROR
                System.out.println(upe.errorValue.toStringMultiline());
                upe.printStackTrace();

                htmlUploadResponse.append(
                        " <b> <h3> File Upload - Failed </h3> </b> <br/>" +
                                "at path: " + localFilePath.toAbsolutePath().toString() + "<br/> <br/>" +
                                "Dropbox has given an Upload Error. Full message: " + upe.errorValue.toString() + " <br/> <br/> "
                );

                //assess the cause, possibly
                long freeSpaceInBytes = DropboxMethods.getTotalFreeSpaceInBytes(dbxClient);
                if (localFilePath.toFile().length() > freeSpaceInBytes) {
                    htmlUploadResponse.append(
                            "According to analysis, this file's size is bigger than your total free space, which is the mostly likely the cause of the error." +
                                    "Free up some space on your dropbox and try again. The upload process will end beyond this point. <br/> " +
                                    "============================================================= "
                    );
                    //Break off the uploading because there's no more space
                    break;

                } else {
                    htmlUploadResponse.append("============================================================= ");
                    continue;
                }

            } catch (DbxException de) {
                System.out.println(de.toString());
                de.printStackTrace();

                htmlUploadResponse.append(
                        " <b> <h3> File Upload - Probable Failure </h3> </b> <br/>" +
                                "at path: " + localFilePath.toAbsolutePath().toString() + "<br/> <br/>" +
                                "Dropbox has given a generic DropboxException Error. Full message: " + de.toString() + " <br/> <br/> "
                );
            }
        }

        //Finally, list all the files the user tried to upload
        htmlUploadResponse.append(" <br/><br/>   <b><u> All local files you attempted to upload: </u></b>   <br/>");

        for (Path localFilePath : projectFilesToUpload) {
            htmlUploadResponse.append(localFilePath.toAbsolutePath().toString() + "<br/>");
        }

        htmlUploadResponse.append("<br/><br/>"); //couple of breaks to space from the back button


        redirectAttributes.addFlashAttribute("htmlResponse", htmlUploadResponse);
        redirectAttributes.addFlashAttribute("projectId", theProject.getId());
        return "redirect:/project/dropbox-upload-response";
    }

        //--------------12.3 UPLOAD RESPONSE PAGE---------------
    @RequestMapping(value = "dropbox-upload-response", method = RequestMethod.GET)
    public String dropboxUploadResponse(Model model) {

        return "project/dropbox-action-response";
    }

        //---------------12.4 PROCESS REMOTE DBX FILES (DOWNLOAD SELECTED/ALL, DELETE SELECTED, ALL) -------------------
    //TODO method for these 4 possibilities  path=process-dbx-remote-files

}