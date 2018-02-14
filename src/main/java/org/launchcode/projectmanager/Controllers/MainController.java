package org.launchcode.projectmanager.Controllers;


import org.launchcode.projectmanager.models.User;
import org.launchcode.projectmanager.models.data.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

import javax.validation.Valid;

@Controller
@RequestMapping("")
public class MainController {

    @Autowired
    public UserDao userDao;

    @RequestMapping(value = "create-user", method = RequestMethod.GET)
    public String createUser(Model model) {
        model.addAttribute(new User());
        return "user/create-user";
    }

    @RequestMapping(value = "create-user", method = RequestMethod.POST)
    public String processCreateUser(@ModelAttribute @Valid User newUser,
                                    Errors errors,
                                    Model model,
                                    @RequestParam String verifyPassword,
                                    HttpSession session) {
        if (errors.hasErrors()) {
            return "user/create-user";
        }

        if(!verifyPassword.equals(newUser.getPassword())) {
            model.addAttribute("verifyPasswordError", "Passwords don't match");
            return "user/create-user";
        }

        userDao.save(newUser);
        session.setAttribute("currentUserId", newUser.getId());
        User currentUser = userDao.findOne(((Integer) session.getAttribute("currentUserId")));
        model.addAttribute("new_user_name", currentUser.getUsername());
        return "user/success-test";
    }

}
