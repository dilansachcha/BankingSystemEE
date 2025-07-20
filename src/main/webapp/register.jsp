<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<%
    response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
    response.setHeader("Pragma","no-cache");
    response.setDateHeader ("Expires", 0);
%>
<!DOCTYPE html>
<html>
<head>
    <title>Register - Banking System</title>
</head>
<body>
<h2>Register</h2>
<form method="post" action="register">
    Full Name: <input type="text" name="fullName" required/><br/>
    Email: <input type="email" name="email" required/><br/>
    NIC: <input type="text" name="nic"
                pattern="^(\d{9}[vVxX]|\d{12})$"
                title="NIC must be 9 digits followed by V/v/X/x or 12 digits." required/><br/>
    Mobile: <input type="text" name="mobile"
                   pattern="^(07[0-9]{8})$"
                   title="Mobile must be valid Sri Lankan number (e.g., 0771234567)" required/><br/>
    Password: <input type="password" id="password" name="password"
                     pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}$"
                     title="At least 8 characters with uppercase, lowercase, number, and special character."
                     required onpaste="return false;" oncopy="return false;" oncut="return false;">
    <input type="checkbox" onclick="togglePassword('password')"> Show Password<br/>

    Confirm Password: <input type="password" id="confirmPassword" name="confirmPassword"
                             required onpaste="return false;" oncopy="return false;" oncut="return false;">
    <input type="checkbox" onclick="togglePassword('confirmPassword')"> Show Password<br/>
    <button type="submit">Register</button>
</form>

<p>Already Registered? <a href="login.jsp">Login Here</a></p>

<% if (request.getParameter("error") != null) { %>
<p style="color:red;"><%= request.getParameter("error") %></p>
<% } %>

<script>
    function togglePassword(id) {
        var field = document.getElementById(id);
        field.type = (field.type === "password") ? "text" : "password";
    }
    document.getElementById("password").addEventListener('paste', e => e.preventDefault());
    document.getElementById("confirmPassword").addEventListener('paste', e => e.preventDefault());
</script>
</body>
</html>
