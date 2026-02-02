package com.oca.controller;

import java.io.IOException;

import com.oca.dao.PartidaDAO;
import com.oca.modelo.Jugador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// Esta URL debe coincidir con la que pusimos en el botón del JSP: "unirse"
@WebServlet("/unirse")
public class UnirsePartidaServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Obtener sesión y jugador
        HttpSession session = request.getSession();
        Jugador jugador = (Jugador) session.getAttribute("jugador");
        
        if (jugador == null) {
            response.sendRedirect("login.html");
            return;
        }

        // 2. Obtener la ID de la partida desde el enlace (unirse?idPartida=5)
        String idPartidaStr = request.getParameter("idPartida");
        
        if (idPartidaStr != null) {
            int idPartida = Integer.parseInt(idPartidaStr);
            
            // 3. Intentar unirse usando el DAO
            PartidaDAO dao = new PartidaDAO();
            boolean unido = dao.unirseAPartida(idPartida, jugador.getId());
            
            if (unido) {
                // Éxito: Volvemos al lobby (o podríamos ir ya al tablero)
                // Usamos un parámetro ?msg para mostrar un mensajito si quieres
                response.sendRedirect("lobby?mensaje=unido");
            } else {
                // Fallo: Estaba llena o error
                response.sendRedirect("lobby?error=llena");
            }
        } else {
            response.sendRedirect("lobby");
        }
    }
}