

<%@page import="javax.naming.InitialContext"%>
<%@page import="javax.sql.DataSource"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="org.w3c.dom.Document"%>
<%@page import="org.w3c.dom.NodeList"%>
<%@page import="org.w3c.dom.Element"%>
<%@page import="java.net.URL"%>
<%@page import="javax.xml.parsers.DocumentBuilderFactory"%>
<%@page import="javax.xml.parsers.DocumentBuilder"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.w3c.dom.Node"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="org.slf4j.LoggerFactory"%>
<html>
<body>
	<%
  Logger logger = LoggerFactory.getLogger("sqlLogger");
  boolean success = true;
  final String sql = "Select SYSDATE from DUAL";

  //-----------------------------------------------------------------------------------------------------------------------
  //Connect to JNDI
  //-----------------------------------------------------------------------------------------------------------------------
  InitialContext ctx = new InitialContext();

  //-----------------------------------------------------------------------------------------------------------------------
  //Iterate throught the datasources and execure select 1 from dual
  //-----------------------------------------------------------------------------------------------------------------------

  String prefix = "java:/comp/env/";
  List<String> dataSources = getJdbcResourceRefNames(pageContext);
  logger.info("Found JNDI datasources: {}", dataSources);
  DataSource ds;
  Connection conn = null;
  Statement stmt = null;
  ResultSet rs = null;
  for (String dsName : dataSources) {
    try {
      ds = (DataSource) ctx.lookup(prefix + dsName);
      conn = ds.getConnection();
      stmt = conn.createStatement();
      String q = sql;

      logger.info("Testing JNDI DataSource: {} using: {}", dsName, q);
      out.println("Tested JNDI " + dsName + " connection using " + q);
      rs = stmt.executeQuery(q);

      if (rs.next()) {
        logger.info("Successfully executed: {} on DatSource: {}", q, dsName);
        out.println(" - Success<hr>");
      } else {
        success = false;
        logger.warn("Test returned no rows using SQL: {} on DatSource: {}", q, dsName);
        out.println(" - Failed. No rows found.<hr>");
      }
    } catch (java.sql.SQLException ex) {
      logger.warn("SQLException while testing DatSource: {}", dsName, ex);
      success = false;
      out.println(" - Failed - " + ex.getMessage() + "<hr>");
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (Exception ex) {
        }
      }
      if (stmt != null) {
        try {
          stmt.close();
        } catch (Exception ex) {
        }
      }
      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ex) {
        }
      }
    }
  }
  if (dataSources.isEmpty()) {
    out.println("<br>No JNDI datasources configured in this Weblogic Server");
  } else if (success) {
    out.println("<br>connected to db using all pools configured in this Weblogic Server");
  }
%>

	<%!

  private List<String> getJdbcResourceRefNames(final PageContext page) throws Exception {
    List<String> list = new ArrayList<String>();
    Document webXmlDoc = getWebXmlDoc(page);
    NodeList nList = webXmlDoc.getElementsByTagName("resource-ref");
    for (int i = 0; i < nList.getLength(); i++) {
      Element e = (Element) nList.item(i);
      String type = e.getElementsByTagName("res-type").item(0).getFirstChild().getNodeValue();
      String value = e.getElementsByTagName("res-ref-name").item(0).getFirstChild().getNodeValue();
      if (type.equals("javax.sql.DataSource") && value.indexOf("jdbc/") > -1) {
        list.add(value);
      }
    }
    return list;
  }

  private Document getWebXmlDoc(PageContext page) throws Exception {
    URL url = page.getServletConfig().getServletContext().getResource("/WEB-INF/web.xml");
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputStream in = url.openStream();
    return builder.parse(in);
  }

%>
</body>
</html>


</html>
