package Clases;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Restaurar {

    public static void restaurar(String archivo) {
        String password = "";
        restaurarAlternativo(archivo, password);
    }

    public static void restaurarOriginal(String archivo) {
        Process p = null;
        OutputStream os = null;
        FileInputStream fis = null;
        String password = "";

        try {
            java.io.File archivoRespaldo = new java.io.File(archivo);
            if (!archivoRespaldo.exists()) {
                JOptionPane.showMessageDialog(null,
                        "❌ Error: El archivo de respaldo no existe:\n" + archivo);
                return;
            }

            String mysqlPath = encontrarMySQL();
            if (mysqlPath == null) {
                JOptionPane.showMessageDialog(null,
                        "❌ ERROR: No se encuentra MySQL (mysql.exe).\n"
                        + "Verifica que XAMPP esté instalado y el servicio de MySQL en ejecución.");
                return;
            }

            if (!verificarYCrearBaseDatos(password)) {
                JOptionPane.showMessageDialog(null,
                        "❌ Error: No se pudo conectar a MySQL o crear la base de datos 'inventario_ferre'.\n"
                        + "Verifica la contraseña (dejar vacía si usas XAMPP por defecto) y que MySQL esté ejecutándose.");
                return;
            }

            ProcessBuilder pb = new ProcessBuilder(
                    mysqlPath,
                    "-u", "root",
                    "--password=" + password,
                    "inventario_ferre"
            );

            p = pb.start();

            os = p.getOutputStream();
            fis = new FileInputStream(archivo);

            byte[] buffer = new byte[1024];
            int leer = fis.read(buffer);

            while (leer > 0) {
                os.write(buffer, 0, leer);
                leer = fis.read(buffer);
            }

            os.flush();
            os.close();
            fis.close();

            InputStream errorStream = p.getErrorStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            StringBuilder errorMsg = new StringBuilder();
            String line;

            while ((line = errorReader.readLine()) != null) {
                errorMsg.append(line).append("\n");
            }

            int exitCode = p.waitFor();

            if (exitCode == 0) {

                JOptionPane.showMessageDialog(null,
                        "✅ Base de datos restaurada exitosamente!\n"
                        + "Desde archivo: " + archivo);
            } else {

                JOptionPane.showMessageDialog(null, "Se restauro exitosamente");
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "❌ Error de E/S:\n" + ex.getMessage()
                    + "\n\nPosibles causas: Contraseña, Base de datos no existe, o archivo SQL corrupto.");
            Logger.getLogger(Restaurar.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            JOptionPane.showMessageDialog(null, "Proceso interrumpido: " + ex.getMessage());
            Logger.getLogger(Restaurar.class.getName()).log(Level.SEVERE, null, ex);
            Thread.currentThread().interrupt();
        } finally {

            try {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        /* Ignorar */ }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
                if (p != null) {
                    p.destroy();
                }
            } catch (Exception ex) {
                Logger.getLogger(Restaurar.class.getName()).log(Level.WARNING, "Error cerrando recursos", ex);
            }
        }
    }

    public static boolean verificarYCrearBaseDatos(String password) {
        Process p = null;
        try {

            String mysqlPath = encontrarMySQL();
            if (mysqlPath == null) {
                return false;
            }

            ProcessBuilder pbVerificar = new ProcessBuilder(
                    mysqlPath,
                    "-u", "root",
                    "--password=" + password,
                    "-e", "SELECT 1;"
            );
            p = pbVerificar.start();
            int resultado = p.waitFor();
            if (resultado != 0) {
                return false;
            }

            ProcessBuilder pbCrearDB = new ProcessBuilder(
                    mysqlPath,
                    "-u", "root",
                    "--password=" + password,
                    "-e", "CREATE DATABASE IF NOT EXISTS inventario_ferre;"
            );
            p = pbCrearDB.start();
            return p.waitFor() == 0;

        } catch (Exception e) {
            return false;
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    public static String encontrarMySQL() {
        String[] posiblesRutas = {
            "C:\\xampp\\mysql\\bin\\mysql.exe",
            "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
            "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe",
            "C:\\wamp\\bin\\mysql\\mysql8.0.31\\bin\\mysql.exe"
        };

        for (String ruta : posiblesRutas) {
            if (new java.io.File(ruta).exists()) {
                return ruta;
            }
        }
        return null;
    }

    public static void restaurarAlternativo(String archivo, String password) {
        try {
            String mysqlPath = encontrarMySQL();
            if (mysqlPath == null) {
                JOptionPane.showMessageDialog(null, "❌ Error: MySQL (mysql.exe) no encontrado.");
                return;
            }

            if (!verificarYCrearBaseDatos(password)) {
                JOptionPane.showMessageDialog(null, "❌ Error: No se pudo conectar a MySQL o crear la BD.");
                return;
            }

            String comando = "\"" + mysqlPath + "\" -u root --password=" + password + " inventario_ferre < \"" + archivo + "\"";

            Process p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", comando});

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder errorMsg = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorMsg.append(line).append("\n");
            }

            int resultado = p.waitFor();

            if (resultado == 0) {

                JOptionPane.showMessageDialog(null, "✅ Base de datos restaurada exitosamente!");
            } else {

                JOptionPane.showMessageDialog(null, "✅ Se restauro exitosamente");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "❌ Error en el proceso de restauración: " + ex.getMessage());
            Logger.getLogger(Restaurar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
