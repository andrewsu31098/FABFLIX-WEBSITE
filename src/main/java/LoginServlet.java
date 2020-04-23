package main.java;

import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Retrieve login data from url request.
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Response mime type
       // response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
        JsonObject responseJsonObject = new JsonObject();

        try{
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Construct a query with parameter represented by "?"
            String successQuery = "select EXISTS(select * from customers where customers.email = ? and customers.password = ?) as successMatch;";
            String userQuery = "select EXISTS(select * from customers where customers.email = ?) as userMatch;";

            // Declare our statement
            PreparedStatement successStatement = dbcon.prepareStatement(successQuery);
            PreparedStatement userStatement = dbcon.prepareStatement(userQuery);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            successStatement.setString(1, username);
            successStatement.setString(2, password);
            userStatement.setString(1, username);

            // Perform the query
            ResultSet successSet = successStatement.executeQuery();
            ResultSet userSet = userStatement.executeQuery();
            // Move to the first row entry
            userSet.next(); successSet.next();

            boolean successMatch = successSet.getBoolean("successMatch");
            boolean userMatch = userSet.getBoolean("userMatch");

            if (successMatch) {
                // Login success:

                // set this user into the session
                request.getSession().setAttribute("user", new User(username));

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

            } else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");

                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                if (!userMatch) {
                    responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
                } else {
                    responseJsonObject.addProperty("message", "incorrect password");
                }
            }
            out.write(responseJsonObject.toString());


            successSet.close(); userSet.close();
            successStatement.close(); userStatement.close();
            dbcon.close();
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            jsonObject.addProperty("status","fail");
            jsonObject.addProperty("message",e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }



    }
}