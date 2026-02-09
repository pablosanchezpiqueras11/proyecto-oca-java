<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.oca.dao.PartidaDAO" %>
<%@ page import="java.util.List" %>

<%
    // 1. Pedimos los datos al DAO
    PartidaDAO dao = new PartidaDAO();
    List<String[]> ranking = dao.obtenerRanking();
%>

<!doctype html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Ranking de Campeones - La Oca</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <style>
      body { background-color: #2c3e50; color: white; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
      .ranking-card { background: white; color: #333; border-radius: 15px; overflow: hidden; }
      .table-hover tbody tr:hover { background-color: #f1f1f1; }
      .medal { font-size: 1.5rem; }
  </style>
</head>
<body>

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            
            <div class="text-center mb-4">
                <h1 class="display-4 fw-bold text-warning">🏆 Salón de la Fama</h1>
                <p class="lead text-light">Los mejores jugadores de la Oca</p>
            </div>

            <div class="card ranking-card shadow-lg border-0">
                <div class="card-body p-0">
                    <table class="table table-hover table-striped mb-0 text-center">
                        <thead class="bg-primary text-white">
                            <tr>
                                <th scope="col" class="py-3">Posición</th>
                                <th scope="col" class="py-3 text-start ps-5">Jugador</th>
                                <th scope="col" class="py-3">Victorias</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% 
                            int pos = 1;
                            if (ranking.isEmpty()) { 
                            %>
                                <tr>
                                    <td colspan="3" class="py-4 text-muted">Aún no hay partidas terminadas... ¡Sé el primero!</td>
                                </tr>
                            <% 
                            } else {
                                for (String[] fila : ranking) { 
                                    String nombre = fila[0];
                                    String victorias = fila[1];
                                    
                                    // Asignar medallas
                                    String medalla = "";
                                    if (pos == 1) medalla = "🥇";
                                    else if (pos == 2) medalla = "🥈";
                                    else if (pos == 3) medalla = "🥉";
                                    else medalla = String.valueOf(pos);
                            %>
                                <tr class="<%= (pos==1) ? "table-warning fw-bold" : "" %>">
                                    <td class="align-middle fs-5"><%= medalla %></td>
                                    <td class="align-middle text-start ps-5">
                                        <span class="fw-bold"><%= nombre %></span>
                                    </td>
                                    <td class="align-middle">
                                        <span class="badge bg-success rounded-pill px-3"><%= victorias %> 🏆</span>
                                    </td>
                                </tr>
                            <% 
                                    pos++;
                                } 
                            }
                            %>
                        </tbody>
                    </table>
                </div>
                <div class="card-footer bg-light text-center p-3">
                    <a href="lobby" class="btn btn-outline-primary fw-bold">🏠 Volver al Menú Principal</a>
                </div>
            </div>

        </div>
    </div>
</div>

</body>
</html>