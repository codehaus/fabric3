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
<html>
<head>
    <title>BigBank Loan Terms Review Form</title>
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
</head>
<body>
<h2>BigBank Loan Terms Review Form</h2>

<form action="LoanAcceptanceFormHandler" method="post">
    <table>
        <tr>
            <td>ID</td>
            <td><input type="text" name="loanId" width="10"></td>
        </tr>
        <tr>
            <td><input type="radio" name="acceptLoan" value="true" title="Accept">Accept</td>
            <td><input type="radio" name="acceptLoan" value="false" title="Decline">Decline</td>
        </tr>

        <tr>
            <td align="right" colspan="3">
                <button name="submit" type="submit">Submit</button>
            </td>
        </tr>
    </table>
</form>
</body>
</html>