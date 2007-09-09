<%@ page import="calculator.CalculatorService" %>
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
<%
%>
<html>
<head><title>Fabric3 Demo Web Client</title></head>
<body>
<h2>Calculator Web Application Client</h2>
<%
    double operand1 = Double.valueOf(request.getParameter("operand1"));
    double operand2 = Double.valueOf(request.getParameter("operand2"));
    double result;
    CalculatorService service =
            (CalculatorService) getServletConfig().getServletContext().getAttribute("calculatorReference");
    if ("add".equals(request.getParameter("operation"))) {
        result = service.add(operand1, operand2);
    } else if ("subtract".equals(request.getParameter("operation"))) {
        result = service.subtract(operand1, operand2);
    } else if ("multiply".equals(request.getParameter("operation"))) {
        result = service.multiply(operand1, operand2);
    } else if ("divide".equals(request.getParameter("operation"))) {
        result = service.divide(operand1, operand2);
    } else {
        throw new ServletException("Unknown operation type");
    }

%>
The result is: <%=result%>
</body>
</html>