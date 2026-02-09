package com.oca.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // DATOS DE CONEXIÓN
    // Asegúrate de que tu base de datos en phpMyAdmin se llama EXACTAMENTE "proyecto_oca"
    private static final String CONTROLADOR = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/proyecto_oca?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String PASSWORD = ""; 

    public static Connection getConexion() {
        Connection cn = null;
        try {
            // 1. Cargamos el driver
            Class.forName(CONTROLADOR);
            
            // 2. ESTA ES LA LÍNEA CLAVE QUE SUELE FALTAR
            cn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            
            System.out.println("✅ Conexión establecida con éxito.");

        } catch (ClassNotFoundException e) {
            System.out.println("❌ Error: No se encuentra el Driver de MySQL.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Error: Fallo al conectar con la BD. ¿Está encendida? ¿El nombre está bien?");
            e.printStackTrace();
        }
        return cn;
    }
}