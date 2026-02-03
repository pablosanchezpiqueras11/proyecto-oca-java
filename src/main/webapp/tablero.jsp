<%@ page import="com.oca.modelo.Jugador" %>
<!DOCTYPE html>
<html>
<body>
    <h1>Tablero de Prueba (Backend)</h1>
    <h3 style="color:red;">${param.msg}</h3> <form action="JuegoServlet" method="post">
        <input type="hidden" name="accion" value="tirar">
        <button type="submit" style="padding: 20px; font-size: 20px;">🎲 TIRAR DADO</button>
    </form>
    
    <br>
    <a href="lobby">Volver al Lobby</a>
</body>
</html>