package com.oca.controller;

import java.io.IOException;

import com.oca.dao.JugadorDAO;
import com.oca.modelo.Jugador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Esta etiqueta conecta el HTML con Java: action="RegistroServlet"
@WebServlet("/RegistroServlet")
public class RegistroServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Recogemos los datos que el usuario escribió en el formulario HTML
        // Los nombres "usuario" y "pass1" deben coincidir con el 'name' de tus inputs en registro.html
        String nombre = request.getParameter("usuario");
        String pass = request.getParameter("pass1");
        
        // 2. Creamos un objeto Jugador con esos datos
        Jugador nuevoJugador = new Jugador(nombre, pass);
        
        // 3. Llamamos al DAO (nuestro experto en base de datos) para guardarlo
        JugadorDAO dao = new JugadorDAO();
        boolean exito = dao.registrarJugador(nuevoJugador);
        
        // 4. Dependiendo de si se guardó bien o mal, redirigimos al usuario
        if (exito) {
            // ¡Éxito! Lo mandamos a la pantalla de Login para que entre
            response.sendRedirect("login.html?registro=ok");
        } else {
            // Fallo (quizás el nombre ya existe o falló la conexión)
            // Lo devolvemos al registro
            response.sendRedirect("registro.html?error=fallo");
        }
    }
}