package Clases;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Respaldo {

    public static void respaldo(String ruta) {
        Process p = null;
        FileOutputStream fos = null;
        InputStream is = null;

        try {

            String mysqldumpPath = "C:\\xampp\\mysql\\bin\\mysqldump.exe";

            java.io.File mysqlFile = new java.io.File(mysqldumpPath);

            if (!mysqlFile.exists()) {
                JOptionPane.showMessageDialog(null,
                        "ERROR: No se encuentra mysqldump.exe en la ruta de XAMPP:\n" + mysqldumpPath
                        + "\n\nVerifica:\n"
                        + "1. Que el servidor MySQL de XAMPP esté corriendo.\n"
                        + "2. La ruta (si XAMPP está en otra unidad, debes cambiar 'C:' por la correcta).");
                return;
            }

            ProcessBuilder pb = new ProcessBuilder(
                    mysqldumpPath,
                    "-u", "root",
                    "--password=" + "",
                    "inventario_ferre"
            );

            pb.redirectErrorStream(true);

            p = pb.start();
            is = p.getInputStream();

            String outputFile = ruta.endsWith(".sql") ? ruta : ruta + ".sql";

            java.io.File outputDir = new java.io.File(outputFile).getParentFile();
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs();
            }

            fos = new FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];

            int leer = is.read(buffer);
            while (leer > 0) {
                fos.write(buffer, 0, leer);
                leer = is.read(buffer);
            }

            int exitCode = p.waitFor();

            if (exitCode == 0) {
                JOptionPane.showMessageDialog(null,
                        "✅Respaldo creado exitosamente!\n"
                        + "Ubicación: " + outputFile);
            } else {

                InputStream errorStream = p.getErrorStream();
                StringBuilder errorMsg = new StringBuilder();
                byte[] errorBuffer = new byte[1024];
                int errorRead;
                while ((errorRead = errorStream.read(errorBuffer)) > 0) {
                    errorMsg.append(new String(errorBuffer, 0, errorRead));
                }

                JOptionPane.showMessageDialog(null,
                        "Error en el respaldo:\n"
                        + "Código: " + exitCode
                        + "\nDetalle: " + (errorMsg.length() > 0 ? errorMsg.toString() : "No se obtuvo información del error"));
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error de E/S:\n" + ex.getMessage()
                    + "\n\nVerifica permisos de escritura en la ruta.");
            Logger.getLogger(Respaldo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            JOptionPane.showMessageDialog(null, "Proceso interrumpido: " + ex.getMessage());
            Logger.getLogger(Respaldo.class.getName()).log(Level.SEVERE, null, ex);
            Thread.currentThread().interrupt();
        } finally {

            try {
                if (fos != null) {
                    fos.close();
                }
                if (is != null) {
                    is.close();
                }
                if (p != null) {
                    p.destroy();
                }
            } catch (IOException ex) {
                Logger.getLogger(Respaldo.class.getName()).log(Level.WARNING, "Error cerrando recursos", ex);
            }
        }
    }

    public static String encontrarMySQL() {
        String[] posiblesRutas = {
            "C:\\xampp\\mysql\\bin\\mysqldump.exe",
            "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
            "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysqldump.exe",
            "C:\\wamp\\bin\\mysql\\mysql8.0.31\\bin\\mysqldump.exe"
        };

        for (String ruta : posiblesRutas) {
            if (new java.io.File(ruta).exists()) {
                return ruta;
            }
        }
        return null;
    }

    public static void setText(String realizar_Respaldo_de_BD) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
