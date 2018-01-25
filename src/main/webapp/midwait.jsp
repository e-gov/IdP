<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<script type="text/javascript">
    function checkResult()
    {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.onreadystatechange = function() {
            if (xmlHttp.readyState == 4)
                if(xmlHttp.status == 200 && xmlHttp.responseText == "WAIT")
                    window.setTimeout(checkResult(),5000);
                else
                    document.getElementById("startmid").submit();
        }
        xmlHttp.open("GET", "${checkUrl}", true);
        xmlHttp.send(null);
    }
    window.onload=function(){
        window.setTimeout(checkResult, 5000);
    };
</script>

<head>
    <title>Wait until authenicated</title>
</head>
<body>

Challenge: ${challenge}


    <form id="startmid" method="post" action="midcheck">
        <input type="hidden" name="SAMLRequest" value="${challenge}"/>
        <input type="hidden" name="sessionToken" value="${sessionToken}"/>
        <noscript>
            <button type="submit" id="checkbtn">Check authentication result</button>
        </noscript>
    </form>
</body>
</html>
