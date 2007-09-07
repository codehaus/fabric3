<!--
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
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="tx.CatalogService" %>
<%@ page import="java.util.Iterator"%>
<html>
    <head><title>Fabric3 Demo Web Client</title></head>
    <body>
        <h2>Calculator Web Application Client</h2>

        <form action="catalog.jsp" method="post">
            <table>
                <tr>
                    <td>Product Name:</td>
                    <td><input type="text" name="product" width="5"></td>
                    <td align="right" colspan="3">
                        <button name="submit" type="submit">Calculate</button>
                    </td>
                </tr>
            </table>
        </form>
        <%
            String name = request.getParameter("product");
            CatalogService service = (CatalogService) application.getAttribute("catalogReference");
            if(name != null) {
                service.addProduct(name);
            }
            out.println("<table>");
            out.println("<tr><th>Name</th></tr>");
            Iterator products = service.getProducts().iterator();
            while(products.hasNext()) {
                out.println("<tr><td>" + products.next() + "</td></tr>");
            }
            out.println("</table>");
        %>
    </body>
</html>