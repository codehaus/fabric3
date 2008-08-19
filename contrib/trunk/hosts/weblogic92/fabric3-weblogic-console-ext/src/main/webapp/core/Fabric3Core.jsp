<%@ page language="java" contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table width=100% border=0 cellpadding=0 cellspacing=6 class="contenttable">

 <c:if test="${!empty subDomains}"> 
   <tr>
 <c:forEach var="subdomain" items="${subDomains}">
      <c:if test="${selectedSubDomain == subdomain}">
      <td width="20"><c:out value="${subdomain}"/></td>
      </c:if>
      <c:if test="${selectedSubDomain != subdomain}">
      <td width="20"><a href='?_nfpb=true&_pageLabel=Fabric3Core&fabric3Corehandle=com.bea.console.handles.JMXHandle%28%22com.bea%3AName%3Df3_local_domain%2CType%3DDomain%22%29'><c:out value="${subdomain}"/></a></td>
      </c:if>
</c:forEach>
  </tr>
  <tr>
 <c:forEach var="component" items="${components}">
      <c:if test="${selectedComponent == component}">
      <td width="20"><c:out value="${component}"/></td>
      </c:if>
      <c:if test="${selectedComponent != component}">
      <td width="20"><a href='?_nfpb=true&_pageLabel=Fabric3Core&fabric3Corehandle=com.bea.console.handles.JMXHandle%28%22com.bea%3AName%3Df3_local_domain%2CType%3DDomain%22%29'><c:out value="${component}"/></a></td>
      </c:if>
</c:forEach>
</tr>
</c:if>
<c:if test="${empty subDomains}">
  <tr>
  <td>There are no fabric3 runtimes configured. </td>
  </tr>
</c:if>
  </tr> 
</table>