package org.launchcode.projectmanager;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectManagerInterceptor implements HandlerInterceptor {

    private List<String> allowedURIs = new ArrayList<>(Arrays.asList(
            "/",
            "/user/login",
            "/user/create-user",
            "/blog",
            "/test"
            ));

    private List<String> forbiddenWhenLoggedIn = new ArrayList<>(Arrays.asList(
            "/user/login",
            "/user/create-user"
    ));

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //I let this URI go through for more detailed validation in the method itself
        // and also let the path for entity that holds the avatar images go through, so that non-users can properly view the blog
        if (request.getRequestURI().contains("project-overview") || request.getRequestURI().contains("user/user-profile/avatar")) {
            return true;
        }

        //if a user is logged in an they do dis tings, den slap them!
        if (request.getSession().getAttribute("currentUserObj") != null && forbiddenWhenLoggedIn.contains(request.getRequestURI())) {
            response.sendRedirect(request.getContextPath() + "/user/dashboard");
        }

        //if someone isn't logged in, and they go to a path that isn't in the list, then go to the login page
        if (request.getSession().getAttribute("currentUserObj") == null && !allowedURIs.contains(request.getRequestURI()) ) {

            response.sendRedirect(request.getContextPath() + "/user/login");
            return false;

        } else {

        return true;
        }
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
