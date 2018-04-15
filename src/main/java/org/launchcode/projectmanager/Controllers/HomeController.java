package org.launchcode.projectmanager.Controllers;


import org.launchcode.projectmanager.models.CloudConvertAPI.CCAPI_Implement;
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

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private CommentDao commentDao;


    @RequestMapping(value = "", method = RequestMethod.GET)
    public String welcome(Model model, HttpSession session) throws IOException {

        if (session.getAttribute("currentUserObj") == null) {
            model.addAttribute("loggedInUser", "No User signed in yet");
        } else {
            User currentUser = (User) session.getAttribute("currentUserObj");
            model.addAttribute("loggedInUser", "Current User: " + (currentUser.getUsername()));
        }

        String htmlString = CCAPI_Implement.getHTMLString();
        model.addAttribute("testString", htmlString);

        LocalDate date = LocalDate.now();
        model.addAttribute("date", String.format("Today's date is: %s %d, %d", date.getMonth(), date.getDayOfMonth(), date.getYear()));
        return "index/welcome";
    }

    @RequestMapping(value = "blog", method = RequestMethod.GET)
    String displayBlog(Model model) {

        List<Project> allPublicProjects = projectDao.findByIsPublic(true);

        if (allPublicProjects.size() == 0) {
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
}
