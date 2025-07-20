<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<h3>Admin: Manage Accounts with User Details</h3>

<table>
    <tr>
        <th>ID</th><th>Account No.</th><th>Type</th><th>Status</th><th>Balance</th>
        <th>User Name</th><th>Email</th><th>Action</th>
    </tr>
    <c:forEach var="a" items="${accounts}">
        <tr>
            <td>${a.id}</td>
            <td>${a.accountNumber}</td>
            <td>${a.accountType}</td>
            <td>${a.status}</td>
            <td>${a.balance}</td>
            <td>${a.user.fullName}</td>
            <td>${a.user.email}</td>
            <td>
                <form action="accounts" method="post">
                    <input type="hidden" name="accountId" value="${a.id}" />
                    <c:choose>
                        <c:when test="${a.status == 'ACTIVE'}">
                            <input type="hidden" name="action" value="block"/>
                            Reason: <input type="text" name="reason" required />
                            <button type="submit">Block</button>
                        </c:when>
                        <c:otherwise>
                            <input type="hidden" name="action" value="unblock"/>
                            <button type="submit">Unblock</button>
                        </c:otherwise>
                    </c:choose>
                </form>
            </td>
        </tr>
    </c:forEach>
</table>

<br><a href="dashboard">⬅️ Back to Dashboard</a>
