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
    <title>BigBank Loan Review Search</title>
</head>
<body>
<h1><font face="arial" color="darkblue">Big</font><font face="arial" color="gray">Bank</font><font size="4" face="arial"
                                                                                                   color="darkblue">
    Lending</font></h1>

<h3><font face="arial"> Loan Search</font></h3>
<br>

<form action="LoanSearchFormHandler" method="post">
    <table>
        <tr>
            <td>Enter the loan id &nbsp;</td>
            <td><input type="text" name="loanId" width="10"></td>
        </tr>
        <tr>
            <td align="right" colspan="3">
                <button name="submit" type="submit">Submit</button>
            </td>
        </tr>
    </table>
<br>
<font face="arial" size="2"><i>Powered by <a href="http://www.fabric3.org">fabric3</a></i></font>

</form>
</body>
</html>