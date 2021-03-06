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
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "MovieServlet", urlPatterns = "/api/movies")
public class MovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;


    String defaultMovieQuery(){
        return "select movies.id, movies.title, movies.year, movies.director, group_concat(distinct genres.name separator ', ') as threeGenres, substring_index(group_concat(stars.name separator ','), ',', 3) as threeStars, substring_index(group_concat(stars.id separator ','), ',', 3) as threeStarIds, movies.rating from (select movies.id, movies.title, movies.year, movies.director, ratings.rating from movies left join ratings on (movies.id = ratings.movieId) order by ratings.rating desc limit 20) as movies left join stars_in_movies on (movies.id = stars_in_movies.movieId) left join stars on (stars.id = stars_in_movies.starId) left join genres_in_movies on (movies.id = genres_in_movies.movieId) left join genres on (genres.id = genres_in_movies.genreId) group by movies.id order by movies.rating desc;";
    }
    String addLimits(String query, String pageOffset, String pageLimit){
        String limitClause = (pageLimit == null) ? "limit 20 " : "limit " + pageLimit + " ";
        if (pageOffset != null)
            limitClause += "offset " + pageOffset + " ";
        return query + limitClause;
    }
    String addSortingCondition(String query, String sortBy){
        query += "order by ";
        if (sortBy != null) {
            switch (sortBy) {
                case "TURU":
                    query += "movies.title asc, ratings.rating asc ";
                    break;
                case "TURD":
                    query += "movies.title asc, ratings.rating desc ";
                    break;
                case "TDRU":
                    query += "movies.title desc, ratings.rating asc ";
                    break;
                case "TDRD":
                    query += "movies.title desc, ratings.rating desc ";
                    break;
                case "RUTU":
                    query += "ratings.rating asc, movies.title asc ";
                    break;
                case "RUTD":
                    query += "ratings.rating asc, movies.title desc ";
                    break;
                case "RDTU":
                    query += "ratings.rating desc, movies.title asc ";
                    break;
                case "RDTD":
                    query += "ratings.rating desc, movies.title desc ";
                    break;
            }
        }
        else{
            query += "ratings.rating desc ";
        }

        return query;
    }
    String constructSearchQuery(String starOfMovie, String titleOfMovie, String yearOfRelease, String directorOfMovie, String sortBy, String pageOffset, String pageLimit){
        // Return a default search if no fields were filled
        if (starOfMovie == null && titleOfMovie == null && yearOfRelease == null && directorOfMovie == null){
            return defaultMovieQuery();
        }

        // Return a custom search if fields were filled
        String searchQuery = "select movies.id, movies.title, movies.year, movies.director, group_concat(distinct genres.name order by genres.name asc separator ',') as threeGenres, substring_index(group_concat(distinct stars.name order by stars.name asc separator ','), ',', 3) as threeStars, substring_index(group_concat(stars.id separator ','), ',', 3) as threeStarIds, ratings.rating from (select movies.id, movies.title, movies.year, movies.director from (select * from movies left join ratings on (movies.id = ratings.movieId) ";

        if (titleOfMovie != null || yearOfRelease != null || directorOfMovie != null)
            searchQuery += "where ";
        if (titleOfMovie!= null)
            searchQuery += String.format("movies.title like '%%%s%%' AND ",titleOfMovie);
        if (yearOfRelease!= null)
            searchQuery += String.format("movies.year = %s AND ",yearOfRelease);
        if (directorOfMovie!= null)
            searchQuery += String.format("movies.director like '%%%s%%' AND ", directorOfMovie);
        //Remove "AND " at the end.
        searchQuery = searchQuery.substring(0,searchQuery.length()-4);

        searchQuery = addSortingCondition(searchQuery, sortBy);
        searchQuery = addLimits(searchQuery, pageOffset, pageLimit);
        searchQuery += ") as movies left join stars_in_movies on (movies.id = stars_in_movies.movieId) left join stars on (stars.id = stars_in_movies.starId) ";

        if (starOfMovie!= null)
            searchQuery += String.format("where stars.name like '%%%s%%' ",starOfMovie);

        searchQuery += "group by movies.id) as movies left join stars_in_movies on (movies.id = stars_in_movies.movieId) left join stars on (stars.id = stars_in_movies.starId) left join genres_in_movies on (movies.id = genres_in_movies.movieId) left join genres on (genres.id = genres_in_movies.genreId) left join ratings on (movies.id = ratings.movieId) group by movies.id ";

        searchQuery = addSortingCondition(searchQuery, sortBy);

        return searchQuery;
    }
    String constructBrowseQuery(String byCategory, String givenCategory, String sortBy, String pageOffset, String pageLimit){
        String browseQuery = "select movies.id, movies.title, movies.year, movies.director, group_concat(distinct genres.name order by genres.name asc separator ',') as threeGenres, substring_index(group_concat(distinct stars.name order by stars.name asc separator ','), ',', 3) as threeStars, substring_index(group_concat(stars.id separator ','), ',', 3) as threeStarIds, ratings.rating from (select movies.id, movies.title, movies.year, movies.director from movies ";

        if (byCategory.equals("title")) {
            browseQuery += "left join ratings on (movies.id = ratings.movieId) ";
            browseQuery += String.format("where movies.title like '%s%%' ", givenCategory);
            browseQuery = addSortingCondition(browseQuery, sortBy);
            browseQuery = addLimits(browseQuery, pageOffset, pageLimit);
        }
        else if (byCategory.equals("genre")){
            browseQuery += "left join genres_in_movies on (movies.id = genres_in_movies.movieId) left join genres on (genres.id = genres_in_movies.genreId) left join ratings on (movies.id = ratings.movieId) ";
            browseQuery += String.format("where genres.name = '%s' ",givenCategory);
            browseQuery = addSortingCondition(browseQuery, sortBy);
            browseQuery = addLimits(browseQuery, pageOffset, pageLimit);
        }
        else{
            return defaultMovieQuery();
        }
        browseQuery += ") as movies left join stars_in_movies on (movies.id = stars_in_movies.movieId) left join stars on (stars.id = stars_in_movies.starId) left join genres_in_movies on (movies.id = genres_in_movies.movieId) left join genres on (genres.id = genres_in_movies.genreId) left join ratings on (movies.id = ratings.movieId) group by movies.id ";

        browseQuery = addSortingCondition(browseQuery, sortBy);

        return browseQuery;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type

        StringBuilder requestURL = new StringBuilder("movie-list.html");
        String queryString = request.getQueryString();

        if (queryString == null) {
            request.getSession().setAttribute("oldMovieLink",requestURL.toString());
        } else {
            request.getSession().setAttribute("oldMovieLink",requestURL.append('?').append(queryString).toString());
        }


        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Declare our statement
            Statement statement = dbcon.createStatement();

            // Query is either Browse or Search
            String query;
            if (request.getParameter("type").equals("search"))
                query = constructSearchQuery(request.getParameter("starOfMovie"), request.getParameter("titleOfMovie"), request.getParameter("yearOfRelease"), request.getParameter("directorOfMovie"), request.getParameter("sortBy"), request.getParameter("pageOffset"), request.getParameter("pageLimit"));
            else if (request.getParameter("type").equals("browse"))
                query = constructBrowseQuery(request.getParameter("byCategory"),request.getParameter("givenCat"), request.getParameter("sortBy"), request.getParameter("pageOffset"), request.getParameter("pageLimit"));
            else
                query = defaultMovieQuery();
            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String threeStarsIds = rs.getString("threeStarIds");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String threeGenres = rs.getString("threeGenres");
                String threeStars = rs.getString("threeStars");
                String rating = rs.getString("rating");
                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("three_stars_ids", threeStarsIds);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("three_genres", threeGenres);
                jsonObject.addProperty("three_stars", threeStars);
                jsonObject.addProperty("rating", rating);

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

    }
}
