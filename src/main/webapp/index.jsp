<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="lk.fortyfourss.ejb.bankingsystemee.model.Account" %>
<%@ page import="lk.fortyfourss.ejb.bankingsystemee.model.User" %>

<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"CUSTOMER".equalsIgnoreCase(user.getRole())) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }
    List<Account> accounts = (List<Account>) request.getAttribute("accounts");
    List<Account> nonFixedAccounts = (List<Account>) request.getAttribute("nonFixedAccounts");
%>
<html>
<head><title>Customer Dashboard</title></head>
<body>

<%
    String success = (String) session.getAttribute("success");
    String error = (String) session.getAttribute("error");
    if (success != null) {
%>
<script>alert("<%= success %>");</script>
<%
        session.removeAttribute("success");
    }
    if (error != null) {
%>
<script>alert("<%= error %>");</script>
<%
        session.removeAttribute("error");
    }
%>

<h2>Welcome, <%= user.getFullName() %>!</h2>

<h3>Your Accounts:</h3>
<%
    if (accounts != null && !accounts.isEmpty()) {
%>
<ul>
    <% for (Account acc : accounts) { %>
    <li>
        Account No: <%= acc.getAccountNumber() %>,
        Type: <%= acc.getAccountType() %>,
        Status: <%= acc.getStatus() %>,
        Balance: <%= String.format("%.2f", acc.getBalance()) %>

        <% if ("FIXED".equalsIgnoreCase(acc.getAccountType()) && "ACTIVE".equalsIgnoreCase(acc.getStatus())) {
            if ("MATURED".equalsIgnoreCase(acc.getMaturityStatus())) { %>
        <button onclick="withdrawMaturedDeposit('<%= acc.getId() %>')">Withdraw Matured Deposit</button>
        <%      } else { %>
        <button onclick="openFixedClosure('<%= acc.getId() %>')">Close Fixed Deposit</button>
        <%      }
        } %>

    </li>
    <% } %>
</ul>
<%
} else {
%>
<p>No Active Accounts Found.</p>
<%
    }
%>
<c:if test="${not empty success}">
    <p style="color:green;">${success}</p>
</c:if>

<br/>
<a href="<%= request.getContextPath() %>/transfer">Make a Transfer</a> |
<a href="<%= request.getContextPath() %>/schedule-transaction">Schedule / Recurring Transfers</a> |
<a href="<%= request.getContextPath() %>/user-transactions-history">Your Transaction History</a> |
<a href="<%= request.getContextPath() %>/account-create">Create New Bank Account</a> |
<a href="<%= request.getContextPath() %>/logout">Logout</a>

<!-- ✅ Fixed Deposit Closure Modal -->
<div id="fixedClosurePopup" style="display:none; border:1px solid black; padding:15px; background:#eee;">
    <h3>Close Fixed Deposit</h3>
    <form method="post" action="close-fixed">
        <input type="hidden" id="fixedId" name="fixedId">
        <label>Select Target Account:</label><br/>
        <select name="targetId" id="targetSelect" required></select><br/><br/>
        <button type="submit">Confirm Closure</button>
        <button type="button" onclick="closePopup()">Cancel</button>
    </form>
</div>

<script>
    const nonFixedAccounts = [
        <% if (nonFixedAccounts != null) {
               for (Account a : nonFixedAccounts) {
                   if (!"FIXED".equalsIgnoreCase(a.getAccountType())) { %>
        {id: "<%= a.getId() %>", number: "<%= a.getAccountNumber() %>", type: "<%= a.getAccountType() %>"},
        <% }}} %>
    ];

    function openFixedClosure(fixedId) {
        if (nonFixedAccounts.length === 0) {
            alert("You have no non-fixed accounts. Please create a new account before closing your Fixed Deposit.");
            return;
        }
        alert("WARNING: Closing this Fixed Deposit will result in loss of all earned interest. Only the initial deposit will be refunded.");
        document.getElementById("fixedId").value = fixedId;
        const select = document.getElementById("targetSelect");
        select.innerHTML = "";
        nonFixedAccounts.forEach(acc => {
            const option = document.createElement("option");
            option.value = acc.id;
            option.text = acc.number + " (" + acc.type + ")";
            select.appendChild(option);
        });
        document.getElementById("fixedClosurePopup").style.display = 'block';
    }

    function closePopup() {
        document.getElementById("fixedClosurePopup").style.display = 'none';
    }
</script>

</body>
</html>
