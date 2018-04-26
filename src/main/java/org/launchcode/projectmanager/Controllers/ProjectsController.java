package org.launchcode.projectmanager.Controllers;

import org.launchcode.projectmanager.DesktopApi;
import org.launchcode.projectmanager.Tools;
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
import java.nio.file.Path;
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


    @RequestMapping(value = "dashboard", method = RequestMethod.GET)
    public String displayDashboard(Model model, HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUserObj");
        User theUser = userDao.findOne(currentUser.getId());
        List<Project> last3projects = Tools.getLastXProjects(theUser.getProjects(), 3);

        model.addAttribute("title", currentUser.getUsername() + "'s Dashboard");
        model.addAttribute("projectList", last3projects);

        return "project/dashboard";
    }

    @RequestMapping(value = "create-project", method = RequestMethod.GET)
    public String displayCreateProject(Model model) {

        model.addAttribute(new Project());
        model.addAttribute("title", "Add Composition");
        model.addAttribute("allModes", Mode.values());
        model.addAttribute("allMusicKeyTypes", MusicKeyType.values());
        model.addAttribute("allTimeSigNum", TimeSignatureNumerator.values());
        model.addAttribute("allTimeSigDen", TimeSignatureDenominator.values());

        return "project/create-project";
    }

    @RequestMapping(value = "create-project", method = RequestMethod.POST)
    public ModelAndView processCreateProject(@ModelAttribute @Valid Project newProject, Errors errors, Model model, @RequestParam String isPublicPrivate,
                                             HttpSession session, final RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            model.addAttribute(new Project());
            model.addAttribute("title", "Add Composition");
            model.addAttribute("allModes", Mode.values());
            model.addAttribute("allMusicKeyTypes", MusicKeyType.values());
            model.addAttribute("allTimeSigNum", TimeSignatureNumerator.values());
            model.addAttribute("allTimeSigDen", TimeSignatureDenominator.values());
            return new ModelAndView("project/create-project");
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

        return new ModelAndView("redirect:/project/dashboard");
    }

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
            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to view that");
            return "redirect:/project/dashboard";
        }

        if (!model.containsAttribute("task")) {
            model.addAttribute("task", new Task());
        }

        model.addAttribute("project", theProject);
        model.addAttribute("tasks", theProject.getTasks()); //List<Task> thisProjectsTasks = theProject.getTasks(); or  taskDao.findByProjectId(theProject.getId());  which one is better lol
        model.addAttribute("filePaths", theProject.getFile_paths());
        model.addAttribute("title", "Overview");

        return "project/project-overview";
    }

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

    @RequestMapping(value = "all-projects", method = RequestMethod.GET)
    public String displayAllProjects(Model model, HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUserObj");

        model.addAttribute("title", "All Projects");
        model.addAttribute("allProjects", projectDao.findByUserId(currentUser.getId()));

        return "project/all-projects";
    }

    @RequestMapping(value = "add-path/{projectId}", method = {RequestMethod.GET, RequestMethod.POST})
    public String addPath(@PathVariable int projectId, @RequestParam String filePathString, final RedirectAttributes redirectAttributes, HttpSession session) {

        Project theProject = projectDao.findOne(projectId);
        File fileToAdd = new File(filePathString);
        User currentUser = (User) session.getAttribute("currentUserObj");

        if (currentUser.getId() != theProject.getUser().getId()) {
            redirectAttributes.addFlashAttribute("actionMessage", "You don't have permission to change this project");
            return "redirect:/project/dashboard";
        }

        if (!fileToAdd.exists()) {
            redirectAttributes.addFlashAttribute("actionMessage", "Path doesn't exist");
            return "redirect:/project/project-overview/" + projectId;
        }

        theProject.addFile_pathString(filePathString);
        projectDao.save(theProject);

        return "redirect:/project/project-overview/" + projectId;
    }

    @RequestMapping(value = "open-path/{projectId}", method = RequestMethod.POST)
    public String openFile(@PathVariable int projectId, @RequestParam String path) {

        File fileToOpen = new File(path);

        DesktopApi.open(fileToOpen);

        return "redirect:/project/project-overview/" + projectId;
    }

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
}