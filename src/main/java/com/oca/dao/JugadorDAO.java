package com.oca.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.oca.modelo.Jugador;

public class JugadorDAO {

    // MÉTODO 1: REGISTRAR 
    public boolean registrarJugador(Jugador jugador) {
        boolean registrado = false;
        String sql = "INSERT INTO jugadores (nombre, password, partidas_ganadas) VALUES (?, ?, 0)";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, jugador.getNombre());
            ps.setString(2, jugador.getPassword());
            
            if (ps.executeUpdate() > 0) {
                registrado = true;
            }
        } catch (SQLException e) {
            System.out.println("Error al registrar: " + e.getMessage());
            e.printStackTrace();
        }
        return registrado;
    }

    // MÉTODO 2: LOGIN 
    public Jugador login(String usuario, String password) {
        Jugador jugador = null;
        String sql = "SELECT * FROM jugadores WHERE nombre = ? AND password = ?";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, usuario);
            ps.setString(2, password);
            
            // Ejecutamos la consulta y obtenemos los resultados
            ResultSet rs = ps.executeQuery();
            
            // Si hay un resultado, es que el usuario y contraseña son correctos
            if (rs.next()) {
                jugador = new Jugador();
                jugador.setId(rs.getInt("id"));
                jugador.setNombre(rs.getString("nombre"));
                jugador.setPassword(rs.getString("password"));
                jugador.setPartidasGanadas(rs.getInt("partidas_ganadas"));
            }
            
        } catch (SQLException e) {
            System.out.println("Error en login: " + e.getMessage());
            e.printStackTrace();
        }
        
        return jugador; // Devuelve el jugador si existe, o null si no existe
    }
    // MÉTODO 3: CAMBIAR CONTRASEÑA
    public boolean cambiarPassword(int idJugador, String passActual, String passNueva) {
        boolean cambiado = false;
        String sql = "UPDATE jugadores SET password = ? WHERE id = ? AND password = ?";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, passNueva);
            ps.setInt(2, idJugador);
            ps.setString(3, passActual);
            
            if (ps.executeUpdate() > 0) {
                cambiado = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cambiado;
    }
}