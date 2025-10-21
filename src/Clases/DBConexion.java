package Clases;

import java.sql.*;

public class DBConexion {
    
   static String url="jdbc:mysql://localhost:3306/inventario_ferre";
   static String usser="root";
   static String pass="";
    
    public static Connection conectar(){
        Connection con=null;
        
        try {
            con=DriverManager.getConnection(url,usser,pass);
            System.out.println(" Conexión establecida con la base de datos");
            
        } catch (SQLException e) {
            
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        
        return con;
    }
}
