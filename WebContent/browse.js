
function handleReturnedGenres(resultData){

    // Populate the genre selections
    // Find the empty table body by id "genre_browse_body"
    let genreBrowseTableBody = jQuery("#genre_browse_body");
    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<td>" +
            "<a href = " +
            // Add a link to single-star.html with id passed with GET url parameter
            "movie-list.html?type=browse&byCategory=genre&givenCat=" + resultData[i]["genre_name"] + ">" +
            resultData[i]["genre_name"] +
            "</a>" +
            "</td>";

        // Append the row created to the table body, which will refresh the page
        genreBrowseTableBody.append(rowHTML);
    }
}
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genres", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleReturnedGenres(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});