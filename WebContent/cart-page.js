/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */


function handleTitleResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let cartTableBodyElement = jQuery("#cart_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(100, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + resultData[i]["movie_title"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_title"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_title"] + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        cartTableBodyElement.append(rowHTML);
    }
}

// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/shopping", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleTitleResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});