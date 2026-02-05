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

        // --- CAMBIO PRINCIPAL AQUÍ ---
        
        // 2. Cargar "Mis Partidas" (Partidas donde el usuario YA está jugando)
        // Esto usa el método nuevo que añadimos al DAO
        List<Partida> misPartidas = partidaDAO.obtenerMisPartidas(jugador.getId());
        request.setAttribute("misPartidas", misPartidas);

        // 3. Cargar "Partidas Disponibles" (Partidas vacías donde el usuario NO está)
        // Esto usa el otro método nuevo del DAO
        List<Partida> partidasDisponibles = partidaDAO.obtenerPartidasDisponibles(jugador.getId());
        request.setAttribute("partidasDisponibles", partidasDisponibles);

        // 4. Mostrar la pantalla del lobby
        request.getRequestDispatcher("lobby.jsp").forward(request, response);
    }

    // doPost: SE EJECUTA CUANDO LE DAN AL BOTÓN "CREAR PARTIDA"
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Obtener usuario de la sesión
        HttpSession session = request.getSession();
        Jugador jugador = (Jugador) session.getAttribute("jugador");

        if (jugador != null) {
            // 2. Leer el nombre que le han puesto a la partida en el formulario
            String nombrePartida = request.getParameter("nombrePartida");

            // 3. Llamar al DAO para crearla
            boolean creada = partidaDAO.crearPartida(nombrePartida, jugador.getId());

            if (creada) {
                // Si se crea bien, recargamos el lobby para que aparezca en las listas
                response.sendRedirect("lobby"); 
            } else {
                // Si falla, mandamos un error (opcional)
                response.sendRedirect("lobby?error=true");
            }
        } else {
            response.sendRedirect("index.html");
        }
    }
}