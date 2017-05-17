<%@ page
	import="java.util.Enumeration,
                 java.util.TreeSet,
                 java.util.Iterator"%>


<body style="font-family: verdana; font-size: 10px;">

	<span style="font-size: 11px;"><b>Session attributes</b></span>
	<table border="1" RULES="ROWS" cellpadding="4">
		<%
      TreeSet set = new TreeSet();
      Enumeration e = request.getSession().getAttributeNames();
      while (e.hasMoreElements()) {
         set.add(e.nextElement());
      }

      Iterator i = set.iterator();
      while (i.hasNext()) {
         String s = (String) i.next();
   %>


		<tr>
			<td style="font-size: 9px;"><%=s%></td>
			<td style="font-size: 9px;"><%=session.getAttribute(s).getClass().getName()%></td>
			<td style="font-size: 9px;"><%=session.getAttribute(s).toString()%></td>
		</tr>

		<%
      }
   %>
	</table>

	<p>&nbsp;</p>
	<span style="font-size: 11px;"><b>App-scoped attributes</b></span>
	<table border="1" RULES="ROWS" cellpadding="4">
		<%
      set = new TreeSet();
      e = pageContext.getServletContext().getAttributeNames();
      while (e.hasMoreElements()) {
         set.add(e.nextElement());
      }

      i = set.iterator();
      while (i.hasNext()) {
         String s = (String) i.next();
   %>

		<tr>
			<td style="font-size: 9px;"><%=s%></td>
			<td style="font-size: 9px;"><%=pageContext.getServletContext().getAttribute(s).getClass().getName()%></td>
			<td style="font-size: 9px;"><%=pageContext.getServletContext().getAttribute(s).toString()%></td>
		</tr>

		<%
      }
   %>

	</table>