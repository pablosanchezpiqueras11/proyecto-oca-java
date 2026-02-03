package com.oca.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

        // 1. OBTENER DATOS DE LA SESIÓN
        HttpSession session = request.getSession();
        Jugador jugador = (Jugador) session.getAttribute("jugador");
        
        // Asumimos que guardaste la ID de la partida en la sesión al entrar
        // (Si da error null, asegúrate de guardarla en UnirsePartidaServlet o Lobby)
        Integer idPartida = (Integer) session.getAttribute("idPartida"); 

        if (jugador == null || idPartida == null) {
            response.sendRedirect("login.html");
            return;
        }

        String accion = request.getParameter("accion");
        String mensaje = "";

        // 2. ACCIÓN: TIRAR EL DADO
        if ("tirar".equals(accion)) {
            
            // A. VERIFICAR CASTIGOS ANTES DE TIRAR
            // Este método resta 1 al castigo y nos dice cuántos quedan
            int turnosCastigo = partidaDAO.gestionarTurnosCastigo(idPartida, jugador.getId());
            
            if (turnosCastigo > 0) {
                // ESTÁ CASTIGADO: No tira y pasa turno
                mensaje = "¡Sigues castigado! Te quedan " + turnosCastigo + " turnos.";
                partidaDAO.pasarTurno(idPartida);
                
            } else {
                // PUEDE JUGAR: Lógica completa de movimiento
                
                // 1. Lanzar dado
                int dado = reglas.lanzarDado();
                
                // 2. Obtener dónde estaba
                int casillaActual = partidaDAO.getCasillaActual(idPartida, jugador.getId());
                
                // 3. Calcular destino inicial (con rebote si pasa de 63)
                int casillaTentativa = reglas.calcularPosicionRebote(casillaActual, dado);
                
                // 4. Verificar efectos especiales (Oca, Puente, Laberinto, Muerte...)
                int casillaFinal = reglas.comprobarCasillaEspecial(casillaTentativa);
                
                // 5. Guardar nueva posición en BBDD
                partidaDAO.actualizarPosicion(idPartida, jugador.getId(), casillaFinal);
                
                // 6. ¿Ha caído en casilla de castigo (Posada/Pozo/Cárcel)?
                int turnosNuevosCastigo = reglas.getTurnosCastigo(casillaFinal);
                if (turnosNuevosCastigo > 0) {
                    partidaDAO.aplicarCastigo(idPartida, jugador.getId(), turnosNuevosCastigo);
                    mensaje = "Has caído en zona de castigo. Pierdes " + turnosNuevosCastigo + " turnos.";
                }

                // 7. GESTIÓN DE TURNO EXTRA
                // Si es Oca o Puente, repite turno. Si no, pasa al siguiente.
                boolean repiteTurno = reglas.turnoExtra(casillaFinal);
                
                if (repiteTurno) {
                    mensaje = "¡Tiras otra vez! (Dado: " + dado + ")";
                } else {
                    // Si no repite y no ha ganado todavía, pasa el turno
                    if (casillaFinal < 63) {
                        partidaDAO.pasarTurno(idPartida);
                        mensaje = "Avanzas a la casilla " + casillaFinal + ". Turno del siguiente.";
                    } else {
                         // ¡HA GANADO! (Aquí podrías poner el estado de la partida a TERMINADA)
                         mensaje = "¡FELICIDADES! HAS GANADO LA PARTIDA 🏆";
                    }
                }
            }
            
            // CODIFICAR EL MENSAJE PARA QUE NO DE ERROR HTTP 400
            // Esto convierte "Avanzas casilla 4" en "Avanzas+casilla+4"
            String mensajeCodificado = URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
            
            // Redirigir al tablero
            response.sendRedirect("tablero.jsp?msg=" + mensajeCodificado);
        }
    }
}