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
    private UserDao userDao;

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

        if (!verifyPassword.equals(newUser.getPassword())) {
            model.addAttribute("verifyPasswordError", "Passwords don't match");
            return "user/create-user";
        }

        newUser.setPassword(Tools.makeSHA256HashString(newUser.getPassword()));
        userDao.save(newUser);
        User currentUser = userDao.findOne(newUser.getId());
        model.addAttribute("new_user_name", currentUser.getUsername());

        //SESSION CREATION
        session.setAttribute("isUserLogged", true);
        session.setAttribute("currentUserId", currentUser.getId());
        session.setAttribute("currentUsername", currentUser.getUsername());

        return "user/success-test";
    }

    // USER MANAGEMENT
    @RequestMapping(value = "user-profile", method = RequestMethod.GET)
    public String userProfile(Model model, HttpSession session) {

        model.addAttribute("currentUsername", session.getAttribute("currentUsername"));
        return "user/profile-settings";
    }

    @RequestMapping(value = "change-username", method = RequestMethod.GET)
    public String changeUsername(Model model, HttpSession session) {

        User currentUser = userDao.findOne((Integer) session.getAttribute("currentUserId"));
        model.addAttribute("currentUsername", currentUser.getUsername());

        return "user/change-username";
    }

    @RequestMapping(value = "change-username", method = RequestMethod.POST)
    public String processChangeUsername(@RequestParam String newUsername, Model model, HttpSession session) {

        User currentUser = userDao.findOne((Integer) session.getAttribute("currentUserId"));
        currentUser.setUsername(newUsername);
        userDao.save(currentUser);

        session.setAttribute("currentUsername", newUsername);

        return "redirect:user-profile";
    }

    @RequestMapping(value = "delete-user", method = RequestMethod.GET)
    public String deleteUser() {
        return "user/delete-user";
    }

    @RequestMapping(value = "delete-user", method = RequestMethod.POST)
    public String processDeleteUser(HttpSession session) {

        Integer currentUserId = (Integer) session.getAttribute("currentUserId");
        User currentUser = userDao.findOne(currentUserId);
        userDao.delete(currentUser);

        //"DELETE SESSION"
        session.setAttribute("isUserLogged", false);
        session.removeAttribute("currentUserId");
        session.removeAttribute("currentUsername");

        return "redirect:/";
    }

}
