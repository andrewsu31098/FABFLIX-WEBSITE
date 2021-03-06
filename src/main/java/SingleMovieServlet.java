package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
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

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("movieId");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Construct a query with parameter represented by "?"
            String query = "select movies.id,  group_concat(distinct stars.id order by subStar.starCount desc, stars.name asc separator ',') as allStarIds, movies.title, movies.year, movies.director, group_concat(distinct genres.name order by genres.name asc separator ',') as allGenres, group_concat(distinct stars.name order by subStar.starCount desc, stars.name asc separator ',') as allStars, ratings.rating from 	movies join ratings on (movies.id = ratings.movieId) join genres_in_movies on (movies.id = genres_in_movies.movieId) join genres on (genres_in_movies.genreId = genres.id) join stars_in_movies on (movies.id = stars_in_movies.movieId) join stars on (stars.id = stars_in_movies.starId) join (select stars.id, count(movies.id) as starCount from  (select stars.id from stars left join stars_in_movies on (stars.id = stars_in_movies.starId) left join movies on (movies.id = stars_in_movies.movieId) where movies.id = ?) as starRoster join stars on (stars.id = starRoster.id) join stars_in_movies on (stars.id = stars_in_movies.starId) join movies on (movies.id = stars_in_movies.movieId) group by stars.id) as subStar on (stars.id = subStar.id) where movies.id = ? group by movies.id;";

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);
            statement.setString(2, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject jsonObject = new JsonObject();

            // Iterate through each row of rs
            while (rs.next()) {

                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");

                String allGenres = rs.getString("allGenres");
                String allStars = rs.getString("allStars");
                String allStarIds = rs.getString("allStarIds");
                String rating = rs.getString("rating");
                // Create a JsonObject based on the data we retrieve from rs


                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);

                jsonObject.addProperty("all_genres", allGenres);
                jsonObject.addProperty("all_stars", allStars);
                jsonObject.addProperty("all_stars_ids", allStarIds);
                jsonObject.addProperty("rating", rating);

            }

            // write JSON string to output
            out.write(jsonObject.toString());
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

    }

}