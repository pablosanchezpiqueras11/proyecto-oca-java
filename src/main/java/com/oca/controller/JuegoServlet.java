package com.oca.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.oca.dao.JugadorDAO;
import com.oca.dao.PartidaDAO;
import com.oca.logica.ReglasOca;
import com.oca.modelo.Jugador;
import com.oca.modelo.PartidaJugador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/JuegoServlet")
public class JuegoServlet extends HttpServlet {

    private ReglasOca reglas = new ReglasOca();
    private PartidaDAO partidaDAO = new PartidaDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // 1. VARIABLE DE SEGURIDAD: Por defecto, siempre volvemos al tablero
        String urlDestino = "tablero.jsp"; 

        try {
            HttpSession session = request.getSession();
            Jugador jugador = (Jugador) session.getAttribute("jugador");
            Integer idPartida = (Integer) session.getAttribute("idPartida"); 

            if (jugador == null || idPartida == null) {
                response.sendRedirect("login.html");
                return;
            }

            String accion = request.getParameter("accion");
            if (accion == null) accion = ""; // Protección contra nulos
            
            boolean esAdmin = jugador.getNombre().equalsIgnoreCase("admin") || jugador.getNombre().equalsIgnoreCase("tester");
            String mensaje = "";
            int valorDado = 0; 

            // ===================================================================
            // CASO 1: TELETRANSPORTE (MODO ADMIN)
            // ===================================================================
            if ("teleportar".equals(accion)) {
                if (esAdmin) {
                    try {
                        String casillaParam = request.getParameter("casillaTeleport");
                        if (casillaParam != null && !casillaParam.trim().isEmpty()) {
                            int casillaDestino = Integer.parseInt(casillaParam);
                            
                            // Límites 1-63
                            if (casillaDestino < 1) casillaDestino = 1;
                            if (casillaDestino > 63) casillaDestino = 63;
                            
                            // Mover ficha
                            partidaDAO.actualizarPosicion(idPartida, jugador.getId(), casillaDestino);
                            mensaje = "🚀 Admin: Salto a la casilla " + casillaDestino;
                        } else {
                            mensaje = "⚠️ Escribe un número válido.";
                        }
                    } catch (Exception e) {
                        mensaje = "⚠️ Error en el número.";
                    }
                } else {
                    mensaje = "⛔ No tienes permisos.";
                }
                // Preparamos la URL de salida con el mensaje
                urlDestino = "tablero.jsp?msg=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
            }

            // ===================================================================
            // CASO 2: TIRAR EL DADO (JUEGO NORMAL)
            // ===================================================================
            else if ("tirar".equals(accion)) {
                
                // A) OBTENER ESTADO DE CASTIGO PREVIO (Para saber si se acaba de liberar)
                // Esto es necesario para el mensaje "Te quedan 0 turnos"
                List<PartidaJugador> posiciones = partidaDAO.obtenerPosiciones(idPartida);
                int castigoPrevio = 0;
                for (PartidaJugador pj : posiciones) {
                    if (pj.getIdJugador() == jugador.getId()) {
                        castigoPrevio = pj.getTurnosCastigo();
                        break;
                    }
                }

                // B) GESTIONAR CASTIGOS (Resta turno si corresponde)
                String estadoActual = partidaDAO.getEstadoPartida(idPartida);
                int idTurnoActual = partidaDAO.getTurnoActual(idPartida);
                int turnosCastigo = partidaDAO.gestionarTurnosCastigo(idPartida, jugador.getId());

                if ("TERMINADA".equals(estadoActual)) {
                    // Si terminó, no hacemos nada
                } 
                else if (idTurnoActual != jugador.getId()) {
                    mensaje = "✋ No es tu turno.";
                }
                else if (turnosCastigo == -1) {
                    mensaje = "¡Sigues en el Pozo! 🕳️<br>Debes esperar a que otro caiga.";
                    partidaDAO.pasarTurno(idPartida);
                }
                else if (turnosCastigo > 0) {
                    mensaje = "¡Estás Castigado!<br>Te quedan " + turnosCastigo + " turnos.";
                    partidaDAO.pasarTurno(idPartida);
                } 
                else {
                    // --- JUGAR (turnosCastigo es 0) ---
                    
                    // Comprobamos si nos acabamos de liberar (teníamos castigo y ahora es 0)
                    String avisoLiberado = "";
                    if (castigoPrevio > 0) {
                        avisoLiberado = "🔓 ¡Te quedan 0 turnos! ¡Es tu turno!<br>";
                    }

                    String dadoTrucado = request.getParameter("dadoTrucado");
                    if (esAdmin && dadoTrucado != null && !dadoTrucado.isEmpty()) {
                        try {
                            valorDado = Integer.parseInt(dadoTrucado);
                        } catch (Exception e) { valorDado = reglas.lanzarDado(); }
                    } else {
                        valorDado = reglas.lanzarDado(); 
                    }

                    int casillaActual = partidaDAO.getCasillaActual(idPartida, jugador.getId());
                    int casillaTentativa = reglas.calcularPosicionRebote(casillaActual, valorDado);
                    int casillaFinal = reglas.comprobarCasillaEspecial(casillaTentativa);
                    
                    partidaDAO.actualizarPosicion(idPartida, jugador.getId(), casillaFinal);
                    
                    // Comprobar Victoria
                    boolean huboSalto = (casillaTentativa != casillaFinal);
                    if (casillaFinal >= 63) {
                         // Si gana, le añadimos el aviso si se acaba de liberar
                         mensaje = avisoLiberado + "¡HAS GANADO! 🏆";
                         partidaDAO.terminarPartida(idPartida, jugador.getId());
                         new JugadorDAO().sumarVictoria(jugador.getId());
                         jugador.setPartidasGanadas(jugador.getPartidasGanadas() + 1);
                    } else {
                         // Mensajes y Turnos
                        if (huboSalto) {
                             if (casillaTentativa == 6 || casillaTentativa == 12) {
                                 mensaje = "🌊 ¡De puente a puente y tiro porque me lleva la corriente!<br>(" + casillaTentativa + "->" + casillaFinal + ")";
                             } else if (casillaTentativa == 26 || casillaTentativa == 53) {
                                 mensaje = "🎲 ¡De dado a dado y tiro porque me ha tocado!<br>(" + casillaTentativa + "->" + casillaFinal + ")";
                             } else if (casillaTentativa == 42) { // Laberinto
                                 mensaje = "🕸️ ¡Del Laberinto al 30! Retrocedes.";
                             } else if (casillaTentativa == 58) { // Muerte
                                 mensaje = "💀 ¡LA MUERTE! Vuelves a la Casilla 1.";
                             } else {
                                 // Oca
                                 mensaje = "🦆 ¡De Oca a Oca y tiro porque me toca!<br>(" + casillaTentativa + "->" + casillaFinal + ")";
                             }
                        } 
                        else if (casillaFinal == 19) {
                            mensaje = "🛌 ¡POSADA! Pierdes 1 turno durmiendo.";
                        } else if (casillaFinal == 31) { // Pozo
                            mensaje = "🕳️ ¡Al POZO! No sales hasta que otro caiga.";
                        } else if (casillaFinal == 52) { // Cárcel
                            mensaje = "⛓️ ¡A la CÁRCEL! Pierdes 3 turnos.";
                        } 
                        else {
                            mensaje = "🎲 Sacas un " + valorDado + " y vas a la casilla " + casillaFinal;
                        }

                        // Añadimos el aviso de "Te quedan 0 turnos" al principio si corresponde
                        mensaje = avisoLiberado + mensaje;

                        // 3. GESTIÓN DE TURNOS EXTRA
                        if (reglas.turnoExtra(casillaFinal)) {
                             mensaje += "<br>¡Vuelves a tirar!";
                        } else {
                             partidaDAO.pasarTurno(idPartida);
                             mensaje += "<br>Turno del siguiente.";
                        }
                    }
                }
                // Preparamos la URL de salida codificando el mensaje para caracteres especiales y saltos
                String msgEnc = URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
                urlDestino = "tablero.jsp?msg=" + msgEnc + "&dado=" + valorDado;
            }

            // ===================================================================
            // CASO 3: INICIAR PARTIDA
            // ===================================================================
            else if ("iniciar".equals(accion)) {
                boolean iniciada = partidaDAO.iniciarPartida(idPartida);
                if (!iniciada) {
                    urlDestino = "tablero.jsp?msg=" + URLEncoder.encode("⚠️ Faltan jugadores (min 2).", StandardCharsets.UTF_8);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            urlDestino = "tablero.jsp?msg=" + URLEncoder.encode("⚠️ Error técnico en el servidor.", StandardCharsets.UTF_8);
        }

        response.sendRedirect(urlDestino);
    }
}