package com.oca.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.oca.dao.JugadorDAO;
import com.oca.dao.PartidaDAO;
import com.oca.logica.ReglasOca;
import com.oca.modelo.Jugador;

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
                
                // Comprobaciones iniciales (Turno, Estado, Castigos)
                String estadoActual = partidaDAO.getEstadoPartida(idPartida);
                int idTurnoActual = partidaDAO.getTurnoActual(idPartida);
                int turnosCastigo = partidaDAO.gestionarTurnosCastigo(idPartida, jugador.getId());

                if ("TERMINADA".equals(estadoActual)) {
                    // Si terminó, no hacemos nada, urlDestino ya es tablero.jsp
                } 
                else if (idTurnoActual != jugador.getId()) {
                    mensaje = "✋ No es tu turno.";
                }
                else if (turnosCastigo == -1) {
                    mensaje = "¡Sigues en el Pozo! 🕳️";
                    partidaDAO.pasarTurno(idPartida);
                }
                else if (turnosCastigo > 0) {
                    mensaje = "¡Castigado! Faltan " + turnosCastigo + " turnos.";
                    partidaDAO.pasarTurno(idPartida);
                } 
                else {
                    // --- JUGAR ---
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
                    if (casillaFinal >= 63) {
                         mensaje = "¡HAS GANADO! 🏆";
                         partidaDAO.terminarPartida(idPartida, jugador.getId());
                         new JugadorDAO().sumarVictoria(jugador.getId());
                         jugador.setPartidasGanadas(jugador.getPartidasGanadas() + 1);
                    } else {
                         // Mensajes y Turnos
                         int castigo = ReglasOca.getTurnosCastigo(casillaFinal);
                         if (castigo == -1) mensaje = "¡Al POZO! 😱";
                         else if (castigo > 0) mensaje = "¡Castigo! Pierdes turnos.";
                         else mensaje = "Sacas un " + valorDado + " y vas a la " + casillaFinal;

                         if (reglas.turnoExtra(casillaFinal)) {
                             mensaje += " ¡Tiras otra vez! 🎲";
                         } else {
                             partidaDAO.pasarTurno(idPartida);
                             mensaje += " Turno del siguiente.";
                         }
                    }
                }
                // Preparamos la URL de salida
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
            // Si hay CUALQUIER error técnico (base de datos, nulls, etc.), capturamos y avisamos
            e.printStackTrace();
            urlDestino = "tablero.jsp?msg=" + URLEncoder.encode("⚠️ Error técnico en el servidor.", StandardCharsets.UTF_8);
        }

        response.sendRedirect(urlDestino);
    }
}