package com.oca.modelo;

public class Partida {
    private int idPartida;
    private String nombre; // Nombre de la sala, ej: "Sala 1"
    private int idEstado;  // 1=No iniciada, 2=En juego...

    public Partida() {}

    public Partida(String nombre) {
        this.nombre = nombre;
        this.idEstado = 1; // Por defecto nace "No iniciada"
    }

    public Partida(int idPartida, String nombre, int idEstado) {
        this.idPartida = idPartida;
        this.nombre = nombre;
        this.idEstado = idEstado;
    }

    public int getIdPartida() { return idPartida; }
    public void setIdPartida(int idPartida) { this.idPartida = idPartida; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getIdEstado() { return idEstado; }
    public void setIdEstado(int idEstado) { this.idEstado = idEstado; }
}