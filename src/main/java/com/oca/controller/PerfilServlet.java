package com.oca.controller;

import java.io.IOException;

import com.oca.dao.JugadorDAO;
import com.oca.modelo.Jugador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/PerfilServlet")
public class PerfilServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Si entran por URL, mostramos el JSP
        request.getRequestDispatcher("perfil.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Jugador jugador = (Jugador) session.getAttribute("jugador");
        
        if (jugador == null) {
            response.sendRedirect("login.html");
            return;
        }

        String passActual = request.getParameter("passActual");
        String passNueva = request.getParameter("passNueva");
        String passRepetida = request.getParameter("passRepetida");

        // 1. Validar que las nuevas contraseñas coincidan
        if (!passNueva.equals(passRepetida)) {
            response.sendRedirect("perfil.jsp?mensaje=noCoinciden&tipo=error");
            return;
        }

        // 2. Intentar cambiar la contraseña en la BD
        JugadorDAO dao = new JugadorDAO();
        boolean exito = dao.cambiarPassword(jugador.getId(), passActual, passNueva);

        if (exito) {
            // Actualizamos la sesión con la nueva contraseña
            jugador.setPassword(passNueva);
            session.setAttribute("jugador", jugador);
            response.sendRedirect("perfil.jsp?mensaje=exito&tipo=ok");
        } else {
            response.sendRedirect("perfil.jsp?mensaje=passIncorrecta&tipo=error");
        }
    }
}