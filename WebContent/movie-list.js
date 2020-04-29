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

function replaceUrlParam(url, paramName, paramValue)
{
    if (paramValue == null) {
        paramValue = '';
    }
    var pattern = new RegExp('\\b('+paramName+'=).*?(&|#|$)');
    if (url.search(pattern)>=0) {
        return url.replace(pattern,'$1' + paramValue + '$2');
    }
    url = url.replace(/[?#]$/,'');
    return url + (url.indexOf('?')>0 ? '&' : '?') + paramName + '=' + paramValue;
}


function constructAPIURL(){
    var returnURL = "api/movies?";
    if (getParameterByName("type")!= null){
        returnURL += "type="; returnURL += getParameterByName("type"); returnURL += "&";
    }
    // SEARCH REQUEST
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
    // BROWSE REQUEST
    if (getParameterByName("byCategory")!= null){
        returnURL += "byCategory="; returnURL += getParameterByName("byCategory"); returnURL += "&";
    }
    if (getParameterByName("givenCat")!= null){
        returnURL += "givenCat="; returnURL += getParameterByName("givenCat"); returnURL += "&";
    }
    // SORT RESULTS, PAGE OFFSET, PAGE LIMIT
    if (getParameterByName("sortBy")!= null){
        returnURL += "sortBy="; returnURL += getParameterByName("sortBy"); returnURL += "&";
    }
    if (getParameterByName("pageOffset")!= null){
        returnURL += "pageOffset="; returnURL += getParameterByName("pageOffset"); returnURL += "&";
    }
    if (getParameterByName("pageLimit")!= null){
        returnURL += "pageLimit="; returnURL += getParameterByName("pageLimit"); returnURL += "&";
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

        rowHTML += "<td>";
        let splitGenres = resultData[i]["three_genres"].split(",");
        for (let i = 0; i<splitGenres.length; i++){
            rowHTML += "<a href = " +
                "movie-list.html?type=browse&byCategory=genre&givenCat=" + splitGenres[i] + ">";
            rowHTML += splitGenres[i];
            rowHTML += "</a>";
            rowHTML += ", ";
        }
        rowHTML += "</td>";

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

        rowHTML += "<td> <button type='button' onclick='addToCart(\""
                        + resultData[i]["movie_id"] + "\")'>Add Movie to Cart</button> </td>";

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}
function successMessage(succ){
    alert(succ);
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


$(document).ready(function(){
    $("select.sort-criteria").change(function(){
        var selectedSort = $(this).children("option:selected").val();
        var sortURL = window.location.href;

        sortURL = replaceUrlParam(sortURL,"sortBy",selectedSort);

        alert("You have selected the country - " + sortURL);
        window.location.replace(sortURL);
    });
});

var pageNumber = (getParameterByName("pageOffset")==null) ? 0 : parseInt(getParameterByName("pageOffset"))/20;
if (pageNumber > 0)
    $("#prev-button").removeClass("disabled");
else if (pageNumber <= 0)
    $("#prev-button").addClass("disabled");

$(document).ready(function(){
    $("#next-button").click(function(){
        pageNumber++;
        alert(pageNumber);

        let nextURL = window.location.href;
        let pageOffset = $("select.page-limit").children("option:selected").val();
        nextURL = replaceUrlParam(nextURL,"pageOffset",pageNumber*pageOffset);
        alert(nextURL);
        window.location.replace(nextURL);
    });
    $("#prev-button").click(function(){

        pageNumber--;

        let prevURL = window.location.href;
        prevURL = replaceUrlParam(prevURL,"pageOffset",pageNumber*20);
        alert(prevURL);
        window.location.replace(prevURL);

    });
});


$(document).ready(function(){
    $("select.page-limit").change(function(){
        let selectLimit = $(this).children("option:selected").val();
        let limitURL = window.location.href;

        limitURL = replaceUrlParam(limitURL,"pageLimit",selectLimit);
        window.location.replace(limitURL);
    });
});

