package com.oca.modelo;

import java.sql.Timestamp;

public class Partida {
    private int id;
    private String nombre;
    private String estado;      // Ahora es String: "ESPERANDO", "JUGANDO", "TERMINADA"
    private int idTurnoActual;  // ID del jugador al que le toca
    private int idGanador;      // ID del ganador
    private Timestamp fechaCreacion;
    private int jugadoresActuales;

    // Constructor vacío
    public Partida() {}

    // Constructor básico para crear partida nueva
    public Partida(String nombre) {
        this.nombre = nombre;
        this.estado = "ESPERANDO"; // Valor por defecto
    }

    // Constructor completo
    public Partida(int id, String nombre, String estado, int idTurnoActual, int idGanador, Timestamp fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.estado = estado;
        this.idTurnoActual = idTurnoActual;
        this.idGanador = idGanador;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getIdTurnoActual() { return idTurnoActual; }
    public void setIdTurnoActual(int idTurnoActual) { this.idTurnoActual = idTurnoActual; }

    public int getIdGanador() { return idGanador; }
    public void setIdGanador(int idGanador) { this.idGanador = idGanador; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public int getJugadoresActuales() {
    return jugadoresActuales;
    }

    public void setJugadoresActuales(int jugadoresActuales) {
    this.jugadoresActuales = jugadoresActuales;
    }
}