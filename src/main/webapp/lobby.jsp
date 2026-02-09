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
        body { font-family: 'Segoe UI', sans-serif; padding: 20px; background-color: #f0f8ff; }
        .container { max-width: 900px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
        h1, h2, h3 { color: #2c3e50; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; margin-bottom: 20px; }
        th, td { padding: 12px; border-bottom: 1px solid #ddd; text-align: left; }
        th { background-color: #2980b9; color: white; }
        tr:hover { background-color: #f1f1f1; }
        
        .btn { padding: 8px 15px; color: white; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 0.9em; }
        .btn-crear { background: #27ae60; border: none; padding: 10px 20px; cursor: pointer; font-size: 1rem; }
        .btn-unirse { background: #e67e22; }
        .btn-jugar { background: #27ae60; }
        
        .crear-box { background: #ecf0f1; padding: 20px; margin-bottom: 30px; border-radius: 8px; display: flex; align-items: center; gap: 10px; }
        .crear-box input { padding: 10px; border: 1px solid #bdc3c7; border-radius: 4px; flex-grow: 1; }

        .header-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .user-menu a { margin-left: 15px; color: #7f8c8d; text-decoration: none; }
        .user-menu a:hover { color: #2980b9; }
    </style>
</head>
<body>

<div class="container">
    <%
        Jugador jugador = (Jugador) session.getAttribute("jugador");
        if (jugador == null) { response.sendRedirect("login.html"); return; }
    %>
    
    <div class="header-top">
        <h1>🦆 Lobby Oca</h1>
        <div class="user-menu">
            <span>Hola, <b><%= jugador.getNombre() %></b></span>
            <a href="perfil.jsp">👤 Mi Perfil</a>
            <a href="index.html">🚪 Salir</a>
            <a href="ranking.jsp" class="btn btn-warning btn-sm fw-bold text-dark" style="margin-right: 10px; text-decoration: none;">🏆 Ranking</a>
        </div>
    </div>

    <div class="crear-box">
        <form action="lobby" method="post" style="width: 100%; display:flex; gap:10px;">
            <input type="text" name="nombrePartida" placeholder="Nombre de la nueva sala..." required>
            <button type="submit" class="btn btn-crear">➕ Crear Partida</button>
        </form>
    </div>

    <h3>▶️ Mis Partidas Activas</h3>
    <%
        List<Partida> misPartidas = (List<Partida>) request.getAttribute("misPartidas");
        if (misPartidas != null && !misPartidas.isEmpty()) {
    %>
        <table>
            <thead>
                <tr>
                    <th>Sala</th>
                    <th>Estado</th>
                    <th>Jugadores</th>
                    <th>Acción</th>
                    <th>Administrar</th>
                </tr>
            </thead>
            <tbody>
              <% for (Partida p : misPartidas) { %>
                <tr>
                    <td><%= p.getNombre() %></td>
                    <td>
                        <span style="<%= p.getEstado().equals("JUGANDO") ? "color: green; font-weight: bold;" : "color: orange; font-weight: bold;" %>">
                            <%= p.getEstado() %>
                        </span>
                    </td>
                    <td><%= p.getJugadoresActuales() %>/4</td>
                    <td>
                        <a href="unirse?idPartida=<%= p.getId() %>" class="btn btn-jugar">ENTRAR 🎲</a>
                    </td>
                    <td>
                        <form action="lobby" method="post" style="margin: 0;">
                            <input type="hidden" name="accion" value="eliminar">
                            <input type="hidden" name="idPartida" value="<%= p.getId() %>">
                            <button type="submit" 
                                    style="background-color: #e74c3c; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer;"
                                    onclick="return confirm('¿Seguro que quieres borrar la partida \'<%= p.getNombre() %>\'? Se eliminará para todos.');">
                                🗑️ Borrar
                            </button>
                        </form>
                   </td>
                </tr>
                <% } %>
            </tbody>
        </table>
    <% } else { %>
        <p><i>No estás jugando ninguna partida actualmente.</i></p>
    <% } %>

    <hr style="margin: 30px 0; border: 0; border-top: 1px solid #eee;">

    <h3>🔍 Partidas Disponibles</h3>
    <%
        List<Partida> disponibles = (List<Partida>) request.getAttribute("partidasDisponibles");
        if (disponibles != null && !disponibles.isEmpty()) {
    %>
        <table>
            <thead>
                <tr>
                    <th>Sala</th>
                    <th>Estado</th>
                    <th>Huecos</th>
                    <th>Acción</th>
                </tr>
            </thead>
            <tbody>
                <% for (Partida p : disponibles) { %>
                <tr>
                    <td><%= p.getNombre() %></td>
                    <td><%= p.getEstado() %></td>
                    <td><%= p.getJugadoresActuales() %>/4</td>
                    <td>
                        <a href="unirse?idPartida=<%= p.getId() %>" class="btn btn-unirse">Unirse 👋</a>
                    </td>
                </tr>
                <% } %>
            </tbody>
        </table>
    <% } else { %>
        <p><i>No hay partidas nuevas disponibles. ¡Crea una tú mismo!</i></p>
    <% } %>

</div>

<script>
    // Configuración: El lobby se actualizará cada 5 segundos
    const INTERVALO_REFRESCO = 5000; 

    setInterval(function() {
        // Localizamos el cuadro de texto donde escribes el nombre de la partida
        const inputNombre = document.querySelector('input[name="nombrePartida"]');
        
        // Solo recarga la página si el usuario NO está escribiendo nada
        if (inputNombre && inputNombre.value.trim() === "") {
            window.location.reload();
        }
    }, INTERVALO_REFRESCO);
</script>

</body>
</html>