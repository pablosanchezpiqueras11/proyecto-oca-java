package com.oca.modelo;

public class PartidaJugador {
    private int idPartida;
    private int idJugador;
    private int casilla;
    private int orden; // 1, 2, 3 o 4 (para saber el turno)
    private int turnosCastigo;

    public PartidaJugador() {}

    public PartidaJugador(int idPartida, int idJugador, int casilla, int orden) {
        this.idPartida = idPartida;
        this.idJugador = idJugador;
        this.casilla = casilla;
        this.orden = orden;
    }

    // Getters y Setters
    public int getIdPartida() { return idPartida; }
    public void setIdPartida(int idPartida) { this.idPartida = idPartida; }

    public int getIdJugador() { return idJugador; }
    public void setIdJugador(int idJugador) { this.idJugador = idJugador; }

    public int getCasilla() { return casilla; }
    public void setCasilla(int casilla) { this.casilla = casilla; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public int getTurnosCastigo() { return turnosCastigo; }
    public void setTurnosCastigo(int turnosCastigo) { this.turnosCastigo = turnosCastigo; }
}