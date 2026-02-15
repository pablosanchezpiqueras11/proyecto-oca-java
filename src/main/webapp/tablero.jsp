<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.oca.modelo.*" %>
<%@ page import="com.oca.dao.PartidaDAO" %>
<%@ page import="java.util.List" %>

<%
    // GESTIÓN DE SESIÓN Y DATOS
    Jugador jugador = (Jugador) session.getAttribute("jugador");
    Integer idPartida = (Integer) session.getAttribute("idPartida");

    if (jugador == null || idPartida == null) {
        response.sendRedirect("login.html");
        return;
    }

    PartidaDAO dao = new PartidaDAO();
    List<PartidaJugador> jugadoresEnPartida = dao.obtenerPosiciones(idPartida);
    String estadoPartida = dao.getEstadoPartida(idPartida);
    if (estadoPartida == null || estadoPartida.isEmpty()) estadoPartida = "ESPERANDO";

    int idTurnoActual = dao.getTurnoActual(idPartida);
    boolean soyAdmin = false;
    boolean soyGanador = false;     
    int idGanador = -1;             
    
    // Variable para saber MI estado actual (si estoy castigado, etc.)
    PartidaJugador miEstado = null;
    for (PartidaJugador pj : jugadoresEnPartida) {
        // Detectar si soy Admin (el creador, orden 1)
        if (pj.getIdJugador() == jugador.getId() && pj.getOrden() == 1) soyAdmin = true;
        // Guardar mi objeto de jugador para consultar mis castigos luego
        if (pj.getIdJugador() == jugador.getId()) {
            miEstado = pj;
        }

        // Detectar ganador
        if (pj.getCasilla() >= 63) {
            idGanador = pj.getIdJugador();
            if (idGanador == jugador.getId()) soyGanador = true;
        }
    }
    int totalJugadores = jugadoresEnPartida.size();
    // RECUPERAR DATOS DE LA URL
    String msg = request.getParameter("msg");
    String dadoParam = request.getParameter("dado");
    int numeroDado = 1; // Valor por defecto
    if (dadoParam != null && !dadoParam.isEmpty()) {
        try { numeroDado = Integer.parseInt(dadoParam);
        } catch(Exception e){}
    }
%>

<!doctype html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>La Oca Online</title>

  <%-- LÓGICA DE REFRESCO INTELIGENTE --%>
  <% if ("ESPERANDO".equals(estadoPartida)) { %>
      <meta http-equiv="refresh" content="3;url=tablero.jsp">
      <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
      
  <% } else if ("TERMINADA".equals(estadoPartida)) { %>
      <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
      
  <% } else { 
      // Si NO es mi turno, refresco la página para ver cuándo me toca
      if (idTurnoActual != jugador.getId()) { 
  %>
      <meta http-equiv="refresh" content="3;url=tablero.jsp">
  <%  } 
     } 
  %>

  <% if (!"ESPERANDO".equals(estadoPartida) && !"TERMINADA".equals(estadoPartida)) { %>
  <style>
        :root { --sidebar-w: 250px;
        --header-h: 90px; /* Un poco más alto para mensajes de 2 líneas */
        }
        body { margin: 0;
        font-family: 'Segoe UI', sans-serif; background: #2c3e50;
        color: white;
        display: flex; flex-direction: column; height: 100vh; overflow: hidden;
        }
        header { height: var(--header-h);
        background: #1a252f; display: flex; align-items: center; justify-content: space-between;
        padding: 0 20px; border-bottom: 2px solid #f1c40f; box-sizing: border-box;
        }
        .header-msg {
            color: #f1c40f;
            font-weight: bold; text-align: center; line-height: 1.4; font-size: 1.1rem;
        }
        .header-submsg {
            font-size: 0.9rem;
            color: #bdc3c7; font-weight: normal;
        }
        .main-container { display: flex;
        flex: 1;
        height: calc(100vh - var(--header-h)); overflow: hidden;
        }
        
        /* PANEL LATERAL */
        .sidebar { width: var(--sidebar-w);
        background: #22313f; border-right: 1px solid #34495e; padding: 15px; display: flex; flex-direction: column; justify-content: space-between; box-sizing: border-box; overflow-y: auto;
        }
        .sidebar h3 { color: #f1c40f; margin: 0 0 15px 0; font-size: 1.1rem;
        border-bottom: 1px solid #34495e; padding-bottom: 5px; }
        
        .player-card { display: flex;
        align-items: center; padding: 10px; margin-bottom: 10px; border-radius: 8px; background: #2c3e50; border: 2px solid transparent; transition: 0.3s;
        }
        .is-turn { border-color: #f1c40f; background: #34495e;
        box-shadow: 0 0 10px rgba(241, 196, 15, 0.4); }
        .dot { width: 12px;
        height: 12px; border-radius: 50%; margin-right: 10px; border: 1px solid #fff; flex-shrink: 0;
        }
        .name { font-size: 0.9rem; font-weight: bold;
        }
        .casilla-info { font-size: 0.75rem; color: #bdc3c7; display: block;
        }

        /* TABLERO */
        .board-area { flex: 1;
        display: flex; align-items: center; justify-content: center; padding: 10px; }
        .board-container { position: relative;
        height: 95%; aspect-ratio: 1/1; }
        .board-img { height: 100%; width: auto; border-radius: 12px;
        box-shadow: 0 0 40px rgba(0,0,0,0.7); }
        .overlay { position: absolute; top: 0;
        left: 0; width: 100%; height: 100%; }
        
        /* FICHAS */
        .token { 
            stroke: #fff;
            stroke-width: 3; 
            filter: drop-shadow(0px 6px 4px rgba(0,0,0,0.8)); 
            transition: all 1s cubic-bezier(0.175, 0.885, 0.32, 1.275); 
            animation: pulse-all 2s infinite alternate ease-in-out;
        }
        @keyframes pulse-all { from { r: 18; stroke-width: 3;
        } to { r: 21; stroke-width: 4; } }

        /* DADO 3D */
        .dice-container { perspective: 600px;
        display: flex; justify-content: center; margin: 20px 0; }
        .dice { width: 60px;
        height: 60px; position: relative; transform-style: preserve-3d; transition: transform 1s ease-out;
        }
        .dice-face { position: absolute; width: 60px; height: 60px; background: #fff;
        border: 2px solid #ddd; border-radius: 8px; display: flex; justify-content: center; align-items: center; font-size: 28px; font-weight: bold; color: #333;
        }
        
        .face-1 { transform: translateZ(30px);
        }
        .face-2 { transform: rotateY(180deg) translateZ(30px);
        }
        .face-3 { transform: rotateY(90deg) translateZ(30px);
        }
        .face-4 { transform: rotateY(-90deg) translateZ(30px);
        }
        .face-5 { transform: rotateX(90deg) translateZ(30px);
        }
        .face-6 { transform: rotateX(-90deg) translateZ(30px);
        }

        .rolling { animation: rolling-anim 0.5s infinite linear;
        }
        @keyframes rolling-anim { 
            0% { transform: rotateX(0deg) rotateY(0deg);
            } 
            100% { transform: rotateX(360deg) rotateY(360deg);
            } 
        }

        .btn-tirar { width: 100%;
        padding: 15px; background: #e67e22; color: white; border: 2px solid white; border-radius: 10px; font-size: 1.1rem; font-weight: bold; cursor: pointer;
        box-shadow: 0 4px 10px rgba(0,0,0,0.4); }
        .btn-tirar:disabled { background: #7f8c8d; opacity: 0.7;
        }
        
        /* ESTILOS DEL MODO TESTER / ADMIN */
        .admin-panel {
            margin-bottom: 15px;
            background: rgba(0, 0, 0, 0.5);
            padding: 10px;
            border-radius: 8px;
            border: 1px solid #f1c40f;
        }
        .admin-title {
            color: #f1c40f;
            font-weight: bold; font-size: 0.85rem; display: block; margin-bottom: 5px; text-transform: uppercase; letter-spacing: 1px;
        }
        .teleport-row {
            display: flex;
            gap: 5px; margin-top: 8px; border-top: 1px solid rgba(255,255,255,0.2); padding-top: 8px;
        }
  </style>
  <% } %>
</head>

<body class="<%= "ESPERANDO".equals(estadoPartida) ? "bg-light" : "" %>">

<%-- ESCENARIO 1: SALA DE ESPERA --%>
<% if ("ESPERANDO".equals(estadoPartida)) { %>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-8 text-center">
                <div class="card shadow-lg">
                    <div class="card-header bg-primary text-white"><h2>⏳ Sala de Espera</h2></div>
   
                    <div class="card-body">
                        <h4 class="mb-4">Jugadores: <span class="badge bg-info"><%= totalJugadores %> / 4</span></h4>
                        <div class="list-group mb-4 text-start">
                    
                        <% for (PartidaJugador pj : jugadoresEnPartida) { 
                                 String nombreReal = dao.getNombreJugador(pj.getIdJugador());
                        %>
                                <div class="list-group-item d-flex justify-content-between align-items-center">
                                    <span>👤 <strong><%= nombreReal %></strong>
                                    <% if (pj.getOrden() == 1) { %><span class="badge bg-warning text-dark ms-2">👑 Host</span><% } %>
                                    <% if (pj.getIdJugador() == jugador.getId()) { %><span class="badge bg-info text-dark ms-1">👈 Tú</span><% } %>
                                   </span>
                                    <span class="badge rounded-pill bg-success">Listo</span>
                                </div>
                        <% } %>
                        </div>
              
                        <% if (soyAdmin && totalJugadores >= 2) { %>
  
                            <form action="JuegoServlet" method="post">
                                <input type="hidden" name="accion" value="iniciar">
                                <button type="submit" class="btn btn-lg btn-primary w-100">🚀 INICIAR</button>
                             </form>
                        <% } else if (soyAdmin) { %>
                
                            <div class="alert alert-warning">Esperando jugadores (mín. 2)...</div>
                        <% } else { %>
                            <div class="alert alert-info">Esperando al anfitrión...</div>
                        <% } %>
              
                        <br><a href="lobby" class="btn btn-outline-danger btn-sm">Salir</a>
                    </div>
                </div>
            </div>
        </div>
    </div>

<%-- ESCENARIO 2: PARTIDA TERMINADA --%>
<% } else if ("TERMINADA".equals(estadoPartida)) { %>
 
    <div class="container-fluid min-vh-100 d-flex justify-content-center align-items-center" style="background-color: #2c3e50;">
        <div class="card shadow-lg border-0 p-4" style="max-width: 600px; width: 100%; border-radius: 20px;">
            <div class="card-body text-center">
                <h1 class="<%= soyGanador ? "text-success" : "text-secondary" %> display-4 fw-bold">
                    <%= soyGanador ? "¡HAS GANADO! 🏆" : "Juego Terminado 🏁" %>
                </h1>
                <p class="lead mt-3">Ganador: <strong><%= dao.getNombreJugador(idGanador) %></strong></p>
                <hr><a href="lobby" class="btn btn-primary w-100 rounded-pill">🏠 Volver</a>
            </div>
        </div>
    </div>

<%-- ESCENARIO 3: JUEGO EN MARCHA --%>
<% } else { 
%>
    <header>
        <div>Partida #<%= idPartida %></div>
        <div class="header-msg">
            <% 
                String textoCabecera = "";
                // Obtenemos el nombre de a quién le toca
                String nombreDelTurno = "Rival";
                try {
                    nombreDelTurno = dao.getNombreJugador(idTurnoActual);
                } catch(Exception e){}

                // PRIORIDAD 1: Mensaje de evento (acaba de tirar)
                if (msg != null) {
                    out.print(java.net.URLDecoder.decode(msg, "UTF-8"));
                } 
                // PRIORIDAD 2: Estado pasivo (mientras espera)
                else {
                    // Verificamos si YO tengo castigo
                    int misTurnosCastigo = (miEstado != null) ? miEstado.getTurnosCastigo() : 0;
                    
                    if (misTurnosCastigo == -1) {
                         // Estoy en el pozo
                         out.print("🕳️ ¡Estás atrapado en el Pozo!<br><span class='header-submsg'>Es el turno de " + nombreDelTurno + "</span>");
                    } 
                    else if (misTurnosCastigo > 0) {
                         // CORRECCIÓN AQUÍ: Restamos 1 visualmente para que Posada sea 1 y Cárcel sea 3
                         out.print("🚫 Te quedan <b>" + (misTurnosCastigo - 1) + "</b> turnos de espera.<br><span class='header-submsg'>Es el turno de " + nombreDelTurno + "</span>");
                    }
                    else if (idTurnoActual == jugador.getId()) {
                         // Es mi turno y no estoy castigado
                         out.print("¡Es tu turno! 🎲");
                    } else {
                         // No es mi turno y estoy libre
                         out.print("Es el turno de " + nombreDelTurno + " ⏳");
                    }
                }
            %>
        </div>
        <a href="lobby" style="color: #bdc3c7; text-decoration: none; font-size: 0.8rem;">← Salir</a>
    </header>

    <div class="main-container">
        <div class="sidebar">
            <div>
                <h3>Jugadores</h3>
  
                <% for (PartidaJugador pj : jugadoresEnPartida) { 
                    String nombreDisplay = dao.getNombreJugador(pj.getIdJugador());
                    boolean esTurno = (pj.getIdJugador() == idTurnoActual);
                    // Colores por orden de llegada (1 al 4)
                    String color = (pj.getOrden()==1)?"#27ae60":(pj.getOrden()==2)?"#2980b9":(pj.getOrden()==3)?"#e74c3c":"#d4fc10";
                %>
                    <div class="player-card <%= esTurno ? "is-turn" : "" %>">
                        <div class="dot" style="background-color: <%= color %>;"></div>
                        <div>
                            <div class="name"><%= nombreDisplay %></div>
                            <span class="casilla-info">Casilla: <%= pj.getCasilla() %>
                            <%-- CORRECCIÓN AQUÍ: Restamos 1 al número visual de castigos --%>
                            <% if (pj.getTurnosCastigo() > 0) { %> (🚫 <%= pj.getTurnosCastigo() - 1 %>) <% } %>
                            <% if (pj.getTurnosCastigo() == -1) { %> (🕳️ POZO) <% } %>
                            </span>
                        </div>
                   </div>
                <% } %>
            </div>

            <div style="text-align: center;">
                <div class="dice-container">
                    <div id="visual-dice" class="dice">
                        <div class="dice-face face-1">1</div>
                        <div class="dice-face face-2">2</div>
                        <div class="dice-face face-3">3</div>
                        <div class="dice-face face-4">4</div>
                        <div class="dice-face face-5">5</div>
                        <div class="dice-face face-6">6</div>
                    </div>
                </div>

                <% if (idTurnoActual == jugador.getId()) { %>
      
                    <form id="form-tirar" action="JuegoServlet" method="post">
                        <input type="hidden" id="accion-input" name="accion" value="tirar">
         
                        <%-- MODO ADMIN / TESTER (INCLUYE DADO Y TELETRANSPORTE) --%>
                        <% if (jugador.getNombre().equalsIgnoreCase("admin") || jugador.getNombre().equalsIgnoreCase("tester")) { %>
                            <div class="admin-panel">
                                <label class="admin-title">🔧 MODO TESTER</label>
                                
                                <%-- 1. DADO TRUCADO --%>
                                <select name="dadoTrucado" style="width: 100%; padding: 5px; border-radius: 4px; font-size: 0.9rem;">
                                    <option value="">🎲 Dado Aleatorio</option>
                                    <option value="1">Forzar 1</option>
                                    <option value="2">Forzar 2</option>
                                    <option value="3">Forzar 3</option>
                                    <option value="4">Forzar 4</option>
                                    <option value="5">Forzar 5</option>
                                    <option value="6">Forzar 6</option>
                                </select>
                         
                                <%-- 2. TELETRANSPORTE --%>
                                <div class="teleport-row">
                                    <input type="number" name="casillaTeleport" placeholder="Casilla #" min="1" max="63" style="width: 70px; padding: 4px; border-radius: 4px; border:none;">
                                    <button type="button" onclick="hacerTeleport()" style="flex:1; background: #8e44ad; color: white; border: none; border-radius: 4px; font-weight: bold; font-size: 0.8rem; cursor: pointer;">IR 🚀</button>
                                </div>
                            </div>
                        <% } %>

                        <button type="button" id="btn-tirar" onclick="animarYEnviar()" class="btn-tirar">
                            <%= (miEstado != null && (miEstado.getTurnosCastigo() > 0 || miEstado.getTurnosCastigo() == -1)) ? "PASAR TURNO (CASTIGO)" : "🎲 TIRAR" %>
                         </button>
                    </form>
            
                <% } else { %>
                    <button class="btn-tirar" disabled>Esperando...</button>
                <% } %>
            </div>
       
        </div>

        <div class="board-area">
            <div class="board-container">
                <img src="assets/tablero.jpg" class="board-img" alt="Tablero">
                <svg class="overlay" viewBox="0 0 1000 1000">
                     <circle id="p1" class="token" fill="#27ae60" r="18" style="display:none"></circle>
                     <circle id="p2" class="token" fill="#2980b9" r="18" style="display:none"></circle>
                    <circle id="p3" class="token" fill="#e74c3c" r="18" style="display:none"></circle>
                    <circle id="p4" class="token" fill="#d4fc10" r="18" style="display:none"></circle>
                </svg>
            </div>
        
        </div>
    </div>

    <script>
        // COORDENADAS
        const posMap = {
            "1": {"cx":172,"cy":885}, "2": {"cx":374,"cy":885}, "3": {"cx":457,"cy":885}, "4": {"cx":535,"cy":885},
            "5": {"cx":619,"cy":885}, "6": {"cx":699,"cy":885}, 
            "7": {"cx":777,"cy":885}, "8": {"cx":851,"cy":885},
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
        // 1. FUNCIÓN PARA MOVER EL DADO A LA CARA CORRECTA
        function fijarDado(num) {
            const dice = document.getElementById('visual-dice');
            const coords = {
                1: 'rotateX(0deg) rotateY(0deg)',
                2: 'rotateY(180deg)',
                3: 'rotateY(-90deg)',
                4: 'rotateY(90deg)',
                5: 'rotateX(-90deg)',
                6: 'rotateX(90deg)'
            };
            if(coords[num]) dice.style.transform = coords[num];
        }

        // Si tenemos un número guardado de la última tirada, lo fijamos
        <% if (numeroDado > 0) { %>
            fijarDado(<%= numeroDado %>);
        <% } %>

        // 2. ANIMACIÓN AL HACER CLIC EN TIRAR
        function animarYEnviar() {
            // Aseguramos que la acción sea 'tirar'
            document.getElementById('accion-input').value = 'tirar';
            const dice = document.getElementById('visual-dice');
            const btn = document.getElementById('btn-tirar');
            dice.classList.add('rolling'); // Activa la animación CSS infinita
            btn.disabled = true;
            // Espera 1 segundo viendo girar el dado antes de enviar
            setTimeout(() => { document.getElementById('form-tirar').submit(); }, 1000);
        }

        // 3.FUNCIÓN: TELETRANSPORTE
        function hacerTeleport() {
            // Cambiamos la acción oculta a 'teleportar'
            document.getElementById('accion-input').value = 'teleportar';
            // Enviamos el formulario inmediatamente (sin esperar al dado)
            document.getElementById('form-tirar').submit();
        }

        // 4. POSICIONAR FICHAS
        function updateFicha(id, casilla, dx, dy) {
            const f = document.getElementById(id);
            const p = posMap[String(casilla)];
            if (f && p) {
                f.setAttribute("cx", p.cx + dx);
                f.setAttribute("cy", p.cy + dy);
                f.style.display = "block";
            }
        }

        <% for (PartidaJugador pj : jugadoresEnPartida) {
            int dx = (pj.getOrden() % 2 != 0) ? -18 : 18;
            int dy = (pj.getOrden() > 2) ? 18 : -18;
        %>
            updateFicha("p<%= pj.getOrden() %>", <%= pj.getCasilla() %>, <%= dx %>, <%= dy %>);
        <% } %>
    </script>
<% } %>

</body>
</html>