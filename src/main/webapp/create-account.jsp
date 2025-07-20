<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
    response.setHeader("Pragma","no-cache");
    response.setDateHeader ("Expires", 0);
%>
<h2>Create a New Bank Account</h2>

<c:if test="${not empty error}">
    <p style="color:red; font-weight:bold;">${error}</p>
</c:if>
<c:if test="${not empty success}">
    <p style="color:green; font-weight:bold;">${success}</p>
</c:if>

<form method="post">
    <label>Account Type:</label><br/>
    <select name="accountType" required>
        <option value="SAVINGS">Savings</option>
        <option value="CHECKING">Checking</option>
        <option value="FIXED">Fixed Deposit</option>
    </select><br/>

    <div id="maturityPeriodSection" style="display:none;">
        <label>Maturity Period:</label><br/>
        <select name="maturityPeriod">
            <option value="6">6 Months</option>
            <option value="12">12 Months</option>
            <option value="24">24 Months</option>
        </select><br/><br/>
    </div>

    <label>Initial Deposit:</label><br/>
    <input type="number" name="initialDeposit" step="0.01" required/><br/><br/>

    <button type="submit">Create Account</button>
</form>

<script>
    document.querySelector('select[name="accountType"]').addEventListener('change', function() {
        document.getElementById('maturityPeriodSection').style.display = this.value === 'FIXED' ? 'block' : 'none';
    });
</script>

<a href="dashboard">&#x2B05; Back to Dashboard</a>
