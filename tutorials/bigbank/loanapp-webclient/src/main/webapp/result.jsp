<%--
 See the NOTICE file distributed with this work for information
 regarding copyright ownership.  This file is licensed
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%response.setHeader("Cache-Control", "no-cache"); %>
<html>
<head><title>Fabric3 Loan Application Demo</title></head>
<body>
<h3>Loan Application Results</h3>
<br>
<c:choose>
    <c:when test="${loanResult.result == -1}">
        After careful review, we are sorry to inform you that your loan application has been declined.
        <br>
        <ul>
            <c:forEach items="${loanResult.reasons}" var="reason">
                <li>
                    <c:out value="${reason}"></c:out>
                </li>
            </c:forEach>
        </ul>
    </c:when>
    <c:when test="${loanResult.result == 1}">
        <table>
            <tr>
                <td><strong>Type</strong></td>
                <td><strong>Rate</strong></td>
                <td><strong>APR</strong></td>
            </tr>
            <c:forEach items="${loanResult.options}" var="option">
                <tr>
                    <td>
                        <c:out value="${option.type}"></c:out>
                    </td>
                    <td>
                        <c:out value="${option.rate}"></c:out>
                    </td>
                    <td>
                        <c:out value="${option.apr}"></c:out>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:when>
</c:choose>
</body>
</html>