<%--
  Created by IntelliJ IDEA.
  User: spillai
  Date: 3/16/2017
  Time: 10:57 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
  <title>Upload File Request Page</title>
</head>
<body>
<form method="POST" action="svc/student/uploadForm" enctype="multipart/form-data">
  File to upload: <input type="file" name="file">


  <input type="submit" value="Preview"> Press here to upload the file!</br>
</form>
<form method="POST" action="svc/student/recordingSubmit" enctype="multipart/form-data">
  <input type="submit" value="Save Recording" >
</form>
<form method="POST" action="svc/student/recordingCancel" enctype="multipart/form-data">
  <input type="submit" value="Cancel Recording" >
</form>

</body>
</html>
