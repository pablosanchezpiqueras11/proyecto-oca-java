package com.oca.controller;

import java.io.IOException;
import java.util.List;

import com.oca.dao.PartidaDAO;
import com.oca.modelo.Jugador;
import com.oca.modelo.Partida;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/lobby")
public class LobbyServlet extends HttpServlet {

    private PartidaDAO partidaDAO;

    @Override
    public void init() {
        partidaDAO = new PartidaDAO();
    }

    // doGet: SE EJECUTA CUANDO ENTRAS A LA PÁGINA "LOBBY"
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Verificar seguridad: ¿Está logueado el usuario?
        HttpSession session = request.getSession();
        Jugador jugador = (Jugador) session.getAttribute("jugador");

        if (jugador == null) {
            // Si no está logueado, patada al Login
            response.sendRedirect("index.html");
            return;
        }
        
        // 2. Cargar "Mis Partidas" (Partidas donde el usuario YA está jugando)

        List<Partida> misPartidas = partidaDAO.obtenerMisPartidas(jugador.getId());
        request.setAttribute("misPartidas", misPartidas);

        // 3. Cargar "Partidas Disponibles" (Partidas vacías donde el usuario NO está)

        List<Partida> partidasDisponibles = partidaDAO.obtenerPartidasDisponibles(jugador.getId());
        request.setAttribute("partidasDisponibles", partidasDisponibles);

        // 4. Mostrar la pantalla del lobby
        request.getRequestDispatcher("lobby.jsp").forward(request, response);
    }

    // doPost: SE EJECUTA CUANDO LE DAN AL BOTÓN "CREAR PARTIDA"
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Obtener usuario de la sesión (común para todo)
        HttpSession session = request.getSession();
        Jugador jugador = (Jugador) session.getAttribute("jugador");

        if (jugador == null) {
            response.sendRedirect("index.html"); // Si no está logueado, fuera
            return;
        }

        // 2. ¿QUÉ QUIERE HACER EL USUARIO? (Leemos la "accion")
        String accion = request.getParameter("accion");

        // CASO A: ELIMINAR PARTIDA 🗑️
        if ("eliminar".equals(accion)) {
            // Leemos el ID que viene oculto en el formulario
            String idStr = request.getParameter("idPartida");
            if (idStr != null) {
                int idPartida = Integer.parseInt(idStr);
                
                // Llamamos al método de borrar
                boolean borrada = partidaDAO.eliminarPartida(idPartida);
                
                if (borrada) {
                    response.sendRedirect("lobby?msg=Partida+eliminada");
                } else {
                    response.sendRedirect("lobby?error=No+se+pudo+borrar");
                }
            }
            return;
        }

        // CASO B: CREAR PARTIDA
        // Si no es eliminar, asumimos que es crear
        
        String nombrePartida = request.getParameter("nombrePartida");
        
        // Un pequeño control por si alguien envía el formulario vacío
        if (nombrePartida != null && !nombrePartida.trim().isEmpty()) {
            boolean creada = partidaDAO.crearPartida(nombrePartida, jugador.getId());
            if (creada) {
                response.sendRedirect("lobby");
            } else {
                response.sendRedirect("lobby?error=Error+al+crear");
            }
        } else {
            // Si llegan aquí sin nombre ni acción, recargamos el lobby
            response.sendRedirect("lobby");
        }
    }
}