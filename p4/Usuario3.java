import java.io.*;
import java.net.*;

public class Usuario3 {
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 1024;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVIDOR, PUERTO);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado al chat.");

            Thread lector = new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = entrada.readLine()) != null) {
                        System.out.println(mensaje);
                    }
                } catch (IOException e) {
                    System.out.println("Desconectado del servidor.");
                }
            });
            lector.start();

            String mensaje;
            while ((mensaje = teclado.readLine()) != null) {
                salida.println(mensaje);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
