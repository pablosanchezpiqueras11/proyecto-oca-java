<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.oca.modelo.*" %>
<%@ page import="com.oca.dao.PartidaDAO" %>
<%@ page import="java.util.List" %>

<%
    // 1. SEGURIDAD: Verificar sesión
    Jugador jugador = (Jugador) session.getAttribute("jugador");
    Integer idPartida = (Integer) session.getAttribute("idPartida");

    if (jugador == null || idPartida == null) {
        response.sendRedirect("login.html");
        return;
    }

    // 2. ACTUALIZAR ESTADO: Consultamos la BBDD para ver cómo va la partida real
    PartidaDAO dao = new PartidaDAO();
    
    // A. Obtenemos posiciones para saber quién está y qué orden tengo YO
    List<PartidaJugador> jugadoresEnPartida = dao.obtenerPosiciones(idPartida);
    
    // B. Obtenemos info de la partida (Estado y Nombre)
    String estadoPartida = "ESPERANDO";
    String nombrePartida = "Partida";
    
    // Buscamos la partida en la lista de mis partidas para sacar el estado actualizado
    List<Partida> misPartidas = dao.obtenerMisPartidas(jugador.getId());
    for(Partida p : misPartidas) {
        if(p.getId() == idPartida) {
            estadoPartida = p.getEstado();
            nombrePartida = p.getNombre();
            break;
        }
    }

    // 3. ¿SOY EL CREADOR? (El creador suele tener orden 1)
    boolean soyAdmin = false;
    for (PartidaJugador pj : jugadoresEnPartida) {
        if (pj.getIdJugador() == jugador.getId() && pj.getOrden() == 1) {
            soyAdmin = true;
        }
    }
    
    int totalJugadores = jugadoresEnPartida.size();
%>

<!doctype html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  
  <% if ("ESPERANDO".equals(estadoPartida)) { %>
      <meta http-equiv="refresh" content="3">
      <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <% } else { %>
      <meta http-equiv="refresh" content="5">
  <% } %>

  <title>Tablero Oca - <%= nombrePartida %></title>

  <%-- ESTILOS DEL TABLERO (Solo se usan si estamos JUGANDO) --%>
  <% if (!"ESPERANDO".equals(estadoPartida)) { %>
  <style>
    :root{ --header-h: 72px; }

    body{
      margin:0;
      font-family:Segoe UI,Tahoma,Geneva,Verdana,sans-serif;
      background:#2c3e50;
      color:#fff;
      overflow:hidden;
    }

    header{
      height: var(--header-h);
      background:#22313f;
      padding:16px 22px;
      display:flex;
      justify-content:space-between;
      align-items:center;
      box-sizing:border-box;
    }
    header h1{margin:0;color:#f1c40f;font-size:1.1rem}
    header a{
      color:#2c3e50;
      text-decoration:none;
      font-weight:900;
      background:#95a5a6;
      padding:10px 12px;
      border-radius:10px
    }

    .stage{
      height: calc(100vh - var(--header-h));
      display:flex;
      align-items:center;
      justify-content:center;
      padding:12px;
      box-sizing:border-box;
    }

    .board-wrap{
      position:relative;
      width: min(95vw, calc(100vh - var(--header-h) - 24px));
    }

    .board-img{
      width:100%;
      height:auto;
      display:block;
      border-radius:16px;
      box-shadow:0 10px 25px rgba(0,0,0,.25);
    }

    .overlay{
      position:absolute;
      top:0; left:0;
      width:100%;
      height:100%;
      border-radius:16px;
    }

    /* hotspots */
    .cell{fill:rgba(0,0,0,0);stroke:rgba(255,255,255,.35);stroke-width:2;cursor:pointer}
    .cell:hover{stroke:rgba(241,196,15,.9);stroke-width:3}

    /* fichas */
    .token{stroke:white;stroke-width:2}
    #p1{fill:#27ae60}
    #p2{fill:#2980b9}
    #p3{fill:#e74c3c} 
    #p4{fill:#d4fc10} 
  </style>
  <% } %>
</head>

<body class="<%= "ESPERANDO".equals(estadoPartida) ? "bg-light" : "" %>">

<%-- ======================================================================== --%>
<%--                        ESCENARIO 1: SALA DE ESPERA                       --%>
<%-- ======================================================================== --%>
<% if ("ESPERANDO".equals(estadoPartida)) { %>

    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-8 text-center">
                
                <div class="card shadow-lg">
                    <div class="card-header bg-primary text-white">
                        <h2>⏳ Sala de Espera: <%= nombrePartida %></h2>
                    </div>
                    <div class="card-body">
                        
                        <h4 class="mb-4">Jugadores unidos: <span class="badge bg-info"><%= totalJugadores %> / 4</span></h4>
                        
                        <div class="list-group mb-4 text-start">
                            <% for (PartidaJugador pj : jugadoresEnPartida) { %>
                                <div class="list-group-item d-flex justify-content-between align-items-center">
                                    <span>
                                        👤 <strong>Jugador (Orden <%= pj.getOrden() %>)</strong>
                                        <%= (pj.getOrden() == 1) ? " (👑 Anfitrión)" : "" %>
                                        <%= (pj.getIdJugador() == jugador.getId()) ? " (Tú)" : "" %>
                                    </span>
                                    <span class="badge rounded-pill bg-success">Listo</span>
                                </div>
                            <% } %>
                        </div>

                        <%-- MENSAJE DE ESPERA O BOTÓN DE INICIAR --%>
                        <% if (soyAdmin) { %>
                            
                            <% if (totalJugadores >= 2) { %>
                                <div class="alert alert-success">¡Ya sois suficientes! Puedes empezar cuando quieras.</div>
                                <form action="JuegoServlet" method="post">
                                    <input type="hidden" name="accion" value="iniciar">
                                    <button type="submit" class="btn btn-lg btn-primary w-100">
                                        🚀 INICIAR PARTIDA
                                    </button>
                                </form>
                            <% } else { %>
                                <div class="alert alert-warning">
                                    ⚠️ Necesitas al menos <strong>2 jugadores</strong> para poder iniciar la partida.
                                    <br>Esperando a que se unan más...
                                </div>
                                <button class="btn btn-secondary btn-lg w-100" disabled>Esperando jugadores...</button>
                            <% } %>

                        <% } else { %>
                            <div class="alert alert-info">
                                ☕ Esperando a que el anfitrión inicie la partida...
                            </div>
                            <div class="spinner-border text-primary" role="status">
                                <span class="visually-hidden">Loading...</span>
                            </div>
                        <% } %>

                        <br><br>
                        <a href="lobby" class="btn btn-outline-danger btn-sm mt-3">Salir al Lobby</a>
                    </div>
                </div>

            </div>
        </div>
    </div>

<%-- ======================================================================== --%>
<%--                        ESCENARIO 2: JUEGO EN MARCH                       --%>
<%-- ======================================================================== --%>
<% } else { %>

    <header>
        <div style="display:flex; align-items:center; gap: 20px;">
            <h1>🦆 Tablero</h1>
            <a href="lobby" style="font-size: 0.8rem;">← Salir</a>
        </div>
    
        <div style="
            background: rgba(255,255,255,0.1); 
            padding: 10px 20px; 
            border-radius: 8px; 
            color: #f1c40f; 
            font-weight: bold;
            border: 1px solid #f1c40f;">
            📢 <%= (request.getParameter("msg") != null) ? java.net.URLDecoder.decode(request.getParameter("msg"), "UTF-8") : "¡Empieza el juego!" %>
        </div>
    </header>
    
    <div class="stage">
        <div class="board-wrap">
            <img class="board-img" src="assets/tablero.jpg" alt="Tablero Oca" />
    
            <svg class="overlay" id="overlay" viewBox="0 0 1000 1000" preserveAspectRatio="none">
                <circle class="token" id="p1" cx="172" cy="891" r="18" style="display:none"></circle>
                <circle class="token" id="p2" cx="190" cy="891" r="18" style="display:none"></circle>
                <circle class="token" id="p3" cx="172" cy="891" r="18" style="display:none"></circle>
                <circle class="token" id="p4" cx="190" cy="891" r="18" style="display:none"></circle>
            </svg>
        </div>
    </div>
    
  <script>
    // 1. DEFINICIÓN DEL MAPA (Igual que antes)
    const posMap = {
        "1": {"cx":172,"cy":885}, "2": {"cx":374,"cy":885}, "3": {"cx":457,"cy":885}, "4": {"cx":535,"cy":885},
        "5": {"cx":619,"cy":885}, "6": {"cx":699,"cy":885}, "7": {"cx":777,"cy":885}, "8": {"cx":851,"cy":885},
        "9": {"cx":895,"cy":835}, "10":{"cx":895,"cy":758}, "11":{"cx":895,"cy":685}, "12":{"cx":895,"cy":604},
        "13":{"cx":895,"cy":523}, "14":{"cx":895,"cy":443}, "15":{"cx":895,"cy":367}, "16":{"cx":895,"cy":290},
        "17":{"cx":895,"cy":217}, "18":{"cx":895,"cy":136}, "19":{"cx":836,"cy":96}, "20":{"cx":760,"cy":96},
        "21":{"cx":691,"cy":96}, "22":{"cx":610,"cy":96}, "23":{"cx":531,"cy":96}, "24":{"cx":452,"cy":96},
        "25":{"cx":374,"cy":96}, "26":{"cx":296,"cy":96}, "27":{"cx":223,"cy":96}, "28":{"cx":153,"cy":96},
        "29":{"cx":92,"cy":145}, "30":{"cx":92,"cy":224}, "31":{"cx":92,"cy":296}, "32":{"cx":92,"cy":372},
        "33":{"cx":92,"cy":450}, "34":{"cx":92,"cy":527}, "35":{"cx":92,"cy":602}, "36":{"cx":92,"cy":679},
        "37":{"cx":139,"cy":733}, "38":{"cx":221,"cy":733}, "39":{"cx":294,"cy":733}, "40":{"cx":374,"cy":733},
        "41":{"cx":454,"cy":733}, "42":{"cx":535,"cy":733}, "43":{"cx":612,"cy":733}, "44":{"cx":687,"cy":733},
        "45":{"cx":736,"cy":678}, "46":{"cx":736,"cy":600}, "47":{"cx":736,"cy":525}, "48":{"cx":736,"cy":446},
        "49":{"cx":736,"cy":371}, "50":{"cx":736,"cy":294}, "51":{"cx":681,"cy":246}, "52":{"cx":603,"cy":246},
        "53":{"cx":530,"cy":246}, "54":{"cx":452,"cy":248}, "55":{"cx":380,"cy":248}, "56":{"cx":305,"cy":246},
        "57":{"cx":254,"cy":296}, "58":{"cx":254,"cy":372}, "59":{"cx":254,"cy":448}, "60":{"cx":254,"cy":520},
        "61":{"cx":294,"cy":578}, "62":{"cx":376,"cy":578}, "63":{"cx":495,"cy":448}
    };
    
    const overlay = document.getElementById("overlay");
    const CELL_R = 26; 

    // Función para dibujar las zonas de clic
    function renderCells(){
        overlay.querySelectorAll(".cell").forEach(c => c.remove());
        for (let i = 1; i <= 63; i++){
            const p = posMap[String(i)];
            if (!p) continue;
            const c = document.createElementNS("http://www.w3.org/2000/svg","circle");
            c.setAttribute("class","cell");
            c.dataset.pos = String(i);
            c.setAttribute("cx", p.cx);
            c.setAttribute("cy", p.cy);
            c.setAttribute("r", CELL_R);
            overlay.insertBefore(c, overlay.firstChild);
        }
    }

    // Función segura para mover y MOSTRAR ficha
    function updateFicha(idElemento, casilla, dx, dy) {
        const ficha = document.getElementById(idElemento);
        const p = posMap[String(casilla)];
        
        if (ficha && p) {
            // 1. Mover
            ficha.setAttribute("cx", p.cx + dx);
            ficha.setAttribute("cy", p.cy + dy);
            // 2. HACER VISIBLE (Esto es lo que fallaba antes)
            ficha.style.display = "block"; 
            ficha.style.opacity = "1";
        }
    }

    renderCells();

    // GENERACIÓN DE CÓDIGO DESDE JAVA
    <% 
    if (jugadoresEnPartida != null) {
        for (PartidaJugador pj : jugadoresEnPartida) {
            // Cálculos en Java para evitar lios en JS
            int dx = (pj.getOrden() % 2 != 0) ? -10 : 10;
            int dy = (pj.getOrden() > 2) ? 10 : -10;
            String idFicha = "p" + pj.getOrden();
    %>
            // Llamada simple: ID, Casilla, DX, DY
            updateFicha("<%= idFicha %>", <%= pj.getCasilla() %>, <%= dx %>, <%= dy %>);
    <% 
        }
    } 
    %>
  </script>
    
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
    
<% } %>

</body>
</html>