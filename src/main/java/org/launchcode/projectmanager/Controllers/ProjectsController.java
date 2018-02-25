package org.launchcode.projectmanager.Controllers;

import org.launchcode.projectmanager.models.Project;
import org.launchcode.projectmanager.models.data.ProjectDoa;
import org.launchcode.projectmanager.models.data.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
@RequestMapping("projects")
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
        return "project/create-project";
    }

    @RequestMapping(value = "create-project", method = RequestMethod.POST)
    public ModelAndView processCreateProject(@ModelAttribute @Valid Project newProject, Errors errors, Model model, @RequestParam String isPublicPrivate) {

        if (errors.hasErrors()) {
            model.addAttribute("title", "Add Composition");
            return new ModelAndView("project/create-project");
        }

        if (isPublicPrivate.equals("public")) {
            newProject.setPublic(true);
        } else {
            newProject.setPublic(false);
        }

        projectDoa.save(newProject);

        return new ModelAndView("redirect:/user/user-profile");
    }

}
