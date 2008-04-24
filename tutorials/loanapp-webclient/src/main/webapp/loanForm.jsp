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
    <title>BigBank Loan Application Form</title>
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
</head>
<body>
<h2>BigBank Loan Application Form</h2>

<form action="LoanApplicationFormHandler" method="post">
    <table>
        <tr>
            <td><b>Applicant Information</b></td>
        </tr>
        <tr>
            <td>SSN</td>
            <td><input type="text" name="ssn" width="10"></td>
        </tr>
        <tr>
            <td>E-Mail</td>
            <td><input type="text" name="email" width="30"></td>
        </tr>
        <tr>
            <td>Amount</td>
            <td><input type="text" name="amount" width="10"></td>
        </tr>
        <tr>
            <td>Down</td>
            <td><input type="text" name="down" width="5"></td>
        </tr>
        <tr>
            <td><b>Property Information</b></td>
        </tr>
        <tr>
            <td>Street</td>
            <td><input type="text" name="street" width="20"></td>
        </tr>
        <tr>
            <td>City</td>
            <td><input type="text" name="city" width="20"></td>
        </tr>
        <tr>
            <td>Zip</td>
            <td><input type="text" name="zip" width="5"></td>
        </tr>

        <tr>
            <td align="right" colspan="3">
                <button name="submit" type="submit">Apply</button>
            </td>
        </tr>
    </table>
</form>
</body>
</html>