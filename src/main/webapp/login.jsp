<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<!DOCTYPE html>
<html>
<head>
    <title>Login - Banking System</title>
    <style>
        .popup-overlay {
            display: none;
            position: fixed;
            top: 0; left: 0;
            width: 100%; height: 100%;
            background: rgba(0,0,0,0.5);
            z-index: 999;
        }
        .popup {
            display: none;
            position: fixed;
            top: 25%;
            left: 35%;
            background: #fff;
            padding: 20px;
            border: 2px solid #000;
            box-shadow: 0 0 15px rgba(0,0,0,0.4);
            z-index: 1000;
        }
        button { margin-top: 5px; }
        .error-message { color: red; display: block; }
        .success-message { color: green; }
    </style>
</head>
<body>

<!-- ✅ Error Messages -->
<c:if test="${param.error == 'invalid'}">
    <p class="error-message">Invalid Credentials or Inactive User!</p>
</c:if>
<c:if test="${param.error == 'admin-reset-block'}">
    <p class="error-message">Admin password resets from here are disabled. Contact support.</p>
</c:if>
<c:if test="${param.error == 'session'}">
    <p class="error-message">Session expired or unauthorized access! Please login again.</p>
</c:if>

<!-- ✅ Success Messages -->
<c:if test="${param.success == 'reset'}">
    <p class="success-message">Password Reset Successfully!</p>
</c:if>
<c:if test="${param.success == 'registered'}">
    <p class="success-message">Registration Successful! Please login.</p>
</c:if>

<h2>Login</h2>
<form method="post" action="login">
    Email: <input type="email" name="email" required/><br/>
    Password: <input type="password" name="password" required/><br/>
    <button type="submit">Login</button>
</form>

<a href="#" onclick="openEmailPopup()">Forgot Password?</a>

<div id="overlay" class="popup-overlay"></div>

<div id="emailPopup" class="popup">
    <h3>Forgot Password</h3>
    <c:if test="${param.error == 'admin-reset-block'}">
        <p style="color:red;">Admin password resets are blocked. Contact IT support.</p>
    </c:if>
    <form method="post" action="forgot-password">
        Enter your Email:<br/>
        <input type="email" name="email" required/><br/><br/>
        <button type="submit">Send Verification Code</button>
        <button type="button" onclick="closePopups()">Cancel</button>
    </form>
</div>

<div id="resetPopup" class="popup">
    <h3>Reset Password</h3>
    <form method="post" action="reset-password" onsubmit="return validateResetForm()">
        <input type="hidden" name="email" id="resetEmail">
        Verification Code:<br/>
        <input type="text" name="code" required/><br/><br/>

        New Password:<br/>
        <input type="password" name="newPassword" id="newPassword" required/>
        <input type="checkbox" onclick="togglePasswordVisibility('newPassword')"> Show Password<br/><br/>

        Confirm Password:<br/>
        <input type="password" id="confirmPassword" required/>
        <input type="checkbox" onclick="togglePasswordVisibility('confirmPassword')"> Show Password<br/>
        <span id="password-error" class="error-message">Passwords do not match!</span><br/><br/>

        <button type="submit">Reset Password</button>
        <button type="button" onclick="closePopups()">Cancel</button>
    </form>
</div>

<script>
    function openEmailPopup() {
        document.getElementById('overlay').style.display = 'block';
        document.getElementById('emailPopup').style.display = 'block';
    }

    function openResetPopup(email) {
        document.getElementById('overlay').style.display = 'block';
        document.getElementById('resetPopup').style.display = 'block';
        document.getElementById('resetEmail').value = email;
    }

    function closePopups() {
        document.getElementById('overlay').style.display = 'none';
        document.getElementById('emailPopup').style.display = 'none';
        document.getElementById('resetPopup').style.display = 'none';
        document.getElementById('password-error').style.display = 'none';
    }

    function togglePasswordVisibility(fieldId) {
        const field = document.getElementById(fieldId);
        field.type = field.type === "password" ? "text" : "password";
    }

    function validateResetForm() {
        const pass = document.getElementById('newPassword').value;
        const confirm = document.getElementById('confirmPassword').value;
        const errorMsg = document.getElementById('password-error');

        if (pass !== confirm) {
            errorMsg.style.display = 'block';
            return false;
        } else {
            errorMsg.style.display = 'none';
            return true;
        }
    }

    <% if (request.getParameter("showReset") != null) { %>
    window.onload = function() {
        closePopups();
        openResetPopup("<%= request.getParameter("showReset") %>");
    }
    <% } %>

</script>

<a href="<%= request.getContextPath() %>/register"> New User? Go to Register</a>
</body>
</html>
