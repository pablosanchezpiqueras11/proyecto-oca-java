package com.oca.modelo;

public class Jugador {

    private int id;
    private String nombre;
    private String password;
    private int partidasGanadas;

    // Constructor vacío (necesario para algunas herramientas)
    public Jugador() {
    }

    // Constructor para crear un jugador nuevo
    public Jugador(String nombre, String password) {
        this.nombre = nombre;
        this.password = password;
        this.partidasGanadas = 0;
    }

    // Constructor completo (cuando lo leemos de la base de datos con su ID)
    public Jugador(int id, String nombre, String password, int partidasGanadas) {
        this.id = id;
        this.nombre = nombre;
        this.password = password;
        this.partidasGanadas = partidasGanadas;
    }

    // Getters y Setters (Para poder leer y escribir los datos)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getPartidasGanadas() { return partidasGanadas; }
    public void setPartidasGanadas(int partidasGanadas) { this.partidasGanadas = partidasGanadas; }
}