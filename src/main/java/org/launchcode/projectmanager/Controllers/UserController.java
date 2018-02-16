package org.launchcode.projectmanager.Controllers;


import org.launchcode.projectmanager.Tools;
import org.launchcode.projectmanager.models.User;
import org.launchcode.projectmanager.models.data.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
@RequestMapping("user")
@Scope("session")
public class UserController {

    @Autowired
    public UserDao userDao;

    // USER CREATION
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

        newUser.setPassword(Tools.makeSHA256HashString(newUser.getPassword()));
        userDao.save(newUser);
        User currentUser = userDao.findOne(newUser.getId());
        model.addAttribute("new_user_name", currentUser.getUsername());

        //CUSTOM_SESSION ATTRIBUTES UNTIL I FIND SOMETHING FOR MULTI-CONTROLLERS
        session.setAttribute("currentUserId", currentUser.getId());

        return "user/success-test";

        // USER MANAGEMENT

    }

}
