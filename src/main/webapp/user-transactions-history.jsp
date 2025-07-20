<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<h2>Your Transaction History</h2>

<table border="1">
    <tr>
        <th>ID</th>
        <th>Account</th>
        <th>Type</th>
        <th>Amount</th>
        <th>Description</th>
        <th>Time</th>
        <th>Receipt</th>
    </tr>

    <c:forEach var="t" items="${transactions}">
        <tr>
            <td>${t.id}</td>
            <td>${t.account.accountNumber}</td>
            <td>${t.transactionType}</td>
            <td>${t.amount}</td>
            <td>${t.description}</td>
            <td>${t.transactionTime}</td>
            <td>
                <c:if test="${t.transactionType == 'DEBIT'}">
                    <form method="get" action="download-receipt">
                        <input type="hidden" name="transactionId" value="${t.id}">
                        <button type="submit">Download Receipt</button>
                    </form>
                </c:if>
            </td>
        </tr>
    </c:forEach>
</table>

<a href="dashboard">Back to Dashboard</a>
