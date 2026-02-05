<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.oca.dao.PartidaDAO" %>
<%@ page import="com.oca.modelo.PartidaJugador" %>
<%@ page import="com.oca.modelo.Partida" %>

<%
    Integer idPartida = (Integer) session.getAttribute("idPartida");
    List<PartidaJugador> posiciones = null;
    Partida partidaActual = null;
    String nombreTurno = "Nadíe";
    
    if (idPartida != null) {
        PartidaDAO dao = new PartidaDAO();
        posiciones = dao.obtenerPosiciones(idPartida);
        partidaActual = dao.obtenerPartida(idPartida);
        
        // Calcular de quién es el turno buscando el ID en la lista
        if (partidaActual != null && posiciones != null) {
            for (PartidaJugador pj : posiciones) {
                if (pj.getIdJugador() == partidaActual.getIdTurnoActual()) {
                    nombreTurno = pj.getNombre();
                }
            }
        }
    }
    
    // Recuperar el dado de la URL (si existe)
    String dadoParam = request.getParameter("dado");
    int dadoValor = (dadoParam != null) ? Integer.parseInt(dadoParam) : 0;
%>
<!doctype html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <meta http-equiv="refresh" content="5"> <title>Tablero Oca</title>
  <style>
    :root{ --header-h: 72px; }
    body{ margin:0; font-family:Segoe UI,Tahoma,Geneva,Verdana,sans-serif; background:#2c3e50; color:#fff; overflow:hidden; }

    /* HEADER */
    header{
      height: var(--header-h); background:#22313f; padding:0 20px; display:flex; justify-content:space-between; align-items:center;
    }
    header h1{ margin:0; color:#f1c40f; font-size:1.2rem; display: flex; align-items: center; gap: 10px;}
    
    /* PANELES DE INFORMACIÓN */
    .info-panel { display: flex; align-items: center; gap: 20px; }
    .turno-box { background: #34495e; padding: 5px 15px; border-radius: 5px; border: 1px solid #7f8c8d; }
    .turno-box span { color: #f1c40f; font-weight: bold; text-transform: uppercase; }

    /* LEYENDA JUGADORES */
    .leyenda { display: flex; gap: 15px; font-size: 0.9rem; }
    .jugador-item { display: flex; align-items: center; gap: 5px; }
    .dot { width: 12px; height: 12px; border-radius: 50%; display: inline-block; border: 1px solid white;}
    
    /* COLORES FICHAS */
    .c1 { background: #27ae60; fill: #27ae60; } /* P1 Verde */
    .c2 { background: #2980b9; fill: #2980b9; } /* P2 Azul */
    .c3 { background: #c0392b; fill: #c0392b; } /* P3 Rojo */
    .c4 { background: #f39c12; fill: #f39c12; } /* P4 Amarillo */

    /* STAGE Y TABLERO */
    .stage{ height: calc(100vh - var(--header-h)); display:flex; align-items:center; justify-content:center; padding:12px; box-sizing:border-box; }
    .board-wrap{ position:relative; width: min(95vw, calc(100vh - var(--header-h) - 24px)); }
    .board-img{ width:100%; height:auto; display:block; border-radius:16px; box-shadow:0 10px 25px rgba(0,0,0,.25); }
    .overlay{ position:absolute; top:0; left:0; width:100%; height:100%; border-radius:16px; }
    
    .token{ stroke:white; stroke-width:2; transition: all 0.5s ease-out; }
    
    /* DADO FLOTANTE */
    .dado-animado {
        position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
        background: white; color: #2c3e50;
        width: 100px; height: 100px;
        display: flex; justify-content: center; align-items: center;
        font-size: 3rem; font-weight: bold;
        border-radius: 15px; border: 4px solid #f1c40f;
        box-shadow: 0 0 50px rgba(0,0,0,0.8);
        z-index: 200;
        animation: popIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }
    @keyframes popIn { 0%{transform:translate(-50%,-50%) scale(0);} 100%{transform:translate(-50%,-50%) scale(1);} }

  </style>
</head>
<body>

<header>
    <h1>🦆 Oca</h1>
    
    <div class="leyenda">
        <% if (posiciones != null) {
            for (PartidaJugador pj : posiciones) { %>
            <div class="jugador-item">
                <span class="dot c<%= pj.getOrden() %>"></span> 
                <%= pj.getNombre() %>
            </div>
        <%  } 
           } %>
    </div>

    <div class="info-panel">
        <div class="turno-box">Turno de: <span><%= nombreTurno %></span></div>
        <a href="lobby" style="color:#bdc3c7; text-decoration:none; font-size:0.8rem;">Salir ✖</a>
    </div>
</header>

<div class="stage">
    <div class="board-wrap">
      <img class="board-img" src="assets/tablero.jpg" alt="Tablero Oca" />
      <svg class="overlay" id="overlay" viewBox="0 0 1000 1000" preserveAspectRatio="none">
         </svg>
    </div>
</div>

<% if (request.getParameter("msg") != null) { %>
    <div style="position: fixed; bottom: 20px; left: 20px; background: rgba(0,0,0,0.8); padding: 15px; border-radius: 8px; max-width: 300px; border-left: 5px solid #f1c40f;">
        <%= request.getParameter("msg") %>
    </div>
<% } %>

<% if (dadoValor > 0) { %>
    <div class="dado-animado" id="dadoVisual">
        <%= dadoValor %>
    </div>
    <script>
        // Ocultar el dado automáticamente a los 3 segundos
        setTimeout(() => {
            const d = document.getElementById("dadoVisual");
            if(d) d.style.display = 'none';
        }, 3000);
    </script>
<% } %>

<form action="JuegoServlet" method="post" style="position: fixed; bottom: 20px; right: 20px; z-index: 100;">
    <input type="hidden" name="accion" value="tirar">
    <button type="submit" style="
        padding: 20px 40px; 
        font-size: 24px; 
        background: #e67e22; 
        color: white; 
        border: 4px solid white; 
        border-radius: 50px; 
        cursor: pointer; 
        box-shadow: 0 10px 20px rgba(0,0,0,0.5);">
        🎲 TIRAR
    </button>
</form>

<script>
    // MAPA DE COORDENADAS (El mismo que tenías)
    const posMap = {
      "1": {"cx":172,"cy":885}, "2": {"cx":374,"cy":885}, "3": {"cx":457,"cy":885}, "4": {"cx":535,"cy":885},
      "5": {"cx":619,"cy":885}, "6": {"cx":699,"cy":885}, "7": {"cx":777,"cy":885}, "8": {"cx":851,"cy":885},
      "9": {"cx":895,"cy":835}, "10":{"cx":895,"cy":758}, "11":{"cx":895,"cy":685}, "12":{"cx":895,"cy":604},
      "13":{"cx":895,"cy":523}, "14":{"cx":895,"cy":443}, "15":{"cx":895,"cy":367}, "16":{"cx":895,"cy":290},
      "17":{"cx":895,"cy":217}, "18":{"cx":895,"cy":136}, "19":{"cx":836,"cy":96},  "20":{"cx":760,"cy":96},
      "21":{"cx":691,"cy":96},  "22":{"cx":610,"cy":96},  "23":{"cx":531,"cy":96},  "24":{"cx":452,"cy":96},
      "25":{"cx":374,"cy":96},  "26":{"cx":296,"cy":96},  "27":{"cx":223,"cy":96},  "28":{"cx":153,"cy":96},
      "29":{"cx":92,"cy":145},  "30":{"cx":92,"cy":224},  "31":{"cx":92,"cy":296},  "32":{"cx":92,"cy":372},
      "33":{"cx":92,"cy":450},  "34":{"cx":92,"cy":527},  "35":{"cx":92,"cy":602},  "36":{"cx":92,"cy":679},
      "37":{"cx":139,"cy":733}, "38":{"cx":221,"cy":733}, "39":{"cx":294,"cy":733}, "40":{"cx":374,"cy":733},
      "41":{"cx":454,"cy":733}, "42":{"cx":535,"cy":733}, "43":{"cx":612,"cy":733}, "44":{"cx":687,"cy":733},
      "45":{"cx":736,"cy":678}, "46":{"cx":736,"cy":600}, "47":{"cx":736,"cy":525}, "48":{"cx":736,"cy":446},
      "49":{"cx":736,"cy":371}, "50":{"cx":736,"cy":294}, "51":{"cx":681,"cy":246}, "52":{"cx":603,"cy":246},
      "53":{"cx":530,"cy":246}, "54":{"cx":452,"cy":248}, "55":{"cx":380,"cy":248}, "56":{"cx":305,"cy":246},
      "57":{"cx":254,"cy":296}, "58":{"cx":254,"cy":372}, "59":{"cx":254,"cy":448}, "60":{"cx":254,"cy":520},
      "61":{"cx":294,"cy":578}, "62":{"cx":376,"cy":578}, "63":{"cx":495,"cy":448}
    };
    
    const overlay = document.getElementById("overlay");
    const CELL_R = 18; 

    // Función para mover ficha
    function moveToken(orden, pos, dx=0, dy=0){
      const p = posMap[String(pos)];
      if(!p) return;
      
      // Creamos la ficha si no existe
      let t = document.getElementById("p" + orden);
      if (!t) {
         t = document.createElementNS("http://www.w3.org/2000/svg","circle");
         t.setAttribute("class", "token c" + orden); // Asigna clase c1, c2, c3... para color
         t.setAttribute("id", "p" + orden);
         t.setAttribute("r", CELL_R);
         overlay.appendChild(t);
      }
      
      t.setAttribute("cx", p.cx + dx);
      t.setAttribute("cy", p.cy + dy);
    }

    // --- BUCLE JAVA -> JS PARA POSICIONAR FICHAS ---
    <% 
    if (posiciones != null) {
        for (PartidaJugador pj : posiciones) { 
            // Desplazamiento para que no se superpongan
            int orden = pj.getOrden();
            int dx = 0;
            if (orden == 1) dx = -10;
            if (orden == 2) dx = 10;
            if (orden == 3) dx = -10; // Si hay más jugadores, ajustar lógica
    %>
        moveToken(<%= orden %>, <%= pj.getCasilla() %>, <%= dx %>, 0);
    <% 
        } 
    } 
    %>

</script>
</body>
</html>