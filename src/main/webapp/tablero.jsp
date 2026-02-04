<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.oca.dao.PartidaDAO" %>
<%@ page import="com.oca.modelo.PartidaJugador" %>

<%
    // RECUPERAR DATOS REALES DE LA BASE DE DATOS
    // Esto es lo que permite que las fichas se muevan de verdad
    Integer idPartida = (Integer) session.getAttribute("idPartida");
    List<PartidaJugador> posiciones = null;
    
    if (idPartida != null) {
        PartidaDAO dao = new PartidaDAO();
        posiciones = dao.obtenerPosiciones(idPartida);
    }
%>
<!doctype html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Tablero Oca</title>
  <style>
    :root{
      --header-h: 72px;
    }

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
  </style>
</head>
<body>
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
        📢 ${param.msg != null ? param.msg : "¡Empieza el juego!"}
    </div>
  </header>

  <div class="stage">
    <div class="board-wrap">
      <img class="board-img" src="assets/tablero.jpg" alt="Tablero Oca" />

      <!-- viewBox: tus coordenadas están en 0..1000 -->
      <svg class="overlay" id="overlay" viewBox="0 0 1000 1000" preserveAspectRatio="none">
        <!-- ✅ SOLO dejamos fichas; las casillas se crean por JS -->
        <circle class="token" id="p1" cx="172" cy="891" r="18"></circle>
        <circle class="token" id="p2" cx="190" cy="891" r="18"></circle>
      </svg>
    </div>
  </div>

  <script>
    // ✅ MAPA COMPLETO 1..63
    const posMap = {
      "1": {"cx":172,"cy":885},
      "2": {"cx":374,"cy":885},
      "3": {"cx":457,"cy":885},
      "4": {"cx":535,"cy":885},
      "5": {"cx":619,"cy":885},
      "6": {"cx":699,"cy":885},
      "7": {"cx":777,"cy":885},
      "8": {"cx":851,"cy":885},
      "9": {"cx":895,"cy":835},
      "10":{"cx":895,"cy":758},
      "11":{"cx":895,"cy":685},
      "12":{"cx":895,"cy":604},
      "13":{"cx":895,"cy":523},
      "14":{"cx":895,"cy":443},
      "15":{"cx":895,"cy":367},
      "16":{"cx":895,"cy":290},
      "17":{"cx":895,"cy":217},
      "18":{"cx":895,"cy":136},
      "19":{"cx":836,"cy":96},
      "20":{"cx":760,"cy":96},
      "21":{"cx":691,"cy":96},
      "22":{"cx":610,"cy":96},
      "23":{"cx":531,"cy":96},
      "24":{"cx":452,"cy":96},
      "25":{"cx":374,"cy":96},
      "26":{"cx":296,"cy":96},
      "27":{"cx":223,"cy":96},
      "28":{"cx":153,"cy":96},
      "29":{"cx":92,"cy":145},
      "30":{"cx":92,"cy":224},
      "31":{"cx":92,"cy":296},
      "32":{"cx":92,"cy":372},
      "33":{"cx":92,"cy":450},
      "34":{"cx":92,"cy":527},
      "35":{"cx":92,"cy":602},
      "36":{"cx":92,"cy":679},
      "37":{"cx":139,"cy":733},
      "38":{"cx":221,"cy":733},
      "39":{"cx":294,"cy":733},
      "40":{"cx":374,"cy":733},
      "41":{"cx":454,"cy":733},
      "42":{"cx":535,"cy":733},
      "43":{"cx":612,"cy":733},
      "44":{"cx":687,"cy":733},
      "45":{"cx":736,"cy":678},
      "46":{"cx":736,"cy":600},
      "47":{"cx":736,"cy":525},
      "48":{"cx":736,"cy":446},
      "49":{"cx":736,"cy":371},
      "50":{"cx":736,"cy":294},
      "51":{"cx":681,"cy":246},
      "52":{"cx":603,"cy":246},
      "53":{"cx":530,"cy":246},
      "54":{"cx":452,"cy":248},
      "55":{"cx":380,"cy":248},
      "56":{"cx":305,"cy":246},
      "57":{"cx":254,"cy":296},
      "58":{"cx":254,"cy":372},
      "59":{"cx":254,"cy":448},
      "60":{"cx":254,"cy":520},
      "61":{"cx":294,"cy":578},
      "62":{"cx":376,"cy":578},
      "63":{"cx":495,"cy":448}
    };

    const overlay = document.getElementById("overlay");
    const CELL_R = 26; // radio de cada casilla clickable

    // ✅ Crear 63 casillas en el SVG (según posMap)
    function renderCells(){
      // Si recargas, evita duplicados: elimina casillas anteriores
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
        overlay.insertBefore(c, overlay.firstChild); // por debajo de las fichas
      }
    }

    // ✅ Mover ficha a una casilla
    function moveToken(tokenId, pos, dx=0, dy=0){
      const p = posMap[String(pos)];
      if(!p) return;
      const t = document.getElementById(tokenId);
      t.setAttribute("cx", p.cx + dx);
      t.setAttribute("cy", p.cy + dy);
    }

    // Pintamos casillas
    renderCells();

    // ✅ BUCLE MÁGICO JAVA -> JAVASCRIPT
    // Esto escribe una línea de código JS por cada jugador que haya en la BBDD
    <% 
    if (posiciones != null) {
        for (PartidaJugador pj : posiciones) { 
            // Calcula un pequeño desplazamiento para que no se monten una encima de otra
            int desplazamiento = (pj.getOrden() % 2 == 0) ? 10 : -10;
    %>
        // Mueve la ficha p1, p2, etc. a su casilla real
        moveToken("p<%= pj.getOrden() %>", <%= pj.getCasilla() %>, <%= desplazamiento %>, 0);
    <% 
        } 
    } 
    %>

    // ✅ Debug útil: click en una casilla => mover fichas ahí
    overlay.addEventListener("click", (e) => {
      const pos = e.target?.dataset?.pos;
      if (!pos) return;
      moveToken("p1", pos, -10, 0);
      moveToken("p2", pos, +10, 0);
      console.log("Casilla:", pos);
    });
    
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
</body>
</html>