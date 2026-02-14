package com.oca.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.oca.logica.ReglasOca;
import com.oca.modelo.Partida;

public class PartidaDAO {

    // 1. CREAR UNA PARTIDA NUEVA
    public boolean crearPartida(String nombrePartida, int idJugadorCreador) {
        String sqlPartida = "INSERT INTO partidas (nombre, estado, id_turno_actual) VALUES (?, 'ESPERANDO', ?)";
        String sqlJugador = "INSERT INTO partidas_jugadores (id_partida, id_jugador, casilla, orden) VALUES (?, ?, 1, 1)";

        Connection conn = null;

        try {
            conn = Conexion.getConexion();
            conn.setAutoCommit(false); // Transacción manual

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

            // B. Insertar al creador
            PreparedStatement psJugador = conn.prepareStatement(sqlJugador);
            psJugador.setInt(1, idPartidaGenerada);
            psJugador.setInt(2, idJugadorCreador);
            psJugador.executeUpdate();

            conn.commit(); // Confirmar
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
                p.setIdTurnoActual(rs.getInt("id_turno_actual"));
                p.setJugadoresActuales(rs.getInt("total_jugadores"));
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 2.A. OBTENER MIS PARTIDAS
    public List<Partida> obtenerMisPartidas(int idJugador) {
        List<Partida> lista = new ArrayList<>();
        String sql = "SELECT partidas.*, " +
                     "(SELECT COUNT(*) FROM partidas_jugadores WHERE partidas_jugadores.id_partida = partidas.id) as total " +
                     "FROM partidas " +
                     "JOIN partidas_jugadores ON partidas.id = partidas_jugadores.id_partida " +
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 2.B. OBTENER PARTIDAS DISPONIBLES
    public List<Partida> obtenerPartidasDisponibles(int idJugador) {
        List<Partida> lista = new ArrayList<>();
        String sql = "SELECT partidas.*, " +
                     "(SELECT COUNT(*) FROM partidas_jugadores WHERE partidas_jugadores.id_partida = partidas.id) as total " +
                     "FROM partidas " +
                     "WHERE partidas.estado = 'ESPERANDO' " +
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 3. UNIRSE A UNA PARTIDA
    public boolean unirseAPartida(int idPartida, int idJugador) {
        Connection conn = null;
        boolean exito = false;

        try {
            conn = Conexion.getConexion();
            
            // 0. Comprobar si ya estoy dentro
            String sqlYaEstoy = "SELECT COUNT(*) FROM partidas_jugadores WHERE id_partida = ? AND id_jugador = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlYaEstoy)) {
                ps.setInt(1, idPartida);
                ps.setInt(2, idJugador);
                ResultSet rsYa = ps.executeQuery();
                if (rsYa.next() && rsYa.getInt(1) > 0) {
                     return true; // Ya estás dentro
                }
            }

            // 1. Verificar hueco
            String sqlVerificar = "SELECT COUNT(*) FROM partidas_jugadores WHERE id_partida = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlVerificar)) {
                ps.setInt(1, idPartida);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    int jugadoresActuales = rs.getInt(1);
                    if (jugadoresActuales < 4) {
                        // 2. Insertar con orden correcto
                        int nuevoOrden = jugadoresActuales + 1;
                        String sqlInsertar = "INSERT INTO partidas_jugadores (id_partida, id_jugador, casilla, orden) VALUES (?, ?, 1, ?)";
                        try (PreparedStatement psInsert = conn.prepareStatement(sqlInsertar)) {
                            psInsert.setInt(1, idPartida);
                            psInsert.setInt(2, idJugador);
                            psInsert.setInt(3, nuevoOrden);
                            int filas = psInsert.executeUpdate();
                            if (filas > 0) exito = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {};
        }
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
            if (rs.next()) {
                casilla = rs.getInt("casilla");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return casilla;
    }

    // 5. ACTUALIZAR POSICIÓN + GESTIÓN DE POZO/CASTIGOS
    public boolean actualizarPosicion(int idPartida, int idJugador, int nuevaCasilla) {
        // SQL para actualizar mi posición y mis turnos de castigo
        String sqlUpdateYo = "UPDATE partidas_jugadores SET casilla = ?, turnos_castigo = ? WHERE id_partida = ? AND id_jugador = ?";
        // SQL para rescatar a otros del pozo
        String sqlRescate = "UPDATE partidas_jugadores SET turnos_castigo = 0 WHERE id_partida = ? AND casilla = ? AND id_jugador != ?";

        try (Connection conn = Conexion.getConexion()) {
            
            // 1. Calcular Castigo según Reglas
            int turnosCastigo = ReglasOca.getTurnosCastigo(nuevaCasilla);

            // 2. Lógica Especial del POZO (31)
            if (nuevaCasilla == ReglasOca.POZO) {
                // Intentamos rescatar a cualquiera que esté atrapado en el pozo (menos yo)
                try (PreparedStatement psRescate = conn.prepareStatement(sqlRescate)) {
                    psRescate.setInt(1, idPartida);
                    psRescate.setInt(2, ReglasOca.POZO);
                    psRescate.setInt(3, idJugador);
                    psRescate.executeUpdate(); 
                }
                // Yo me quedo atrapado (-1)
                turnosCastigo = -1;
            }

            // 3. Actualizar mi ficha
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateYo)) {
                ps.setInt(1, nuevaCasilla);
                ps.setInt(2, turnosCastigo);
                ps.setInt(3, idPartida);
                ps.setInt(4, idJugador);
                
                int filas = ps.executeUpdate();
                return filas > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 6. PASAR TURNO AL SIGUIENTE
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
            // Paso 1
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

            // Paso 2
            int siguienteOrden = ordenActual + 1;
            if (siguienteOrden > totalJugadores) siguienteOrden = 1;

            // Paso 3
            int idSiguienteJugador = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlBuscarSiguiente)) {
                ps.setInt(1, idPartida);
                ps.setInt(2, siguienteOrden);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) idSiguienteJugador = rs.getInt("id_jugador");
            }

            // Paso 4
            if (idSiguienteJugador > 0) {
                try (PreparedStatement ps = conn.prepareStatement(sqlActualizar)) {
                    ps.setInt(1, idSiguienteJugador);
                    ps.setInt(2, idPartida);
                    ps.executeUpdate();
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 7. GESTIÓN DE CASTIGOS (Consultar y Restar turno)
    public int gestionarTurnosCastigo(int idPartida, int idJugador) {
        String sqlSelect = "SELECT turnos_castigo FROM partidas_jugadores WHERE id_partida = ? AND id_jugador = ?";
        String sqlUpdate = "UPDATE partidas_jugadores SET turnos_castigo = turnos_castigo - 1 WHERE id_partida = ? AND id_jugador = ? AND turnos_castigo > 0";
        
        try (Connection conn = Conexion.getConexion()) {
            // 1. Consultar estado actual
            int turnosRestantes = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlSelect)) {
                ps.setInt(1, idPartida);
                ps.setInt(2, idJugador);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    turnosRestantes = rs.getInt("turnos_castigo");
                }
            }
            
            // 2. Lógica del POZO (-1)
            if (turnosRestantes == -1) {
                return -1; // Sigues atrapado, no restamos nada
            }

            // 3. Lógica Normal (Posada/Cárcel)
            if (turnosRestantes > 0) {
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, idPartida);
                    ps.setInt(2, idJugador);
                    ps.executeUpdate();
                }
                return turnosRestantes - 1; // Devolvemos lo que queda
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0; // Libre
    }

    // 8. OBTENER POSICIONES (Para pintar el tablero)
    public List<com.oca.modelo.PartidaJugador> obtenerPosiciones(int idPartida) {
        List<com.oca.modelo.PartidaJugador> lista = new ArrayList<>();
        String sql = "SELECT * FROM partidas_jugadores WHERE id_partida = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idPartida);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                com.oca.modelo.PartidaJugador pj = new com.oca.modelo.PartidaJugador();
                pj.setIdPartida(rs.getInt("id_partida"));
                pj.setIdJugador(rs.getInt("id_jugador"));
                pj.setCasilla(rs.getInt("casilla"));
                pj.setOrden(rs.getInt("orden"));
                pj.setTurnosCastigo(rs.getInt("turnos_castigo")); 
                lista.add(pj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 9. TERMINAR LA PARTIDA
    public void terminarPartida(int idPartida, int idGanador) {
        String sql = "UPDATE partidas SET estado = 'TERMINADA', id_ganador = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGanador);
            ps.setInt(2, idPartida);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 

    // 10. OBTENER DE QUIÉN ES EL TURNO ACTUAL
    public int getTurnoActual(int idPartida) {
        String sql = "SELECT id_turno_actual FROM partidas WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idPartida);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id_turno_actual");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; // Si falla o no encuentra
    }

    // 11. ELIMINAR PARTIDA
    public boolean eliminarPartida(int idPartida) {
        String sqlJugadores = "DELETE FROM partidas_jugadores WHERE id_partida = ?";
        String sqlPartida = "DELETE FROM partidas WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            conn.setAutoCommit(false); 
            
            try (PreparedStatement ps1 = conn.prepareStatement(sqlJugadores)) {
                ps1.setInt(1, idPartida);
                ps1.executeUpdate();
            }
            
            try (PreparedStatement ps2 = conn.prepareStatement(sqlPartida)) {
                ps2.setInt(1, idPartida);
                int filas = ps2.executeUpdate();
                
                if (filas > 0) {
                    conn.commit(); 
                    return true;
                }
            }
            
            conn.rollback();
            return false;
            
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // 12. INICIAR PARTIDA
    public boolean iniciarPartida(int idPartida) {
        String sqlCount = "SELECT COUNT(*) FROM partidas_jugadores WHERE id_partida = ?";
        String sqlUpdate = "UPDATE partidas SET estado = 'JUGANDO' WHERE id = ?";
        
        try (Connection conn = Conexion.getConexion()) {
            
            int total = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlCount)) {
                ps.setInt(1, idPartida);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) total = rs.getInt(1);
            }
            
            if (total >= 2 && total <= 4) {
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, idPartida);
                    int filas = ps.executeUpdate();
                    return filas > 0;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 13. OBTENER ESTADO DE LA PARTIDA
    public String getEstadoPartida(int idPartida) {
        String sql = "SELECT estado FROM partidas WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idPartida);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("estado");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // 14. OBTENER NOMBRE DEL JUGADOR
    public String getNombreJugador(int idJugador) {
        String sql = "SELECT nombre FROM jugadores WHERE id = ?"; 
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idJugador);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("nombre");
        } catch (Exception e) { e.printStackTrace(); }
        return "Jugador " + idJugador; // Fallback
    }

    // 15. OBTENER RANKING
    public List<String[]> obtenerRanking() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT nombre, partidas_ganadas " +
                     "FROM jugadores " +
                     "WHERE partidas_ganadas > 0 " +
                     "ORDER BY partidas_ganadas DESC " +
                     "LIMIT 10";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                String victorias = String.valueOf(rs.getInt("partidas_ganadas"));
                lista.add(new String[]{nombre, victorias});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}