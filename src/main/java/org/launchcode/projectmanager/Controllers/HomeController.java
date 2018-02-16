package org.launchcode.projectmanager.Controllers;


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

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String welcome(Model model, HttpSession session) {

        if (session.getAttribute("currentUserId") == null) {
            model.addAttribute("loggedInUser", "No User signed in yet");
        } else {
            model.addAttribute("loggedInUser", "Current User: " + userDao.findOne((Integer) session.getAttribute("currentUserId")).getUsername());
        }

        return "index/welcome";
    }

}
