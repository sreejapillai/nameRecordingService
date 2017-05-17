<%@ page import="java.lang.String"%>
<%@ page import="com.fullspan.jwhich.JWhich"%>

<%
  String cls= request.getParameter("class");
%>

Enter the class you wish to find: i.e. atg/common/access/AccessRow.class

<FORM>
	<INPUT TYPE=text NAME="class" SIZE="64" VALUE=`cls`> 
	<INPUT TYPE=submit VALUE="JWhich this class!">
</FORM>


<%
  if (cls!=null && !cls.equals("")){
      out.println("JWhich.printResourceLocations prints to standard out<br>" );
      JWhich.printResourceLocations(cls);
  }
%>


