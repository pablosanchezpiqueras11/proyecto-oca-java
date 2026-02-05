package com.oca.modelo;

public class PartidaJugador {
    private int idPartida;
    private int idJugador;
    private int casilla;
    private int orden;
    private String nombre; 

    public PartidaJugador() {}

    // Getters y Setters
    public int getIdPartida() { return idPartida; }
    public void setIdPartida(int idPartida) { this.idPartida = idPartida; }

    public int getIdJugador() { return idJugador; }
    public void setIdJugador(int idJugador) { this.idJugador = idJugador; }

    public int getCasilla() { return casilla; }
    public void setCasilla(int casilla) { this.casilla = casilla; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}