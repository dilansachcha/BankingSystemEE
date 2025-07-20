<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<%@ page import="lk.fortyfourss.ejb.bankingsystemee.model.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=session");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <title>Admin Dashboard</title>
    <style>
        body { font-family: Arial; margin: 20px; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid black; padding: 10px; text-align: left; }
        th { background: #333; color: white; }
        h2 { color: #2c3e50; }
    </style>
</head>
<body>

<h2>Welcome - <%= user.getFullName() %></h2>

<h3>Pending User Approvals</h3>
<c:choose>
    <c:when test="${not empty pendingUsers}">
        <table>
            <tr><th>Email</th><th>Action</th></tr>
            <c:forEach var="u" items="${pendingUsers}">
                <tr>
                    <td>${u.email}</td>
                    <td>
                        <form action="action" method="post">
                            <input type="hidden" name="userId" value="${u.id}" />
                            <input type="hidden" name="action" value="activate" />
                            <button type="submit">Approve</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:when>
    <c:otherwise>
        <p>No pending users to approve.</p>
    </c:otherwise>
</c:choose>

<br><br>
<a href="../logout">Logout</a>

<script>
    const socket = new WebSocket("ws://localhost:8080/BankingSystemEE-1.0-SNAPSHOT/admin-notify");
    socket.onmessage = function(event) {
        alert("ADMIN ALERT: " + event.data);
    };
    socket.onclose = function() {
        console.warn("WebSocket Disconnected!");
    };
</script>

<a href="accounts">Manage Accounts (Block/Unblock)</a>
<a href="download-audit-report">📥 Download Audit Report (PDF)</a>

</body>
</html>
