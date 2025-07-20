<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<h2>Your Scheduled Transfers</h2>
<table border="1">
  <tr><th>From</th><th>To</th><th>Amount</th><th>Scheduled Time</th><th>Action</th></tr>
  <c:forEach var="st" items="${scheduledList}">
    <tr>
      <td>${st.fromAccount}</td>
      <td>${st.toAccount}</td>
      <td>${st.amount}</td>
      <td>${st.scheduledTime}</td>
      <td>${st.status}</td>
      <td>${st.retryCount}</td>
      <td>
        <form action="delete-scheduled-transfer" method="post">
          <input type="hidden" name="scheduledId" value="${st.id}" />
          <button type="submit">Cancel</button>
        </form>
      </td>
    </tr>
  </c:forEach>
</table>

