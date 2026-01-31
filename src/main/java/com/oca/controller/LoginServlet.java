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

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Recoger datos del formulario (login.html)
        String usuario = request.getParameter("usuario");
        String pass = request.getParameter("password");
        
        // 2. Preguntar al DAO si existen
        JugadorDAO dao = new JugadorDAO();
        Jugador jugador = dao.login(usuario, pass);
        
        if (jugador != null) {
            // 3. ¡Login correcto!
            // Creamos una "Sesión" para recordar al usuario mientras navega
            HttpSession session = request.getSession();
            session.setAttribute("jugadorLogueado", jugador);
            
            // Lo mandamos a la portada (o al panel de juego)
            response.sendRedirect("index.html"); 
        } else {
            // 4. Login incorrecto
            // Lo devolvemos al login con un aviso de error
            response.sendRedirect("login.html?error=incorrecto");
        }
    }
}