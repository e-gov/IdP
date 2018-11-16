(function() {

    function checkResult() {
        var checkUrl = document.body.getAttribute('data-check-url');

        var xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function() {
            if (this.readyState !== 4) return;
            if (this.status === 200 && this.responseText === 'WAIT') {
                window.setTimeout(checkResult, 5000);
            } else {
                document.forms['mobileIdCheckForm'].submit();
            }
        };

        // dummy ts parameter is needed to overcome IE caching
        xhttp.open('GET', checkUrl + '&ts=' + Date.now(), true);
        xhttp.send();
    };

    window.setTimeout(checkResult, 5000);

})();