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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows

    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<td>" + resultData["movie_title"] + "</td>";
    rowHTML += "<td>" + resultData["movie_year"] + "</td>";
    rowHTML += "<td>" + resultData["movie_director"] + "</td>";

    rowHTML += "<td>"
    let splitGenres = resultData["all_genres"].split(',');
    for (let i = 0; i<splitGenres.length; i++){
        rowHTML += "<a href = movie-list.html?type=browse&byCategory=genre&givenCat=" + splitGenres[i] + ">"
            + splitGenres[i] + "</a>" + ", ";
    }
    rowHTML += "</td>";

    rowHTML += "<td>";
    let splitStars = resultData["all_stars"].split(',');
    let splitStarIds = resultData["all_stars_ids"].split(',');
    for (let i = 0; i<splitStars.length; i++){
        rowHTML += "<a href = single-star.html?starId=" + splitStarIds[i] + ">"
            + splitStars[i] + "</a>" + ", ";
    }
    rowHTML += "</td>";

    rowHTML += "<td>" + resultData["rating"] + "</td>";

    rowHTML += "<td> <button type='button' onclick='addToCart(\""
        + resultData["movie_id"] + "\")'>Add Movie to Cart</button> </td>";

    rowHTML += "</tr>";

    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.append(rowHTML);
}


function successMessage(succ){
    alert("Added to cart. Success!");
}
function addToCart(movieId){
    alert(movieId);
    $.ajax("api/shopping", {
        method: "POST",
        data: {"postType":"set", "movie_id":movieId},
        success: successMessage
    });
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('movieId');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?movieId=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});