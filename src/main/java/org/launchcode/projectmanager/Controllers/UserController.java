package org.launchcode.projectmanager.Controllers;

import org.launchcode.projectmanager.BCrypt;
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
import java.io.File;
import java.util.ArrayList;

@Controller
@RequestMapping("user")
public class UserController {

    /*
    * SECTIONS:
    * 1. USER CREATION (CreateUser)
    * 2. USER MANAGEMENT (UserProfile... ChangeUsername... DeleteUser...)
    * 3. USER LOGIN/LOGOUT (proper methods)
     */

    @Autowired
    private UserDao userDao;

    private static ArrayList<String> tempData = new ArrayList<>();


    // USER CREATION
    @RequestMapping(value = "create-user", method = RequestMethod.GET)
    public String displayCreateUser(Model model) {

        model.addAttribute(new User());
        model.addAttribute("title", "Sign Up");
        return "user/create-user";
    }

    @RequestMapping(value = "create-user", method = RequestMethod.POST)
    public String processCreateUser(@ModelAttribute @Valid User newUser,
                                    Errors errors,
                                    Model model,
                                    @RequestParam String verifyPassword,
                                    HttpSession session) {
        if (errors.hasErrors()) {
            model.addAttribute("title", "Sign Up");
            return "user/create-user";
        }

        if (!verifyPassword.equals(newUser.getPassword())) {
            model.addAttribute("title", "Sign Up");
            model.addAttribute("verifyPasswordError", "Passwords don't match");
            return "user/create-user";
        }

        String hashedPassword = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());

        newUser.setPassword(hashedPassword);
        userDao.save(newUser);
        User currentUser = userDao.findOne(newUser.getId());
        model.addAttribute("new_user_name", currentUser.getUsername());

                //SESSION CREATION
        session.setAttribute("currentUserObj", currentUser);

        return "user/success-test";
    }

    // USER MANAGEMENT
    @RequestMapping(value = "user-profile", method = RequestMethod.GET)
    public String displayUserProfile(Model model, HttpSession session) {

        model.addAttribute("currentUsername", ((User) session.getAttribute("currentUserObj")).getUsername());

        return "user/profile-settings";
    }

    @RequestMapping(value = "change-username", method = RequestMethod.GET)
    public String displayChangeUsername(Model model, HttpSession session) {

        User currentUser = userDao.findOne(((User) session.getAttribute("currentUserObj")).getId());
        model.addAttribute("currentUsername", currentUser.getUsername());

        return "user/change-username";
    }

    @RequestMapping(value = "change-username", method = RequestMethod.POST)
    public String processChangeUsername(@RequestParam String newUsername, Model model, HttpSession session) {

        User currentUser = userDao.findOne(((User) session.getAttribute("currentUserObj")).getId());
        currentUser.setUsername(newUsername);
        userDao.save(currentUser);

                //SESSION MANAGEMENT
        session.setAttribute("currentUserObj", currentUser);

        return "redirect:user-profile";
    }

    @RequestMapping(value = "delete-user", method = RequestMethod.GET)
    public String displayDeleteUser() {
        return "user/delete-user";
    }

    @RequestMapping(value = "delete-user", method = RequestMethod.POST)
    public String processDeleteUser(HttpSession session) {

        Integer currentUserId = ((User) session.getAttribute("currentUserObj")).getId();
        User currentUser = userDao.findOne(currentUserId);
        userDao.delete(currentUser);

                //DELETE SESSION
        session.removeAttribute("currentUserObj");

        return "redirect:/";
    }


    //USER LOGIN/LOGOUT
    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String displayLogIn(Model model) {

        model.addAttribute("title", "Log In");
        return "user/login";
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public String processLogIn(Model model, @RequestParam String username, @RequestParam String password, HttpSession session) {

        if (username.isEmpty() || password.isEmpty()) {
            model.addAttribute("title", "Log In");
            model.addAttribute("usernameError", "Neither field can be empty");
            model.addAttribute("passwordError", "Neither field can be empty");
            return "user/login";
        }

        if (userDao.findByUsername(username).isEmpty()) {
            model.addAttribute("title", "Log In");
            model.addAttribute("usernameError", "This user doesn't exist");
            return "user/login";
        }

        User proposedUser = userDao.findByUsername(username).get(0);

        if (!BCrypt.checkpw(password, proposedUser.getPassword())){
            model.addAttribute("title", "Log In");
            model.addAttribute("passwordError", "Incorrect Password");
            return "user/login";
        }
                //SESSION MANAGEMENT
        session.setAttribute("currentUserObj", proposedUser);

        model.addAttribute("new_user_name", proposedUser.getUsername());

        return "redirect:/";
    }

    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public String logOut(HttpSession session) {

                //SESSION DELETE
        session.removeAttribute("currentUserObj");

        return "user/logout-success";
    }

    @RequestMapping(value = "upload-avatar", method = RequestMethod.GET)
    public String uploadAvatar(Model model) {

        if (!tempData.isEmpty()) {
            model.addAttribute("imgPath", tempData.get(0));
        }

        return "user/upload-avatar";
    }

    @RequestMapping(value = "upload-avatar", method = RequestMethod.POST)
    public String processUploadAvatar(@RequestParam File avatarUpload) {

        try {
            tempData.add(0,avatarUpload.getName() + " and " + avatarUpload.getAbsolutePath());
        } catch (Exception e) {
            tempData.add(e.getMessage() + " : " + e.getCause().toString());
        }

        return "redirect:/user/upload-avatar";
    }
}
