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
        String sql = "SELECT p.*, (SELECT COUNT(*) FROM partidas_jugadores WHERE id_partida = p.id) as total_jugadores " +
             "FROM partidas p WHERE p.estado = 'ESPERANDO'";

        // Aquí usamos try-with-resources igual que en tu JugadorDAO
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Partida p = new Partida();
                p.setJugadoresActuales(rs.getInt("total_jugadores"));
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
    // Método para unirse a una partida existente
    // Método para unirse a una partida existente (MODIFICADO CON ORDEN)
    // Método para unirse (o re-entrar) a una partida
    public boolean unirseAPartida(int idPartida, int idJugador) {
        Connection conn = null;
        boolean exito = false;

        try {
            conn = Conexion.getConexion();
            
            // 0. ¡NUEVO! COMPROBAR SI YA ESTOY DENTRO
            // Si eres el creador o ya te uniste antes, esto devolverá 1
            String sqlYaEstoy = "SELECT COUNT(*) FROM partidas_jugadores WHERE id_partida = ? AND id_jugador = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlYaEstoy)) {
                ps.setInt(1, idPartida);
                ps.setInt(2, idJugador);
                ResultSet rsYa = ps.executeQuery();
                if (rsYa.next() && rsYa.getInt(1) > 0) {
                     // Ya estás dentro: ¡Te dejamos pasar!
                     return true; 
                }
            }

            // 1. Si no estoy dentro, verificamos si hay hueco
            String sqlVerificar = "SELECT COUNT(*) FROM partidas_jugadores WHERE id_partida = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlVerificar)) {
                ps.setInt(1, idPartida);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    int jugadoresActuales = rs.getInt(1);
                    
                    if (jugadoresActuales < 4) {
                        // 2. Insertamos al jugador CON SU ORDEN
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
    // 3. OBTENER CASILLA ACTUAL DE UN JUGADOR
    public int getCasillaActual(int idPartida, int idJugador) {
        int casilla = 1; // Por defecto empezamos en la 1
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
    // 4. ACTUALIZAR POSICIÓN DEL JUGADOR
    public boolean actualizarPosicion(int idPartida, int idJugador, int nuevaCasilla) {
        String sql = "UPDATE partidas_jugadores SET casilla = ? WHERE id_partida = ? AND id_jugador = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, nuevaCasilla);
            ps.setInt(2, idPartida);
            ps.setInt(3, idJugador);
            
            int filas = ps.executeUpdate();
            return filas > 0; // Devuelve true si se actualizó bien
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // 5. PASAR TURNO AL SIGUIENTE JUGADOR
    public boolean pasarTurno(int idPartida) {
        // Lógica:
        // 1. Averiguar quién tiene el turno ahora y cuál es su orden.
        // 2. Calcular el siguiente orden (si es el 4, vuelve al 1).
        // 3. Buscar qué jugador tiene ese nuevo orden.
        // 4. Actualizar la partida con el ID del nuevo jugador.

        String sqlInfoActual = 
            "SELECT p.id_turno_actual, pj.orden, " +
            "(SELECT COUNT(*) FROM partidas_jugadores WHERE id_partida = p.id) as total " +
            "FROM partidas p " +
            "JOIN partidas_jugadores pj ON p.id = pj.id_partida AND p.id_turno_actual = pj.id_jugador " +
            "WHERE p.id = ?";
            
        String sqlBuscarSiguiente = "SELECT id_jugador FROM partidas_jugadores WHERE id_partida = ? AND orden = ?";
        String sqlActualizar = "UPDATE partidas SET id_turno_actual = ? WHERE id = ?";

        try (Connection conn = Conexion.getConexion()) {
            
            // PASO 1: Obtener datos actuales
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

            if (totalJugadores == 0) return false; // Algo fue mal

            // PASO 2: Calcular siguiente orden (Rotativo)
            int siguienteOrden = ordenActual + 1;
            if (siguienteOrden > totalJugadores) {
                siguienteOrden = 1; // Volver al principio
            }

            // PASO 3: ¿Quién es el jugador con ese orden?
            int idSiguienteJugador = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlBuscarSiguiente)) {
                ps.setInt(1, idPartida);
                ps.setInt(2, siguienteOrden);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    idSiguienteJugador = rs.getInt("id_jugador");
                }
            }

            // PASO 4: Darle el turno
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
    // 6. GESTIÓN DE CASTIGOS
    // Sirve para consultar si estoy castigado y para reducir el castigo cada vez que intento tirar
    public int gestionarTurnosCastigo(int idPartida, int idJugador) {
        String sqlSelect = "SELECT turnos_castigo FROM partidas_jugadores WHERE id_partida = ? AND id_jugador = ?";
        String sqlUpdate = "UPDATE partidas_jugadores SET turnos_castigo = turnos_castigo - 1 WHERE id_partida = ? AND id_jugador = ? AND turnos_castigo > 0";
        
        int turnosRestantes = 0;
        
        try (Connection conn = Conexion.getConexion()) {
            // 1. Miramos cuántos turnos de castigo tiene
            try (PreparedStatement ps = conn.prepareStatement(sqlSelect)) {
                ps.setInt(1, idPartida);
                ps.setInt(2, idJugador);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    turnosRestantes = rs.getInt("turnos_castigo");
                }
            }
            
            // 2. Si tiene castigo, le restamos 1 (porque "ha gastado" un turno intentando tirar)
            if (turnosRestantes > 0) {
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, idPartida);
                    ps.setInt(2, idJugador);
                    ps.executeUpdate();
                }
                // Devolvemos cuántos le quedan AHORA (para decirle: "Te quedan X turnos")
                return turnosRestantes - 1; 
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0; // 0 significa "No estás castigado, puedes jugar"
    }

    // 7. APLICAR NUEVO CASTIGO (Cuando cae en Posada/Cárcel)
    public void aplicarCastigo(int idPartida, int idJugador, int turnos) {
        String sql = "UPDATE partidas_jugadores SET turnos_castigo = ? WHERE id_partida = ? AND id_jugador = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, turnos);
            ps.setInt(2, idPartida);
            ps.setInt(3, idJugador);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 8. OBTENER POSICIONES DE TODOS LOS JUGADORES
    // Este método devuelve la lista para que el tablero sepa dónde pintar las fichas
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
}