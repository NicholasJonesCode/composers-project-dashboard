package org.launchcode.projectmanager.Controllers;


import org.launchcode.projectmanager.models.User;
import org.launchcode.projectmanager.models.data.ProjectDoa;
import org.launchcode.projectmanager.models.data.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("")
@Scope("session")
public class HomeController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ProjectDoa projectDoa;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String welcome(Model model, HttpSession session) {

        if (session.getAttribute("currentUserObj") == null) {
            model.addAttribute("loggedInUser", "No User signed in yet");
        } else {
            User currentUser = (User) session.getAttribute("currentUserObj");
            model.addAttribute("loggedInUser", "Current User: " + (currentUser.getUsername()));
    }

        return "index/welcome";
    }

}
