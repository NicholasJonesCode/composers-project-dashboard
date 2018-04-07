package org.launchcode.projectmanager;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectManagerInterceptor implements HandlerInterceptor {

    List<String> allowedURIs = new ArrayList<>(Arrays.asList(
            "/",
            "/user/login",
            "/user/create-user"
    ));

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (request.getSession().getAttribute("currentUserObj") == null && !allowedURIs.contains(request.getRequestURI())) {

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
