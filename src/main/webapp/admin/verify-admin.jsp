<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<h2>Admin Verification Code</h2>

<c:if test="${param.error == 'invalid'}">
  <p style="color:red;">Invalid OTP Code. Please try again.</p>
</c:if>

<form action="verify-admin" method="post">
  <input type="hidden" name="email" value="${param.email}" />
  <label>Enter OTP Code:</label><br/>
  <input type="text" name="otp" required/><br/><br/>
  <button type="submit">Verify</button>
</form>

