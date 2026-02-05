package com.oca.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.oca.modelo.Partida;
import com.oca.modelo.PartidaJugador;

public class PartidaDAO {

    // 1. CREAR UNA PARTIDA NUEVA (Transacción: Mesa + Jugador)
    public boolean crearPartida(String nombrePartida, int idJugadorCreador) {
        String sqlPartida = "INSERT INTO partidas (nombre, estado, id_turno_actual) VALUES (?, 'ESPERANDO', ?)";
        String sqlJugador = "INSERT INTO partidas_jugadores (id_partida, id_jugador, casilla, orden) VALUES (?, ?, 1, 1)";

        Connection conn = null;

        try {
            conn = Conexion.getConexion();
            conn.setAutoCommit(false); // Transacción

            // A. Insertar la partida
            PreparedStatement psPartida = conn.prepareStatement(sqlPartida, Statement.RETURN_GENERATED_KEYS);
            psPartida.setString(1, nombrePartida);
            psPartida.setInt(2, idJugadorCreador); 
            psPartida.executeUpdate();

            ResultSet rs = psPartida.getGeneratedKeys();
            int idPartidaGenerada = 0;
            if (rs.next()) {
                idPartidaGenerada = rs.getInt(1);
            }

            // B. Insertar al jugador creador
            PreparedStatement psJugador = conn.prepareStatement(sqlJugador);
            psJugador.setInt(1, idPartidaGenerada);
            psJugador.setInt(2, idJugadorCreador);
            psJugador.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // 2. LISTAR PARTIDAS EN ESPERA
    public List<Partida> obtenerPartidasEnEspera() {
        List<Partida> lista = new ArrayList<>();
        String sql = "SELECT p.*, (SELECT COUNT(*) FROM partidas_jugadores WHERE id_partida = p.id) as total_jugadores " +
             "FROM partidas p WHERE p.estado = 'ESPERANDO'";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Partida p = new Partida();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setEstado(rs.getString("estado"));
                p.setJugadoresActuales(rs.getInt("total_jugadores"));
                lista.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    // 3. UNIRSE A PARTIDA
    public boolean unirseAPartida(int idPartida, int idJugador) {
        Connection conn = null;
        boolean exito = false;

        try {
            conn = Conexion.getConexion();
            
            // Verificar si ya estoy dentro
            String sqlYaEstoy = "SELECT COUNT(*) FROM partidas_jugadores WHERE id_partida = ? AND id_jugador = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlYaEstoy)) {
                ps.setInt(1, idPartida);
                ps.setInt(2, idJugador);
                ResultSet rsYa = ps.executeQuery();
                if (rsYa.next() && rsYa.getInt(1) > 0) return true; 
            }

            // Verificar hueco e insertar
            String sqlVerificar = "SELECT COUNT(*) FROM partidas_jugadores WHERE id_partida = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlVerificar)) {
                ps.setInt(1, idPartida);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    int jugadoresActuales = rs.getInt(1);
                    if (jugadoresActuales < 4) {
                        int nuevoOrden = jugadoresActuales + 1;
                        String sqlInsertar = "INSERT INTO partidas_jugadores (id_partida, id_jugador, casilla, orden) VALUES (?, ?, 1, ?)";
                        try (PreparedStatement psInsert = conn.prepareStatement(sqlInsertar)) {
                            psInsert.setInt(1, idPartida);
                            psInsert.setInt(2, idJugador);
                            psInsert.setInt(3, nuevoOrden);
                            if (psInsert.executeUpdate() > 0) exito = true;
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { try { if (conn != null) conn.close(); } catch (Exception e) {}; }
        
        return exito;
    }

    // 4. OBTENER CASILLA ACTUAL
    public int getCasillaActual(int idPartida, int idJugador) {
        int casilla = 1;
        String sql = "SELECT casilla FROM partidas_jugadores WHERE id_partida = ? AND id_jugador = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPartida);
            ps.setInt(2, idJugador);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) casilla = rs.getInt("casilla");
        } catch (Exception e) { e.printStackTrace(); }
        return casilla;
    }

    // 5. ACTUALIZAR POSICIÓN
    public boolean actualizarPosicion(int idPartida, int idJugador, int nuevaCasilla) {
        String sql = "UPDATE partidas_jugadores SET casilla = ? WHERE id_partida = ? AND id_jugador = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevaCasilla);
            ps.setInt(2, idPartida);
            ps.setInt(3, idJugador);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 6. PASAR TURNO
    public boolean pasarTurno(int idPartida) {
        String sqlInfoActual = 
            "SELECT p.id_turno_actual, pj.orden, " +
            "(SELECT COUNT(*) FROM partidas_jugadores WHERE id_partida = p.id) as total " +
            "FROM partidas p " +
            "JOIN partidas_jugadores pj ON p.id = pj.id_partida AND p.id_turno_actual = pj.id_jugador " +
            "WHERE p.id = ?";
            
        String sqlBuscarSiguiente = "SELECT id_jugador FROM partidas_jugadores WHERE id_partida = ? AND orden = ?";
        String sqlActualizar = "UPDATE partidas SET id_turno_actual = ? WHERE id = ?";

        try (Connection conn = Conexion.getConexion()) {
            int ordenActual = 0;
            int totalJugadores = 0;
            
            try (PreparedStatement ps = conn.prepareStatement(sqlInfoActual)) {
                ps.setInt(1, idPartida);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    ordenActual = rs.getInt("orden");
                    totalJugadores = rs.getInt("total");
                }
            }

            if (totalJugadores == 0) return false;

            int siguienteOrden = (ordenActual % totalJugadores) + 1;
            int idSiguienteJugador = 0;
            
            try (PreparedStatement ps = conn.prepareStatement(sqlBuscarSiguiente)) {
                ps.setInt(1, idPartida);
                ps.setInt(2, siguienteOrden);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) idSiguienteJugador = rs.getInt("id_jugador");
            }

            if (idSiguienteJugador > 0) {
                try (PreparedStatement ps = conn.prepareStatement(sqlActualizar)) {
                    ps.setInt(1, idSiguienteJugador);
                    ps.setInt(2, idPartida);
                    ps.executeUpdate();
                    return true;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // 7. GESTIÓN DE CASTIGOS
    public int gestionarTurnosCastigo(int idPartida, int idJugador) {
        String sqlSelect = "SELECT turnos_castigo FROM partidas_jugadores WHERE id_partida = ? AND id_jugador = ?";
        String sqlUpdate = "UPDATE partidas_jugadores SET turnos_castigo = turnos_castigo - 1 WHERE id_partida = ? AND id_jugador = ? AND turnos_castigo > 0";
        int turnosRestantes = 0;
        
        try (Connection conn = Conexion.getConexion()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlSelect)) {
                ps.setInt(1, idPartida);
                ps.setInt(2, idJugador);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) turnosRestantes = rs.getInt("turnos_castigo");
            }
            if (turnosRestantes > 0) {
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, idPartida);
                    ps.setInt(2, idJugador);
                    ps.executeUpdate();
                }
                return turnosRestantes - 1; 
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public void aplicarCastigo(int idPartida, int idJugador, int turnos) {
        String sql = "UPDATE partidas_jugadores SET turnos_castigo = ? WHERE id_partida = ? AND id_jugador = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, turnos);
            ps.setInt(2, idPartida);
            ps.setInt(3, idJugador);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 8. OBTENER POSICIONES (MODIFICADO: JOIN CON NOMBRE)
    public List<PartidaJugador> obtenerPosiciones(int idPartida) {
        List<PartidaJugador> lista = new ArrayList<>();
        // ✅ Recuperamos el nombre del jugador haciendo JOIN con la tabla 'jugadores'
        String sql = "SELECT pj.*, j.nombre FROM partidas_jugadores pj " +
                     "JOIN jugadores j ON pj.id_jugador = j.id " +
                     "WHERE pj.id_partida = ? ORDER BY pj.orden ASC";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idPartida);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                PartidaJugador pj = new PartidaJugador();
                pj.setIdPartida(rs.getInt("id_partida"));
                pj.setIdJugador(rs.getInt("id_jugador"));
                pj.setCasilla(rs.getInt("casilla"));
                pj.setOrden(rs.getInt("orden"));
                
                // ✅ IMPORTANTE: Esto requiere que hayas añadido 'private String nombre;' y su setter en PartidaJugador.java
                pj.setNombre(rs.getString("nombre")); 
                
                lista.add(pj);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
    
    // 9. OBTENER PARTIDA POR ID (NUEVO: Para saber el turno)
    public Partida obtenerPartida(int idPartida) {
        Partida p = null;
        String sql = "SELECT * FROM partidas WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idPartida);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                p = new Partida();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setEstado(rs.getString("estado"));
                p.setIdTurnoActual(rs.getInt("id_turno_actual"));
                p.setIdGanador(rs.getInt("id_ganador"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return p;
    }

    // 10. OBTENER MIS PARTIDAS
    public List<Partida> obtenerMisPartidas(int idJugador) {
        List<Partida> lista = new ArrayList<>();
        String sql = "SELECT partidas.*, (SELECT COUNT(*) FROM partidas_jugadores WHERE partidas_jugadores.id_partida = partidas.id) as total " +
                     "FROM partidas JOIN partidas_jugadores ON partidas.id = partidas_jugadores.id_partida " +
                     "WHERE partidas_jugadores.id_jugador = ? AND partidas.estado <> 'TERMINADA'";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idJugador);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Partida p = new Partida();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setEstado(rs.getString("estado"));
                p.setJugadoresActuales(rs.getInt("total"));
                lista.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    // 11. OBTENER PARTIDAS DISPONIBLES
    public List<Partida> obtenerPartidasDisponibles(int idJugador) {
        List<Partida> lista = new ArrayList<>();
        String sql = "SELECT partidas.*, (SELECT COUNT(*) FROM partidas_jugadores WHERE partidas_jugadores.id_partida = partidas.id) as total " +
                     "FROM partidas WHERE partidas.estado = 'ESPERANDO' " +
                     "AND partidas.id NOT IN (SELECT id_partida FROM partidas_jugadores WHERE id_jugador = ?)";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idJugador);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int total = rs.getInt("total");
                if (total < 4) {
                    Partida p = new Partida();
                    p.setId(rs.getInt("id"));
                    p.setNombre(rs.getString("nombre"));
                    p.setEstado(rs.getString("estado"));
                    p.setJugadoresActuales(total);
                    lista.add(p);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}