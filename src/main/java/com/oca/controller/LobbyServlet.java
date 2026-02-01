package com.oca.controller;

import com.oca.dao.PartidaDAO;
import com.oca.modelo.Jugador;
import com.oca.modelo.Partida;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

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

        // 2. Pedir al DAO la lista de partidas disponibles
        List<Partida> listaPartidas = partidaDAO.obtenerPartidasEnEspera();

        // 3. Meter la lista en la "mochila" (request) para enviarla al HTML/JSP
        request.setAttribute("listaPartidas", listaPartidas);

        // 4. Mostrar la pantalla del lobby (Nota: usaremos .jsp para poder mostrar la lista dinámica)
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
                // Si se crea bien, recargamos el lobby para que aparezca en la lista
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