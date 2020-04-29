package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@WebServlet(name = "ShoppingServlet", urlPatterns = "/api/shopping")
public class ShoppingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;


    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String item = request.getParameter("movie_id");
        HttpSession session = request.getSession();

        JsonArray previousItems = (JsonArray) session.getAttribute("previousItems");

        if (previousItems == null) {
            previousItems = new JsonArray();
            JsonObject moviePair = new JsonObject();
            moviePair.addProperty(item, 1);
            previousItems.add(moviePair);
            session.setAttribute("previousItems", previousItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousItems) {
                JsonObject moviePair = new JsonObject();
                moviePair.addProperty(item, 1);
                previousItems.add(moviePair);
            }
        }

        response.getWriter().write(previousItems.toString());
    }

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        JsonArray previousItems = (JsonArray) session.getAttribute("previousItems");



        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Declare our statement
            Statement statement = dbcon.createStatement();

            // Query is either Browse or Search
            String query = "select movies.title from movies where movies.id in (";
            for (int i = 0; i<previousItems.size(); i++ ){
                if (i==previousItems.size()-1)
                    query += "'" + ((JsonObject) previousItems.get(i)).keySet().iterator().next() + "'";
                else
                    query += "'" + ((JsonObject) previousItems.get(i)).keySet().iterator().next() + "',";
            }
            query += ")";


            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_title = rs.getString("title");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_title", movie_title);


                jsonArray.add(jsonObject);
            }

            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            rs.close();
            statement.close();
            dbcon.close();
        } catch (Exception e) {

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);

        }
        out.close();

        // write all the data into the jsonObject
        response.getWriter().write(previousItems.toString());
    }

}