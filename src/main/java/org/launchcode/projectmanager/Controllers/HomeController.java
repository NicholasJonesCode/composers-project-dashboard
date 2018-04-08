package org.launchcode.projectmanager.Controllers;


import org.launchcode.projectmanager.models.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;

@Controller
@RequestMapping("")
public class HomeController {

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String welcome(Model model, HttpSession session) {

        if (session.getAttribute("currentUserObj") == null) {
            model.addAttribute("loggedInUser", "No User signed in yet");
        } else {
            User currentUser = (User) session.getAttribute("currentUserObj");
            model.addAttribute("loggedInUser", "Current User: " + (currentUser.getUsername()));
    }

        LocalDate date = LocalDate.now();
        model.addAttribute("date", String.format("Today's date is: %s %d, %d", date.getMonth(), date.getDayOfMonth(), date.getYear()));
        return "index/welcome";
    }
}
