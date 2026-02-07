package com.oca.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.oca.dao.JugadorDAO;
import com.oca.dao.PartidaDAO; // Añadido import que faltaba
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

        // 1. OBTENER DATOS DE LA SESIÓN
        HttpSession session = request.getSession();
        Jugador jugador = (Jugador) session.getAttribute("jugador");
        Integer idPartida = (Integer) session.getAttribute("idPartida"); 

        if (jugador == null || idPartida == null) {
            response.sendRedirect("login.html");
            return;
        }

        String accion = request.getParameter("accion");
        String mensaje = "";

        // 2. ACCIÓN: TIRAR EL DADO
        if ("tirar".equals(accion)) {
            // 1. Preguntamos a la base de datos de quién es el turno
            int idTurnoActual = partidaDAO.getTurnoActual(idPartida); // <--- Necesitas crear este método rápido en el DAO

            // 2. Si el turno NO es mío... ¡Quieto parado!
            if (idTurnoActual != jugador.getId()) {
                String error = URLEncoder.encode("✋ No es tu turno. Espera a que juegue el otro.", StandardCharsets.UTF_8);
                response.sendRedirect("tablero.jsp?msg=" + error);
            return; // <--- IMPRESCINDIBLE: Cortamos la ejecución aquí
            }
            
            // A. VERIFICAR CASTIGOS ANTES DE TIRAR
            int turnosCastigo = partidaDAO.gestionarTurnosCastigo(idPartida, jugador.getId());
            
            // CASO POZO (-1): Atrapado hasta rescate
            if (turnosCastigo == -1) {
                 mensaje = "¡Estás en el Pozo! 🕳️ Necesitas que otro jugador caiga aquí para salir.";
                 partidaDAO.pasarTurno(idPartida); // Pasa el turno sin tirar
            }
            // CASO CASTIGO NORMAL (>0): Resta turno y pasa
            else if (turnosCastigo > 0) {
                mensaje = "¡Sigues castigado! Te quedan " + turnosCastigo + " turnos.";
                partidaDAO.pasarTurno(idPartida); // Pasa el turno sin tirar
            } 
            // CASO LIBRE (0): PUEDE JUGAR
            else {
                // 1. Lanzar dado
                int dado = reglas.lanzarDado();
                
                // 2. Obtener dónde estaba
                int casillaActual = partidaDAO.getCasillaActual(idPartida, jugador.getId());
                
                // 3. Calcular destino
                int casillaTentativa = reglas.calcularPosicionRebote(casillaActual, dado);
                
                // 4. Efectos especiales (Oca, Puente...)
                int casillaFinal = reglas.comprobarCasillaEspecial(casillaTentativa);
                
                // 5. Guardar nueva posición (¡Aquí dentro se calcula el Pozo/Castigo automáticamente!)
                partidaDAO.actualizarPosicion(idPartida, jugador.getId(), casillaFinal);
                
                // 6. Preparar mensaje para el usuario
                if (casillaFinal >= 63) {
                     mensaje = "¡FELICIDADES! HAS GANADO LA PARTIDA 🏆";
                     partidaDAO.terminarPartida(idPartida, jugador.getId());
                     JugadorDAO jugadorDAO = new JugadorDAO();
                     jugadorDAO.sumarVictoria(jugador.getId());
                     jugador.setPartidasGanadas(jugador.getPartidasGanadas() + 1);
                } else {
                     // Miramos si ha caído en zona peligrosa para avisarle
                     int castigoNuevo = ReglasOca.getTurnosCastigo(casillaFinal);
                     if (castigoNuevo == -1) {
                         mensaje = "¡Has caído en el POZO! 😱 Espera a que te rescaten.";
                     } else if (castigoNuevo > 0) {
                         mensaje = "Has caído en zona de castigo. Pierdes " + castigoNuevo + " turnos.";
                     } else {
                         mensaje = "Avanzas a la casilla " + casillaFinal + ".";
                     }

                     // 7. Gestión de Turno Extra
                     boolean repiteTurno = reglas.turnoExtra(casillaFinal);
                     if (repiteTurno) {
                         mensaje += " ¡Tiras otra vez! 🎲";
                     } else {
                         partidaDAO.pasarTurno(idPartida);
                         mensaje += " Turno del siguiente.";
                     }
                }
            }
            
            // CODIFICAR EL MENSAJE
            String mensajeCodificado = URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
            response.sendRedirect("tablero.jsp?msg=" + mensajeCodificado);
        }
        // 3. ACCIÓN: INICIAR PARTIDA (Solo para el creador)
        if ("iniciar".equals(accion)) {
            
            // Intentamos iniciarla
            boolean iniciada = partidaDAO.iniciarPartida(idPartida);
            
            if (iniciada) {
                // Si todo va bien, recargamos el tablero y ya saldrá el juego
                response.sendRedirect("tablero.jsp");
            } else {
                // Si falla (ej. solo hay 1 jugador), avisamos
                String error = URLEncoder.encode("⚠️ Necesitas entre 2 y 4 jugadores para empezar.", StandardCharsets.UTF_8);
                response.sendRedirect("tablero.jsp?msg=" + error);
            }
            return; // Cortamos aquí
        }
    }
}