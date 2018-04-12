package org.launchcode.projectmanager.Controllers;

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
    public String projectOverview(@PathVariable int projectId, Model model) {

        Project theProject = projectDao.findOne(projectId);
        model.addAttribute("project", theProject);

        List<Task> thisProjectsTasks = theProject.getTasks(); //or  taskDao.findByProjectId(theProject.getId());  which one is better lol
        model.addAttribute("tasks", thisProjectsTasks);

        if (!model.containsAttribute("task")) {
            model.addAttribute("task", new Task());
        }

        model.addAttribute("title", "Project Overview");

        return "project/project-overview";
    }

    @RequestMapping(value = "create-task/{projectId}", method = RequestMethod.POST)
    public ModelAndView createTask(@Valid @ModelAttribute("task") Task task,
                                   Errors errors,
                                   @PathVariable int projectId,
                                   RedirectAttributes redirectAttributes,
                                   final BindingResult bindingResult) {

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
    public ModelAndView deleteTask(@PathVariable int taskId, @PathVariable int projectId) {

        taskDao.delete(taskId);

        return new ModelAndView("redirect:/project/project-overview/" + projectId);
    }

    @RequestMapping(value = "delete-project/{projectId}", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView deleteProject(@PathVariable int projectId, final RedirectAttributes redirectAttributes) {

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
}
