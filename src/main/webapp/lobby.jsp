<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.oca.modelo.Partida" %>
<%@ page import="com.oca.modelo.Jugador" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Sala de Espera (Lobby)</title>
    <style>
        body { font-family: sans-serif; padding: 20px; background-color: #f0f8ff; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
        h2 { color: #333; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 10px; border: 1px solid #ddd; text-align: left; }
        th { background-color: #007bff; color: white; }
        .btn { padding: 5px 10px; background: #28a745; color: white; text-decoration: none; border-radius: 4px; }
        .crear-box { background: #eee; padding: 15px; margin-bottom: 20px; border-radius: 5px; }
    </style>
</head>
<body>

<div class="container">
    <%
        Jugador jugador = (Jugador) session.getAttribute("jugador");
        if (jugador != null) {
    %>
        <h1>Hola, <%= jugador.getNombre() %> 👋</h1>
    <% } else { %>
        <h1>Hola, Invitado</h1>
    <% } %>

    <div class="crear-box">
        <h3>Crear Nueva Partida</h3>
        <form action="lobby" method="post">
            <input type="text" name="nombrePartida" placeholder="Nombre de la sala (ej: Mesa 1)" required>
            <button type="submit">Crear Partida</button>
        </form>
    </div>

    <hr>

    <h3>Partidas Disponibles</h3>
    
    <%
        // Recuperamos la lista que nos envió el Servlet
        List<Partida> lista = (List<Partida>) request.getAttribute("listaPartidas");
        
        if (lista != null && !lista.isEmpty()) {
    %>
        <table>
            <thead>
                <tr>
                    <th>Nombre de la Sala</th>
                    <th>Estado</th>
                    <th>Acción</th>
                </tr>
            </thead>
            <tbody>
                <% for (Partida p : lista) { %>
                <tr>
                    <td><%= p.getNombre() %></td>
                    <td><%= p.getEstado() %></td>
                    <td>
                        <a href="unirse?idPartida=<%= p.getId() %>" class="btn">Unirse</a>
                    </td>
                </tr>
                <% } %>
            </tbody>
        </table>
    <% } else { %>
        <p><i>No hay partidas creadas. ¡Sé el primero en crear una!</i></p>
    <% } %>

    <br>
    <a href="perfil.jsp">Ver mi Perfil</a> | <a href="index.html">Cerrar Sesión</a>
</div>

</body>
</html>