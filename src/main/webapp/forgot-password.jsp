<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<!DOCTYPE html>
<html>
<head><title>Forgot Password</title></head>
<body>
<h2>Forgot Password</h2>
<%
    String emailParam = request.getParameter("email");
    if (emailParam != null) {
        lk.fortyfourss.ejb.bankingsystemee.model.User user = (lk.fortyfourss.ejb.bankingsystemee.model.User) session.getAttribute("userService").findByEmail(emailParam);
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect("login.jsp?error=admin-reset-block");
            return;
        }
    }
%>

<form method="post" action="forgot-password">
    Enter your email: <input type="email" name="email" required/><br/>
    <button type="submit">Send Verification Code</button>
</form>

<% if (request.getParameter("success") != null) { %>
<p style="color:green;">Verification code sent to your email!</p>
<% } else if (request.getParameter("error") != null) { %>
<p style="color:red;">Error sending code. Please check your email.</p>
<% } %>
</body>
</html>
