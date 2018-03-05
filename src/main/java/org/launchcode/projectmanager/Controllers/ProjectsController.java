package org.launchcode.projectmanager.Controllers;

import org.launchcode.projectmanager.Tools;
import org.launchcode.projectmanager.models.Project;
import org.launchcode.projectmanager.models.User;
import org.launchcode.projectmanager.models.data.ProjectDoa;
import org.launchcode.projectmanager.models.data.UserDao;
import org.launchcode.projectmanager.models.enums.Mode;
import org.launchcode.projectmanager.models.enums.MusicKeyType;
import org.launchcode.projectmanager.models.enums.TimeSignatureDenominator;
import org.launchcode.projectmanager.models.enums.TimeSignatureNumerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("project")
public class ProjectsController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ProjectDoa projectDoa;

//    @RequestMapping(value = "", method = RequestMethod.GET)
//    public String displayMainMenu() {
//        return null;
//    }

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

        return new ModelAndView("redirect:/user/user-profile");
    }

    @RequestMapping(value = "dashboard", method = RequestMethod.GET)
    public String displayDashboard(Model model, HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUserObj");
        List<Project> last3projects = Tools.getLastXProjects(projectDoa.findByUserId(currentUser.getId()), 3);

        model.addAttribute("title", currentUser.getUsername() + "'s Dashboard");
        model.addAttribute("projectList", last3projects);

        return "project/dashboard";
    }

}
