package com.oca.logica;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstadoPartida {

    private int idPartida;
    
    // Lista ordenada con los IDs de los jugadores en la partida
    private List<Integer> ordenJugadores;
    
    // Índice en la lista 'ordenJugadores' que indica a quién le toca (0, 1, 2...)
    private int indiceTurnoActual; 
    
    // Mapa: ID Jugador -> Número de Casilla
    private Map<Integer, Integer> posiciones;
    
    // Mapa: ID Jugador -> Turnos de castigo restantes (0 si puede jugar)
    private Map<Integer, Integer> castigos;

    private boolean partidaTerminada;
    private int idGanador;

    // Constructor: Inicializa la partida con los jugadores en la casilla 1
    public EstadoPartida(int idPartida, List<Integer> jugadoresIds) {
        this.idPartida = idPartida;
        this.ordenJugadores = jugadoresIds;
        this.indiceTurnoActual = 0; // Empieza el primero de la lista
        this.partidaTerminada = false;
        this.idGanador = -1;

        this.posiciones = new HashMap<>();
        this.castigos = new HashMap<>();

        // Todos empiezan en la casilla 1 y sin castigos
        for (Integer id : jugadoresIds) {
            posiciones.put(id, 1);
            castigos.put(id, 0);
        }
    }

    // --- GESTIÓN DE TURNOS ---

    public int getIdJugadorActual() {
        if (ordenJugadores == null || ordenJugadores.isEmpty()) return -1;
        return ordenJugadores.get(indiceTurnoActual);
    }

    public void avanzarTurno() {
        if (partidaTerminada) return;

        // Pasamos al siguiente índice circularmente
        indiceTurnoActual = (indiceTurnoActual + 1) % ordenJugadores.size();
        
        // Verificar si el siguiente jugador está castigado
        int idSiguiente = getIdJugadorActual();
        int turnosPendientes = castigos.getOrDefault(idSiguiente, 0);

        if (turnosPendientes > 0) {
            // Reducimos el castigo y pasamos turno automáticamente
            castigos.put(idSiguiente, turnosPendientes - 1);
            // Recursividad: intentamos pasar al siguiente de nuevo
            avanzarTurno(); 
        }
    }

    // --- GESTIÓN DE MOVIMIENTO ---

    public int getPosicionJugador(int idJugador) {
        return posiciones.getOrDefault(idJugador, 1);
    }

    public void actualizarPosicion(int idJugador, int nuevaCasilla) {
        posiciones.put(idJugador, nuevaCasilla);
        
        if (nuevaCasilla == 63) {
            partidaTerminada = true;
            idGanador = idJugador;
        }
    }

    // --- GESTIÓN DE CASTIGOS ---

    public void aplicarCastigo(int idJugador, int turnos) {
        castigos.put(idJugador, turnos);
    }

    // --- GETTERS ---

    public boolean isPartidaTerminada() { return partidaTerminada; }
    public int getIdGanador() { return idGanador; }
    public List<Integer> getOrdenJugadores() { return ordenJugadores; }
}