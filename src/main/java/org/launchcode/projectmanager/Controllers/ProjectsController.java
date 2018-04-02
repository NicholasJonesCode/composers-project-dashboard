package org.launchcode.projectmanager.Controllers;

import org.launchcode.projectmanager.Tools;
import org.launchcode.projectmanager.models.Project;
import org.launchcode.projectmanager.models.Task;
import org.launchcode.projectmanager.models.User;
import org.launchcode.projectmanager.models.data.ProjectDoa;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("project")
public class ProjectsController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ProjectDoa projectDoa;

    @Autowired
    private TaskDao taskDao;


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
    public ModelAndView processCreateProject(@ModelAttribute @Valid Project newProject, Errors errors, Model model, @RequestParam String isPublicPrivate, HttpSession session) {

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
        projectDoa.save(newProject);

        return new ModelAndView("redirect:/project/dashboard");
    }

    @RequestMapping(value = "dashboard", method = RequestMethod.GET)
    public String displayDashboard(Model model, HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUserObj");
        List<Project> last3projects = Tools.getLastXProjects(projectDoa.findByUserId(currentUser.getId()), 3);

        model.addAttribute("title", currentUser.getUsername() + "'s Dashboard");
        model.addAttribute("projectList", last3projects);

        return "project/dashboard";
    }

    @RequestMapping(value = "project-overview/{projectId}", method = RequestMethod.GET)
    public String projectOverview(@PathVariable int projectId, Model model) {

        Project theProject = projectDoa.findOne(projectId);
        model.addAttribute("project", theProject);

        List<Task> thisProjectsTasks = taskDao.findByProjectId(theProject.getId());
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
                                   @RequestParam String dueDate,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {

        String path = "redirect:/project/project-overview/" + projectId;

        /*Working on new control flow:
        *if description has errors and date is empty
        */

        if (bindingResult.hasFieldErrors("description")) {

            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                LocalDate newDueDate = LocalDate.parse(dueDate, formatter);
                task.setDueDate(newDueDate);

            } catch (DateTimeParseException dtpe) {

                redirectAttributes.addFlashAttribute("dateError", " - Invalid Date Format!");
            }

            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.task", bindingResult);
            redirectAttributes.addFlashAttribute("task", task);
            return new ModelAndView(path);
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate newDueDate = LocalDate.parse(dueDate, formatter);
            task.setDueDate(newDueDate);

        } catch (DateTimeParseException dtpe) {

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName(path);
            redirectAttributes.addFlashAttribute("dateError", " - Invalid Date Format!");
            return modelAndView;
        }

        Project theProject = projectDoa.findOne(projectId);
        task.setProject(theProject);
        taskDao.save(task);

        return new ModelAndView(path);
    }

    public ArrayList<String> coolList = new ArrayList<>(Arrays.asList("Yeet", "Yeet2"));
}
