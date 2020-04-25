let search_form = $('#search_form');

function appendParameter(youngURL, parameter){
    if (document.getElementById(parameter).value){
        youngURL += parameter + "=" + document.getElementById(parameter).value + "&";
    }
    return youngURL;
}

function submitSearchForm(formSubmitEvent) {
    console.log("submitting search form to movie-list");
    /*Based off login.js*/
    formSubmitEvent.preventDefault();
    $('#search_error_message').text(document.getElementById("titleOfMovie").value);
    var movieLink = "movie-list.html?type=search&";
    movieLink = appendParameter(movieLink, "titleOfMovie");
    movieLink = appendParameter(movieLink, "yearOfRelease");
    movieLink = appendParameter(movieLink, "directorOfMovie");
    movieLink = appendParameter(movieLink, "starOfMovie");
    // Remove an & symbol at the end;
    movieLink = movieLink.substring(0,movieLink.length-1);
    window.location.assign(movieLink);
}
search_form.submit(submitSearchForm);