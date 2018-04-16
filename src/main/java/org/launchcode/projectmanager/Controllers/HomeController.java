package org.launchcode.projectmanager.Controllers;


import org.launchcode.projectmanager.Tools;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tautua.markdownpapers.parser.ParseException;

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
    public String welcome(Model model, HttpSession session) throws IOException, ParseException {

        //use my secondary key if the first one doesn't work lol
        String htmlReadmeString;
        try {
            htmlReadmeString = CCAPI_Implement.getHTMLString("Nw_KX8DDBah89cWmFDL00xl3sAMp-idcCGGkcoe9iluM2eywWpLSNRrXVx1F0DJVfmv8Lpu8KWm1KvgV02xEiQ");
        } catch (HttpClientErrorException e) {
            try {
                htmlReadmeString = CCAPI_Implement.getHTMLString("6Z5LV1mfoLKGS6LeYQgRro5k_mj5qzBM9F7EQ6pECtVe3B-9nwuu0Dy6Fvq5eQmyCm9RcJknaZXd0BG8NTmGig");
            } catch (HttpClientErrorException e2) {
                htmlReadmeString = "429 null Cloud Convert API, too many requests at once and all that: " + e2.toString() +
                        " ...<a href=\"https://cloudconvert.com/api/conversions#bestpractices\">Click here for more info on that</a> <br/> " +
                        " ...Below is the same file from the same source, grabbed from the remote then converted locally instead of with the CC API: <br/>" +
                        Tools.getRemoteReadmeAndConvertToHTMLString();
            }
        }

        model.addAttribute("testString", htmlReadmeString);

        LocalDate date = LocalDate.now();
        model.addAttribute("date", String.format("Today's date is: %s %d, %d", date.getMonth(), date.getDayOfMonth(), date.getYear()));

        if (session.getAttribute("currentUserObj") == null) {
            model.addAttribute("loggedInUser", "No User signed in yet");
        } else {
            User currentUser = (User) session.getAttribute("currentUserObj");
            model.addAttribute("loggedInUser", "Current User: " + (currentUser.getUsername()));
        }


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
