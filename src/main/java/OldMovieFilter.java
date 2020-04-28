package main.java;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebFilter(filterName = "OldMovieFilter", urlPatterns = "/api/movies")
public class OldMovieFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (httpRequest.getParameter("returnOld") != null){
            String oldMovieLink = (String) httpRequest.getSession().getAttribute("oldMovieLink");
            if (oldMovieLink != null)
                httpResponse.sendRedirect(httpRequest.getContextPath()+"/"+oldMovieLink);
            else
                httpResponse.sendRedirect(httpRequest.getContextPath()+"/movie-list.html");
        }
        chain.doFilter(request, response);
        return;
    }

    public void init(FilterConfig fConfig) {
    }

    public void destroy() {
        // ignored.
    }

}