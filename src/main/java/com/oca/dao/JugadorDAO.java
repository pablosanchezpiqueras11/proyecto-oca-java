package com.oca.dao;

import com.oca.modelo.Jugador;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JugadorDAO {

    // Método para guardar un nuevo jugador en la BD
    public boolean registrarJugador(Jugador jugador) {
        boolean registrado = false;
        
        // La sentencia SQL con interrogaciones (para seguridad)
        String sql = "INSERT INTO jugadores (nombre, password, partidas_ganadas) VALUES (?, ?, 0)";
        
        // Usamos try-with-resources para que la conexión se cierre sola
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            // Sustituimos las interrogaciones por los datos del jugador
            ps.setString(1, jugador.getNombre());
            ps.setString(2, jugador.getPassword()); // En el futuro esto debería ir encriptado
            
            // Ejecutamos la orden
            int filasAfectadas = ps.executeUpdate();
            
            // Si ha guardado al menos una fila, es que ha ido bien
            if (filasAfectadas > 0) {
                registrado = true;
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar jugador: " + e.getMessage());
            e.printStackTrace();
        }
        
        return registrado;
    }
}