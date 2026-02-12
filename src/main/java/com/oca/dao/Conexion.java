package com.oca.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // DATOS DE CONEXIÓN
    // Si existen variables de entorno (en AWS), las usa. Si no (en tu PC), usa los valores por defecto "localhost".
    private static final String DB_HOST = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private static final String DB_PORT = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";
    private static final String DB_NAME = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "proyecto_oca";
    private static final String DB_USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static final String DB_PASS = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : "";

    private static final String URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    public static Connection getConexion() {
        Connection cn = null;
        try {
            // 1. Cargamos el driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 2. Establecemos la conexión
            cn = DriverManager.getConnection(URL, DB_USER, DB_PASS);
            
            // Solo para depuración (quitar en producción si se desea)
            System.out.println("✅ Intentando conectar a: " + URL);

        } catch (ClassNotFoundException e) {
            System.out.println("❌ Error: No se encuentra el Driver de MySQL.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Error SQL conectando a: " + URL);
            e.printStackTrace();
        }
        return cn;
    }
}