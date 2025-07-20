<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<%@ page import="lk.fortyfourss.ejb.bankingsystemee.model.Account" %>
<%@ page import="java.util.List" %>
<%
    response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
    response.setHeader("Pragma","no-cache");
    response.setDateHeader ("Expires", 0);
%>

<%
    List<Account> accounts = (List<Account>) request.getAttribute("accounts");
    if (accounts == null) {
        response.sendRedirect("transfer");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Manual Fund Transfer</title>
</head>
<body>
<h1>Manual Fund Transfer</h1>

<form action="transfer" method="post">
    <label>From Account Number:</label><br/>
    <select name="fromAcc">
        <% for (Account acc : accounts) { %>
        <option value="<%= acc.getAccountNumber() %>"><%= acc.getAccountNumber() %> (Balance: <%= String.format("%.2f", acc.getBalance()) %>)</option>
        <% } %>
    </select><br/>

    <label>To Account Number:</label><br/>
    <input type="text" name="toAcc" required/><br/>

    <label>Amount:</label><br/>
    <input type="number" step="0.01" name="amount" required/><br/><br/>

    <input type="submit" value="Transfer Funds"/>
</form>

<c:if test="${param.error != null}">
    <h3 style="color:red; font-weight:bold;">${param.error}</h3>
</c:if>
<c:if test="${param.success != null}">
    <h3 style="color:green; font-weight:bold;">${param.success}</h3>
</c:if>

<a href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a> |
</body>
</html>
