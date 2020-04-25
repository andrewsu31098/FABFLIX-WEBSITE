/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function constructAPIURL(){
    var returnURL = "api/movies?";
    if (getParameterByName("type")!= null){
        returnURL += "type="; returnURL += getParameterByName("type"); returnURL += "&";
    }
    if (getParameterByName("starOfMovie")!= null){
        returnURL += "starOfMovie="; returnURL += getParameterByName("starOfMovie"); returnURL += "&";
    }
    if (getParameterByName("titleOfMovie")!= null){
        returnURL += "titleOfMovie="; returnURL += getParameterByName("titleOfMovie"); returnURL += "&";
    }
    if (getParameterByName("yearOfRelease")!= null){
        returnURL += "yearOfRelease="; returnURL += getParameterByName("yearOfRelease"); returnURL += "&";
    }
    if (getParameterByName("directorOfMovie")!= null){
        returnURL += "directorOfMovie="; returnURL += getParameterByName("directorOfMovie"); returnURL += "&";
    }

    if (getParameterByName("byCategory")!= null){
        returnURL += "byCategory="; returnURL += getParameterByName("byCategory"); returnURL += "&";
    }
    if (getParameterByName("givenCat")!= null){
        returnURL += "givenCat="; returnURL += getParameterByName("givenCat"); returnURL += "&";
    }
    return returnURL;
}


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */



function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(100, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<td>" +
                "<a href = " +
            // Add a link to single-star.html with id passed with GET url parameter
                    "single-movie.html?movieId=" + resultData[i]["movie_id"] + ">" +
                    resultData[i]["movie_title"] +
                "</a>" +
            "</td>";
        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";
        rowHTML += "<td>" + resultData[i]["three_genres"] + "</td>";
        rowHTML += "<td>";
        let splitStars = resultData[i]["three_stars"].split(",");
        let splitSIds = resultData[i]["three_stars_ids"].split(",");
        for (let i = 0; i<splitStars.length; i++){
            rowHTML += "<a href = " +
                            "single-star.html?starId=" + splitSIds[i] + ">";
            rowHTML += splitStars[i];
            rowHTML += "</a>";
            rowHTML += ", ";
        }
        rowHTML += "</td>";
        rowHTML += "<td>" + resultData[i]["rating"] + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
var apiURL = constructAPIURL();
// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: apiURL, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});