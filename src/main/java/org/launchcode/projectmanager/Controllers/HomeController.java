package org.launchcode.projectmanager.Controllers;

import org.launchcode.projectmanager.Tools;
import org.launchcode.projectmanager.models.Comment;
import org.launchcode.projectmanager.models.Project;
import org.launchcode.projectmanager.models.User;
import org.launchcode.projectmanager.models.data.CommentDao;
import org.launchcode.projectmanager.models.data.ProjectDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("")
public class HomeController {

    //TODO 1: FULL TEST OF APP, ERRORS, AND ABILITIES!!

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private CommentDao commentDao;


    @RequestMapping(value = "", method = RequestMethod.GET)
    public String welcome(Model model, HttpSession session) throws IOException, org.tautua.markdownpapers.parser.ParseException {

        if (session.getAttribute("currentUserObj") == null) {
            model.addAttribute("loggedInUser", "No User signed in yet");
        } else {
            User currentUser = (User) session.getAttribute("currentUserObj");
            model.addAttribute("loggedInUser", "Current User: " + (currentUser.getUsername()));
        }

        model.addAttribute("date", String.format("Today's date is: %s %d, %d", LocalDate.now().getMonth(), LocalDate.now().getDayOfMonth(), LocalDate.now().getYear())); //Month Day, Year
        model.addAttribute("title", "Welcome to the Composer's Project Dashboard");
        model.addAttribute("readme", Tools.getReadmeHtmlAllMethods());

        return "index/welcome";
    }

    @RequestMapping(value = "blog", method = RequestMethod.GET)
    String displayBlog(Model model) {

        List<Project> allPublicProjects = Tools.sortProjectsNewestToOldest(projectDao.findByIsPublic(true));

        if (allPublicProjects.isEmpty()) {
            model.addAttribute("noProjects", "No projects.... this app is desolate...");
        }

        if (!model.containsAttribute("comment")) {
            model.addAttribute(new Comment());
        }

        model.addAttribute("allPublicProjects", allPublicProjects);
        model.addAttribute("title", "Showcase Blog");

        return "index/blog";
    }

    @RequestMapping(value = "blog", method = RequestMethod.POST)
    String addComment(@Valid @ModelAttribute("comment") Comment comment,
                      Errors errors,
                      @RequestParam Integer projectId,
                      HttpSession session,
                      Model model,
                      RedirectAttributes redirectAttributes,
                      final BindingResult bindingResult) {

        if (session.getAttribute("currentUserObj") == null) {
            return "redirect:user/login";
        }

        if (bindingResult.hasFieldErrors("contents")) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.comment", bindingResult);
            redirectAttributes.addFlashAttribute("comment", comment);
            return "redirect:/blog";
        }

        User loggedUser = (User) session.getAttribute("currentUserObj");

        comment.setProject(projectDao.findOne(projectId));
        comment.setUser(loggedUser);
        commentDao.save(comment);

        return "redirect:/blog";
    }

    @RequestMapping(value = "test", method = RequestMethod.GET)
    public String testing(Model model) {

        return "index/test";
    }

}
