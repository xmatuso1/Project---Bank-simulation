<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<body>
<table border="1">
    <thead>
       
    <tr>
        <th>meno</th>
        <th>datum narodenia</th>
        
    </tr>
    </thead>
    <c:forEach items="${persons}" var="person">
        <tr>
            <td><c:out value="${person.name}"/></td>
            <td><c:out value="${person.born}"/></td>
            <td><form method="post" action="${pageContext.request.contextPath}/persons/delete?id=${person.id}"
                      style="margin-bottom: 0;"><input type="submit" value="Smazat"></form></td>
        </tr>
    </c:forEach>
</table>
<h2>Zadejte klienta</h2>
<c:if test="${not empty chyba}">
    <div style="border: solid 1px red; background-color: blue; padding: 15px">
        <c:out value="${chyba}"/>
    </div>
</c:if>
<form action="${pageContext.request.contextPath}/persons/add" method="post">
    <table>
        <tr>
            <th>Meno:</th>
            <td><input type="text" name="name" value="<c:out value='${param.name}'/>"/></td>
        </tr>
        <tr>
            <th>Dátum narodenia:</th>
            <td>
                <input type="text" name="born" value="<c:out value='${param.born}'/>"/>
            </td>
        </tr>
    </table>
    <input type="Submit" value="Zadat" />
</form>



</body>
</html>