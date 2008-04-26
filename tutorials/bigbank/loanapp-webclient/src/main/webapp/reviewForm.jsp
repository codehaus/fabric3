<!--
See the NOTICE file distributed with this work for information
regarding copyright ownership. This file is licensed
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License.
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%response.setHeader("Cache-Control", "no-cache"); %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>BigBank Loan Terms Review Form</title>
</head>
<body>
<h1><font face="arial" color="darkblue">Big</font><font face="arial" color="gray">Bank</font><font size="4" face="arial"
                                                                                                   color="darkblue">
    Lending</font></h1>

<h3><font face="arial"> Loan Terms Review</font></h3>
Please select a loan option:
<br>
<br>

<form action="LoanAcceptanceFormHandler" method="post">
    <table>
        <c:forEach items="${loanTerms.options}" var="option">
            <tr>
                <td><input type="radio" name="acceptLoan" value="${option.type}" title="${option.type}">${option.type}
                    at ${option.rate}% and ${option.apr} APR
                </td>
            </tr>
        </c:forEach>
        <tr>
            <td><input type="radio" name="acceptLoan" value="decline" title="Decline">Decline the loan</td>
        </tr>

        <tr>
            <td align="right" colspan="3">
                <button name="submit" type="submit">Submit</button>
            </td>
        </tr>
    </table>
</form>
<font face="arial" size="2"><i>Powered by <a href="http://www.fabric3.org">fabric3</a></i></font>
</body>
</html>