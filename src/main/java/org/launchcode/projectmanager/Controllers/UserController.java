package org.launchcode.projectmanager.Controllers;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import org.launchcode.projectmanager.Tools;
import org.launchcode.projectmanager.models.DropboxAPI.DropboxMethods;
import org.launchcode.projectmanager.models.User;
import org.launchcode.projectmanager.models.data.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;

@Controller
@ComponentScan
@RequestMapping("user")
public class UserController {

    /*
    * SECTIONS:
    * 1. USER CREATION (CreateUser)
    * 2. USER MANAGEMENT (UserProfile... ChangeUsername... DeleteUser...)
    * 3. USER LOGIN/LOGOUT (proper methods)
    * 4. USER AVATAR METHODS
    * 5. DROPBOX STUFF
     */

    @Autowired
    private UserDao userDao;


    // ----------------- 1. USER CREATION -------------------
    @RequestMapping(value = "create-user", method = RequestMethod.GET)
    public String displayCreateUser(Model model) {

        model.addAttribute(new User());
        model.addAttribute("title", "Sign Up");
        return "user/create-user";
    }

    @RequestMapping(value = "create-user", method = RequestMethod.POST)
    public String processCreateUser(@ModelAttribute @Valid User newUser, Errors errors, Model model,
                                    @RequestParam String verifyPassword, HttpSession session, RedirectAttributes redirectAttributes) {
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
                //SESSION CREATION
        session.setAttribute("currentUserObj", currentUser);

        redirectAttributes.addFlashAttribute("statusMessage", "You successfully created your new account!");
        return "redirect:/user/user-profile";
    }

    // ----------------- 2. USER MANAGEMENT (UserProfile... ChangeUsername... DeleteUser...) -------------------
    @RequestMapping(value = "user-profile", method = RequestMethod.GET)
    public String displayUserProfile(Model model, HttpSession session) throws DbxException {

        User currentUser = (User) session.getAttribute("currentUserObj");
        model.addAttribute("currentUser", currentUser);

                //DROPBOX API CALL
        if(currentUser.getDbxAccessToken() != null) {
            DbxRequestConfig config = DbxRequestConfig.newBuilder("Composer's Project Dashboard/1.0").build();
            DbxClientV2 client = new DbxClientV2(config, currentUser.getDbxAccessToken());
            FullAccount currentAccount = client.users().getCurrentAccount();

            model.addAttribute("dbxAccountObj", currentAccount);

            long freeSpaceInBytes = DropboxMethods.getTotalFreeSpaceInBytes(client);
            String freeSpaceString = Tools.readableFileSize(freeSpaceInBytes);
            model.addAttribute("freeSpace", freeSpaceString);
        }

        return "user/profile-settings";
    }

    @RequestMapping(value = "change-username", method = RequestMethod.GET)
    public String displayChangeUsername(Model model, HttpSession session) {

        User currentUser = userDao.findOne(((User) session.getAttribute("currentUserObj")).getId());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("title", "Change Username");

        return "user/change-username";
    }

    @RequestMapping(value = "change-username", method = RequestMethod.POST)
    public String processChangeUsername(@RequestParam String newUsername, Model model, HttpSession session) {

        User currentUser = userDao.findOne(((User) session.getAttribute("currentUserObj")).getId());
        currentUser.setUsername(newUsername);
        userDao.save(currentUser);

                //UPDATE SESSION
        session.setAttribute("currentUserObj", currentUser);

        return "redirect:user-profile";
    }

    @RequestMapping(value = "delete-user", method = RequestMethod.GET)
    public String displayDeleteUser() {
        return "user/delete-user";
    }

    @RequestMapping(value = "delete-user", method = RequestMethod.POST)
    public String processDeleteUser(HttpSession session) {

        User currentUser = userDao.findOne( ((User)session.getAttribute("currentUserObj")).getId() );
        userDao.delete(currentUser);

                //DELETE SESSION
        session.invalidate();

        return "redirect:/";
    }


    //// ----------------- 3. USER LOGIN/LOGOUT (proper methods -------------------
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
                //SESSION CREATION
        session.setAttribute("currentUserObj", proposedUser);

        model.addAttribute("new_user_name", proposedUser.getUsername());

        return "redirect:/project/dashboard";
    }

    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public String logOut(HttpSession session) {

                //SESSION DELETE
        session.invalidate();

        return "user/logout-success";
    }

    // ----------------- 4. USER AVATAR METHODS -------------------
    @RequestMapping(value = "upload-avatar", method = RequestMethod.GET)
    public String uploadAvatar(Model model) {

        return "user/upload-avatar";
    }

    @RequestMapping(value = "upload-avatar", method = RequestMethod.POST)
    public String processUploadAvatar(@RequestParam MultipartFile avatarUpload, HttpSession session, final RedirectAttributes redirectAttributes) throws IOException {

        if (avatarUpload.isEmpty()) {
            redirectAttributes.addFlashAttribute("actionMessage", "You must choose an image; cannot upload nothing");
            return "redirect:/user/upload-avatar";
        }

        if (avatarUpload.getSize() > 5242880) {
            redirectAttributes.addFlashAttribute("actionMessage", "Cannot upload images greater than 5MB");
            return "redirect:/user/upload-avatar";
        }

        byte[] imgData = avatarUpload.getBytes();

        User currentUser = (User) session.getAttribute("currentUserObj");
        currentUser.setAvatarImage(imgData);
        userDao.save(currentUser);

        return "redirect:/user/user-profile";
        //errors to catch:
        //no such file
    }

    @ResponseBody
    @RequestMapping(value = "user-profile/avatar/{currentUser_id}", method = RequestMethod.GET)
    public HttpEntity<byte[]> getImage(@PathVariable("currentUser_id") int userId) throws IOException {

        byte[] imageContent = userDao.findOne(userId).getAvatarImage(); //get image from DAO based on userId

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageContent.length);

        return new HttpEntity<byte[]>(imageContent, headers);
    }


    // ----------------- 5. DROPBOX STUFF -------------------

    //INFO AND TOOLS FOR START AND FINISH METHODS
    private final String APP_KEY = "693ssuwifm7m4dy";
    private final String APP_SECRET = "p77sloa52v6b12x";
    private final String redirectUri = "http://localhost:8080/user/dropbox-auth-finish";

        //set up the config for the authentication flow
    private DbxRequestConfig dbxRequestConfig = DbxRequestConfig.newBuilder("Composer's Project Dashboard/1.0").build();
    private DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
    private DbxWebAuth auth = new DbxWebAuth(dbxRequestConfig, appInfo);

    @RequestMapping(value = "dropbox-auth-start", method = RequestMethod.POST)
    public String startDbxAuth(HttpSession session) throws IOException {

        //set up a place in the session to put
        DbxSessionStore csrfTokenStore = new DbxStandardSessionStore(session, "dropbox-auth-csrf-token");

        //build an auth request
        DbxWebAuth.Request authRequest = DbxWebAuth.newRequestBuilder().withRedirectUri(redirectUri, csrfTokenStore).build();

        //start authorization
        String authorizePageUrl = auth.authorize(authRequest);

        // Redirect the user to the Dropbox website so they can approve our application.
        // The Dropbox website will send them back to the user profile when they're done.
        return "redirect:" + authorizePageUrl;
    }

    @RequestMapping(value = "dropbox-auth-finish", method = {RequestMethod.GET, RequestMethod.POST})
    public String finishDbxAuth(HttpSession session, HttpServletRequest request, HttpServletResponse response, final RedirectAttributes redirectAttributes) throws IOException {

        DbxSessionStore csrfTokenStore = new DbxStandardSessionStore(session, "dropbox-auth-csrf-token");
        DbxAuthFinish authFinish;

        try {
            authFinish = auth.finishFromRedirect(redirectUri, csrfTokenStore, request.getParameterMap());

        } catch (DbxWebAuth.BadRequestException ex) {
            response.sendError(400, "On /dropbox-auth-finish: Bad request: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("statusMessage", "On /dropbox-auth-finish: Bad request: " + ex.getMessage());
            return "redirect:/user/user-profile";

        } catch (DbxWebAuth.BadStateException ex) {
            // Send them back to the start of the auth flow.
            return "redirect:/user/dropbox-auth-start";

        } catch (DbxWebAuth.CsrfException ex) {
            response.sendError(403, "Forbidden. " + "On /dropbox-auth-finish: CSRF mismatch: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("statusMessage", "Forbidden. " + "On /dropbox-auth-finish: CSRF mismatch: " + ex.getMessage());
            return "redirect:/user/user-profile";

        } catch (DbxWebAuth.NotApprovedException ex) {
            // When Dropbox asked "Do you want to allow this app to access your Dropbox account?", the user clicked "No".
            redirectAttributes.addFlashAttribute("statusMessage", "You denied access to your Dropbox account, authorization failed");
            return "redirect:/user/user-profile";

        } catch (DbxWebAuth.ProviderException ex) {
            response.sendError(503, "Error communicating with Dropbox. " + "On /dropbox-auth-finish: Auth failed: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("statusMessage", "Error communicating with Dropbox." + "On /dropbox-auth-finish: Auth failed: " + ex.getMessage());
            return "redirect:/user/user-profile";

        } catch (DbxException ex) {
            response.sendError(503, "Error communicating with Dropbox. " + "On /dropbox-auth-finish: Error getting token: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("statusMessage", "Error communicating with Dropbox. " + "On /dropbox-auth-finish: Error getting token: " + ex.getMessage());
            return "redirect:/user/user-profile";
        }

        //FINISHED!!!!!
        String accessToken = authFinish.getAccessToken();

        User currentUser = (User) session.getAttribute("currentUserObj");
        currentUser.setDbxAccessToken(accessToken);
        userDao.save(currentUser);

        redirectAttributes.addFlashAttribute("statusMessage", "You successfully connected your Dropbox");

        return "redirect:/user/user-profile";
    }

    @RequestMapping(value = "dropbox-disconnect", method = {RequestMethod.POST})
    String dbxDisconnect(HttpSession session, final RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUserObj");
        currentUser.setDbxAccessToken(null);
        userDao.save(currentUser);

        redirectAttributes.addFlashAttribute("statusMessage", "You successfully disconnected your Dropbox");

        return "redirect:/user/user-profile";
    }

}
