<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
    response.setHeader("Pragma","no-cache");
    response.setDateHeader ("Expires", 0);
%>
<head><title>Schedule a Transaction</title></head>
<form action="schedule-transaction" method="post">
    <c:choose>
        <c:when test="${fn:length(accounts) == 1}">
            From Account: <input type="text" name="fromAcc" value="${accounts[0].accountNumber}" readonly /><br/>
        </c:when>
        <c:otherwise>
            From Account:
            <select name="fromAcc" required>
                <c:forEach var="acc" items="${accounts}">
                    <option value="${acc.accountNumber}">
                            ${acc.accountNumber} (Balance: ${acc.balance})
                    </option>
                </c:forEach>
            </select><br/>
        </c:otherwise>
    </c:choose>
    <br/>
    To Account: <input name="toAcc" required><br/>
    Amount: <input name="amount" step="0.01" required><br/>
    Schedule Time: <input name="scheduledTime" type="datetime-local" required><br/>
    Recurring: <input type="checkbox" name="recurring"> <br/>
    Recurrence Type:
    <select name="recurrenceType">
        <option value="DAILY">Daily</option>
        <option value="WEEKLY">Weekly</option>
    </select><br/>
    <button type="submit">Schedule Transfer</button>
</form>

<c:if test="${param.error != null}">
    <p style="color:red; font-weight:bold;">${param.error}</p>
</c:if>
<c:if test="${param.success != null}">
    <p style="color:green; font-weight:bold;">${param.success}</p>
</c:if>


<a href="scheduled-transfers"> View Scheduled Transactions </a>


