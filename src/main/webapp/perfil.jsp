<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.oca.modelo.Jugador" %>
<%
    Jugador jugador = (Jugador) session.getAttribute("jugador");
    if (jugador == null) {
        response.sendRedirect("login.html");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Mi Perfil - La Oca</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background-color: #2c3e50; color: white; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        .container { background-color: #34495e; padding: 40px; border-radius: 15px; width: 400px; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }
        h2 { color: #f1c40f; text-align: center; }
        .info-box { background: rgba(255,255,255,0.1); padding: 15px; border-radius: 5px; margin-bottom: 20px; }
        label { display: block; margin-top: 10px; font-weight: bold; }
        input[type="password"] { width: 100%; padding: 10px; margin-top: 5px; border: none; border-radius: 4px; box-sizing: border-box; }
        button { width: 100%; padding: 12px; background: #27ae60; color: white; border: none; border-radius: 4px; margin-top: 20px; cursor: pointer; font-size: 16px; font-weight: bold; }
        button:hover { background: #2ecc71; }
        .alert { padding: 10px; margin-bottom: 15px; border-radius: 4px; text-align: center; color: #fff; font-weight: bold; }
        .error { background-color: #c0392b; }
        .ok { background-color: #27ae60; }
        a { display: block; text-align: center; margin-top: 20px; color: #3498db; text-decoration: none; }
    </style>
</head>
<body>

<div class="container">
    <h2>👤 Mi Perfil</h2>

    <% 
    String msg = request.getParameter("mensaje");
    if ("exito".equals(msg)) { %>
        <div class="alert ok">✅ ¡Contraseña actualizada!</div>
    <% } else if ("noCoinciden".equals(msg)) { %>
        <div class="alert error">❌ Las contraseñas nuevas no coinciden.</div>
    <% } else if ("passIncorrecta".equals(msg)) { %>
        <div class="alert error">❌ La contraseña actual no es correcta.</div>
    <% } %>

    <div class="info-box">
        <p><strong>Usuario:</strong> <%= jugador.getNombre() %></p>
        <p><strong>Partidas Ganadas:</strong> <%= jugador.getPartidasGanadas() %> 🏆</p>
    </div>

    <form action="PerfilServlet" method="post">
        <label>Contraseña Actual:</label>
        <input type="password" name="passActual" required>

        <label>Nueva Contraseña:</label>
        <input type="password" name="passNueva" required>

        <label>Repetir Nueva Contraseña:</label>
        <input type="password" name="passRepetida" required>

        <button type="submit">Actualizar Contraseña</button>
    </form>

    <a href="lobby">← Volver al Lobby</a>
</div>

</body>
</html>