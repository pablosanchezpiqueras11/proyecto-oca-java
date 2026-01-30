package com.oca.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // DATOS DE CONEXIÓN
    // Nota: Cuando paséis esto a la Máquina Virtual, tendréis que cambiar esto
    // por la IP de la máquina y la contraseña real.
    private static final String CONTROLADOR = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/proyecto_oca?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";   // Usuario por defecto en local
    private static final String PASSWORD = "";      // Contraseña (suele ser vacía o 'root' en local)

    // Método para conectar
    public static Connection getConexion() {
        Connection cn = null;
        try {
            // 1. Cargamos el driver
            Class.forName(CONTROLADOR);
            
            // 2. Intentamos conectar
            cn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            System.out.println("✅ ¡Conexión a Base de Datos exitosa!");
            
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Error: No se encontró el Driver de MySQL");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Error: Fallo al conectar con la BD (Revisa usuario/pass)");
            e.printStackTrace();
        }
        return cn;
    }
    
    // Método para cerrar la conexión (importante para no saturar el servidor)
    public static void cerrarConexion(Connection cn) {
        try {
            if (cn != null && !cn.isClosed()) {
                cn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}