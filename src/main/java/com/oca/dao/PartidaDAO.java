package com.oca.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.oca.modelo.Partida;

public class PartidaDAO {

    // 1. CREAR UNA PARTIDA NUEVA (Transacción: Mesa + Jugador)
    public boolean crearPartida(String nombrePartida, int idJugadorCreador) {
        // SQL A: Crear la mesa
        String sqlPartida = "INSERT INTO partidas (nombre, estado, id_turno_actual) VALUES (?, 'ESPERANDO', ?)";
        // SQL B: Sentar al creador
        String sqlJugador = "INSERT INTO partidas_jugadores (id_partida, id_jugador, casilla, orden) VALUES (?, ?, 1, 1)";

        Connection conn = null;

        try {
            // USAMOS TU CLASE CONEXION
            conn = Conexion.getConexion();
            
            // 🛑 Desactivamos guardado automático para hacer una transacción segura
            conn.setAutoCommit(false);

            // A. Insertar la partida
            PreparedStatement psPartida = conn.prepareStatement(sqlPartida, Statement.RETURN_GENERATED_KEYS);
            psPartida.setString(1, nombrePartida);
            psPartida.setInt(2, idJugadorCreador); 
            psPartida.executeUpdate();

            // Recuperar el ID de la partida que se acaba de crear
            ResultSet rs = psPartida.getGeneratedKeys();
            int idPartidaGenerada = 0;
            if (rs.next()) {
                idPartidaGenerada = rs.getInt(1);
            }

            // B. Insertar al jugador creador en la tabla intermedia
            PreparedStatement psJugador = conn.prepareStatement(sqlJugador);
            psJugador.setInt(1, idPartidaGenerada);
            psJugador.setInt(2, idJugadorCreador);
            psJugador.executeUpdate();

            // ✅ Confirmar cambios (Commit)
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback(); // 🔙 Si falla, deshacemos todo
            } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { 
                if (conn != null) conn.close(); 
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // 2. LISTAR PARTIDAS EN ESPERA (Para el Lobby)
    public List<Partida> obtenerPartidasEnEspera() {
        List<Partida> lista = new ArrayList<>();
        String sql = "SELECT * FROM partidas WHERE estado = 'ESPERANDO'";

        // Aquí usamos try-with-resources igual que en tu JugadorDAO
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Partida p = new Partida();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setEstado(rs.getString("estado"));
                p.setIdTurnoActual(rs.getInt("id_turno_actual"));
                // No necesitamos rellenar todos los campos para el listado
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}