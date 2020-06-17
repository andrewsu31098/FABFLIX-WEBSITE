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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



@WebServlet(name = "ShoppingServlet", urlPatterns = "/api/shopping")
public class ShoppingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;


    String constructShoppingQuery(HashMap<String,Integer> prevOrders){
        String query = "select movies.id, movies.title from movies where movies.id in (";
        for (Map.Entry mapElement : prevOrders.entrySet()) {
            query += "?,";
        }
        query = query.substring(0,query.length()-1);
        query += ")";
        return query;
    }


    // Adds movie into shopping cart cookie and error/success message.
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String postType = request.getParameter("postType");
        String movId = request.getParameter("movieId");
        HttpSession session = request.getSession();

        // get the previous items in a Hashmap
        HashMap<String, Integer> prevOrders = (HashMap<String, Integer>) session.getAttribute("prevOrders");


        switch (postType){
            case "add":
                if (prevOrders == null) {
                    prevOrders = new HashMap<String, Integer>();
                    prevOrders.put(movId, 1);
                    session.setAttribute("prevOrders", prevOrders);
                } else {
                    // prevent corrupted states through sharing under multi-threads
                    // will only be executed by one thread at a time
                    synchronized (prevOrders) {
                        // Default Dict behavior YES!
                        prevOrders.merge(movId, 1, Integer::sum);
                    }
                }
                break;
            case "sub":
                synchronized (prevOrders) {
                    // Default Dict behavior YES!
                    prevOrders.merge(movId, -1, Integer::sum);
                }
                if (prevOrders.get(movId)<= 0){
                    synchronized (prevOrders){
                        prevOrders.remove(movId);
                    }
                }
                break;
            case "remove":
                synchronized (prevOrders){
                    prevOrders.remove(movId);
                }
                break;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("movieId",movId);
        jsonObject.addProperty("count",prevOrders.get(movId));

        response.getWriter().write(jsonObject.toString());
    }

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        // get the previous items in a Hashmap
        HashMap<String, Integer> prevOrders = (HashMap<String, Integer>) session.getAttribute("prevOrders");


        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Construct a query with parameter represented by "?"
            String query = constructShoppingQuery(prevOrders);

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            int index = 1;
            for (Map.Entry mapElement : prevOrders.entrySet()) {
                statement.setString(index++, (String) mapElement.getKey());
            }

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate Hashmap and prepare response object
            if (prevOrders != null) {
                while (rs.next()){
                    JsonObject jsonObject = new JsonObject();

                    String movieId = rs.getString("id");
                    jsonObject.addProperty("movieId", movieId);
                    jsonObject.addProperty("movieTitle", rs.getString("title"));
                    jsonObject.addProperty("count", prevOrders.get(movieId));
                    jsonObject.addProperty("price", movieId.hashCode() & 0xfffffff %20 );
                    jsonArray.add(jsonObject);
                }

            }

            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);

        }
        out.close();

    }

}