package main.java;

import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@WebServlet(name = "ShoppingServlet", urlPatterns = "/api/shopping")
public class ShoppingServlet extends HttpServlet {

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String item = request.getParameter("movie_id");
        HttpSession session = request.getSession();


        JsonObject previousItems = (JsonObject) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new JsonObject();
            previousItems.addProperty(item,1);
            session.setAttribute("previousItems", previousItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousItems) {
                previousItems.addProperty(item,1);
            }
        }


        response.getWriter().write(previousItems.toString());
    }
}