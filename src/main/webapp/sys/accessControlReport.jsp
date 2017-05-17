<%@ page import="org.w3c.dom.Document"%>
<%@ page import="java.net.URL"%>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory"%>
<%@ page import="javax.xml.parsers.DocumentBuilder"%>
<%@ page import="java.io.InputStream"%>
<%@ page import="java.util.*"%>
<%@ page import="org.w3c.dom.Element"%>
<%@ page import="org.w3c.dom.NodeList"%>
<%@ page import="org.w3c.dom.Node"%>
<%@ page import="hbs.common.appnaccess.*"%>
<%@ page import="hbs.common.appnaccess.web.AppnAccessControlFilter"%>
<%@ page import="org.springframework.context.ApplicationContext"%>
<%@ page
	import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ page
	import="org.springframework.web.context.support.WebApplicationContextUtils"%>

<%
    ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(application);
    IAuthzClient client = (IAuthzClient) ctx.getBean(IAuthzClient.class);
    Collection<IApplicationRef> appns = client.getRegisteredApplications();
%>
<html>
<head>
<title>HBS Application Access Control Report</title>
<style TYPE="text/css">
body {
	font-family: verdana;
	font-size: 11px;
	background-color: #cdcdcd;
}

table {
	border-style: solid;
	border-color: #888888;
	border-width: 1px;
	width: 100%;
}

table.userReport {
	border-color: #000000;
	background-color: #ffff00;
	width: 100%;
}

td {
	padding: 4px;
	font-family: verdana;
	font-size: 10px;
}

td.appn {
	background-color: #ffffff;
	font-weight: bold;
}

td.role {
	background-color: #dddddd;
	padding-left: 20px;
}

td.memberRole {
	border-top-style: solid;
	border-top-width: 1px;
	border-top-color: #999999;
}

td.group {
	background-color: #bbbbbb;
	padding-left: 40px;
}

td.member {
	border-color: #999999;
	border-width: 1px;
	background-color: #bbbbbb;
	padding-left: 60px;
}
</style>
</head>

<body>
	<h1>HBS Application Access Control Report</h1>

	<h2>Registered Applications</h2>

	<ul>
		<% for (IApplicationRef ref : appns) {
    %>
		<li><%=ref.getApplicationDescription()%> (<%=ref.getApplicationTypeCode()%>)</li>
		<% } %>
	</ul>

	<table
		style="border-width: 1px; border-color: #000077; border-style: solid; background-color: #ccccff;"
		width="100%">
		<tr>
			<form action="./accessControlReport.jsp" method="get">
				<td align="right">Report for user: search by prsn id:</td>
				<td><input type="text" size="8" name="prsnId"> <input
					type="submit" value="Search"></td>
			</form>
		</tr>
		<tr>
			<form action="./accessControlReport.jsp" method="get">
				<td align="right">Report for user: search by email address:</td>
				<td><input type="text" size="48" name="email"> <input
					type="submit" value="Search"></td>
			</form>
		</tr>
		<tr>
			<td align="right">&nbsp;</td>
			<td>Note: AppnAccess Web Service clients do not cache Access
				Control Info information locally</td>
		</tr>
	</table>

	<%
    if (request.getParameter("prsnId") != null || request.getParameter("email") != null) {
        IPerson p = null;
        long prsnId;
        String email;
        if (request.getParameter("prsnId") != null) {
            prsnId = Long.parseLong(request.getParameter("prsnId"));
            p = client.getPerson(prsnId);
        } else if (request.getParameter("email") != null) {
            email = request.getParameter("email");
            p = client.getPerson(email);
        }

        if (p != null) {
%>
	<table class="userReport">
		<tr>
			<td>
				<h2>
					User Report:
					<%=p.getFirstName()%>
					<%=p.getLastName()%>
					(<%=p.getPrimaryEmailAddress()%>
					::<%=p.getPersonId()%>)
				</h2> <%
                for (IApplicationRef appnRef : appns) {
                    Collection<IAccessRole> roles = client.getApplicationAccessRoles(appnRef.getApplicationTypeCode());
            %>
				<table width="100%" cellspacing="0" border="0" cellpadding="3">
					<tr>
						<td class="appn" colspan="2">Application: <span
							style="color: red"><%=appnRef.getApplicationDescription()%>
								(<%=appnRef.getApplicationTypeCode()%>)</span>
						</td>
					</tr>
					<%
                    for (IAccessRole role : roles) {
                %>
					<tr>
						<td class="role memberRole">Access Role: <span
							style="color: red"> <%=role.getRoleLongName()%> (<%=role.getRoleShortName()%>)
						</span>
						</td>
						<td class="role memberRole" align="right">User has role: <%=role.hasRole(p)%>
						</td>
					</tr>

					<%
                    }
                %>
				</table> <%
                }
            %>
			</td>
		</tr>
	</table>
	<%
        }
    }
%>

	<h2>Registered Access Roles</h2>
	<% for (IApplicationRef ref : appns) {
    Collection<IAccessRole> roles = client.getApplicationAccessRoles(ref.getApplicationTypeCode());
%>

	<table width="100%" cellspacing="0" cellpadding="3" border="0">
		<tr>
			<td class="appn">Application: <span style="color: red;">
					<%=ref.getApplicationDescription()%> (<%=ref.getApplicationTypeCode()%>)
			</span>
			</td>
		</tr>


		<% for (IAccessRole role : roles) {
    %>
		<tr>
			<td class="role"><span style="font-weight: bold;"> Access
					Role: <span style="color: red;"> <%=role.getRoleLongName()%>
						(<%=role.getRoleShortName()%>)
				</span>
			</span> Mapped Groups:</td>
		</tr>

		<%
        if (role.getGroups().size() == 0) {
    %>
		<tr>
			<td class="group">None</td>
		</tr>
		<%
    } else {
        TreeSet<IGroup> groups = new TreeSet<IGroup>(new GroupComparator());
        groups.addAll(role.getGroups());
        for (IGroup g : groups) {
    %>
		<tr>
			<td class="group">(<%=g.getGroupType()%>) <%=g.getGroupDescription()%>
			</td>
		</tr>

		<%
        if (g.getMemberIds() != null && g.getMemberIds().size() > 0) {
            TreeSet<IPerson> s = new TreeSet<IPerson>(new PersonComparator());
            for (Long id : g.getMemberIds()) {
                s.add(client.getPerson(id));
            }
            for (IPerson m : s) {
    %>
		<tr>
			<td class="member"><%=m.getFirstName()%> <%=m.getLastName()%> (<%=m.getPrimaryEmailAddress()%>)</td>
		</tr>
		<%
                    }
                }
            }

        }
    %>

		<% } %>
	</table>
	<% } %>

	<h2>Access Role Filter Mappings</h2>

	<%
    try {
        List mappings = getMappingsReport(pageContext);
        Iterator i;
%>

	<table width="100%" cellspacing="0" border="0" cellpadding="3">
		<tr>
			<td class="appn"><b>Filter</b></td>
			<td class="appn"><b>Role(s)</b></td>
			<td class="appn"><b>Mapped URL(s)</b></td>
		</tr>

		<%
        i = mappings.iterator();
        while (i.hasNext()) {
            MappedRole mr = (MappedRole) i.next();
    %>
		<tr>
			<td class="role"><%=mr.filterName%></td>
			<td class="role"><%=mr.mappedRoles%></td>
			<td class="role"><%=mr.mappedUrls%></td>
		</tr>
		<%
        }
    %>

	</table>
	<%
} catch (Exception e) {
%>

	<table width="100%" cellspacing="0" border="0" cellpadding="3"
		style="border-color: red;">
		<tr>
			<td class="appn" colspan="2"><b>Error: failed to read app
					config for filter mappings. Generated exception of type <%=e.getClass().getName()%>
					with message <%=e.getMessage()%>.
			</b></td>
		</tr>
	</table>
	<%
    }
%>


	<%!
    class GroupComparator implements Comparator<IGroup> {
        public int compare(IGroup o1, IGroup o2) {
            int c;
            if ((c = o1.getGroupType().compareTo(o2.getGroupType())) !=0) return c;
            if ((c = o1.getGroupDescription().compareTo(o2.getGroupDescription())) !=0) return c;
            return new Long(o1.getGroupId()).compareTo(o2.getGroupId());
        }
    }

    class PersonComparator implements Comparator<IPerson> {
        public int compare(IPerson o1, IPerson o2) {
            int c;
            if ((c = o1.getLastName().compareTo(o2.getLastName())) != 0) return c;
            if ((c = o1.getFirstName().compareTo(o2.getFirstName())) !=0) return c;
            return (new Long(o1.getPersonId()).compareTo(o2.getPersonId()));
        }
    }

    class MappedRole {
        String filterName;
        String mappedRoles;
        String mappedUrls;
    }

    List getMappingsReport(PageContext page) throws Exception {
        Document doc = null;
        URL url = page.getServletConfig().getServletContext().getResource("/WEB-INF/web.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream in = url.openStream();
        doc = builder.parse(in);

        Map mappedRoles = new HashMap();
        Map mappings = new HashMap();

        prepMaps(doc, mappedRoles, mappings);
        loadUrls(doc, mappings);

        List list = listRoleMappings(mappedRoles, mappings);
        return list;
    }


    List listRoleMappings(Map mappedRoles, Map mappings) {
        List l = new ArrayList();
        Iterator it = mappedRoles.keySet().iterator();
        while (it.hasNext()) {
            MappedRole mr = new MappedRole();
            String s = (String) it.next();
            mr.filterName = s;
            mr.mappedRoles = mappedRoles.get(s).toString();
            //String out = "Role: " + mappedRoles.get(s).toString() + " -- ";
            String out = "";
            Iterator it2 = ((List) mappings.get(s)).iterator();
            while (it2.hasNext()) {
                String s1 = (String) it2.next();
                out += s1;
                if (it2.hasNext()) {
                    out += ", ";
                }
            }
            mr.mappedUrls = out;
            l.add(mr);
        }
        return l;
    }


    void prepMaps(Document doc, Map mappedRoles, Map mappings) {
        String s = AppnAccessControlFilter.class.getName();
        NodeList nl = doc.getElementsByTagName("filter");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            NodeList filterClass = ((Element) n).getElementsByTagName("filter-class");

            if (filterClass.item(0).getFirstChild().getNodeValue().equals(s)) {
                NodeList filterName = ((Element) n).getElementsByTagName("filter-name");
                String name = filterName.item(0).getFirstChild().getNodeValue();

                NodeList params = ((Element) n).getElementsByTagName("init-param");
                String appnName = null;
                String roles = null;
                for (int j = 0; j < params.getLength(); j++) {
                    String pName = ((Element) params.item(j)).getElementsByTagName("param-name").item(0).getFirstChild().getNodeValue();
                    String pValue = ((Element) params.item(j)).getElementsByTagName("param-value").item(0).getFirstChild().getNodeValue();
                    if (pName.equals("hbsAppnTypeCode")) {
                        appnName = pValue;
                    }
                    if (pName.equals("hbsAppnAccessRole")) {
                        roles = pValue;
                    }
                }
                String v = appnName + "::" + roles;
                mappedRoles.put(name, v);
                mappings.put(name, new ArrayList());
            }
        }
    }


    void loadUrls(Document doc, Map mappings) {
        NodeList nl = doc.getElementsByTagName("filter-mapping");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            String n = el.getElementsByTagName("filter-name").item(0).getFirstChild().getNodeValue();
            if (mappings.keySet().contains(n)) {
                List mappedUrls = (List) mappings.get(n);
                NodeList urls = el.getElementsByTagName("url-pattern");
                if (urls != null && urls.getLength() > 0) {
                    String v = urls.item(0).getFirstChild().getNodeValue();
                    mappedUrls.add(v);
                }

                NodeList servlets = el.getElementsByTagName("servlet-name");
                if (servlets != null && servlets.getLength() > 0) {
                    getServletMappings(doc, servlets.item(0).getFirstChild().getNodeValue(), mappedUrls);
                }
            }
        }
    }


    void getServletMappings(Document d, String servletName, List l) {
        NodeList nl = d.getElementsByTagName("servlet-mapping");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            String name = el.getElementsByTagName("servlet-name").item(0).getFirstChild().getNodeValue();
            if (name.equals(servletName)) {
                String v = el.getElementsByTagName("url-pattern").item(0).getFirstChild().getNodeValue();
                l.add(v);
            }
        }
    }

%>

</body>
</html>