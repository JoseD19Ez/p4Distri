import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor3 {
    private static final int PUERTO_CLIENTES = 1026;
    private static final int PUERTO_SERVIDORES = 3089;
    private static Set<PrintWriter> clientes = Collections.synchronizedSet(new HashSet<>());
    private static Set<PrintWriter> otrosServidores = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Servidor C iniciado en puerto " + PUERTO_SERVIDORES);

        // Hilo para aceptar clientes normales
        new Thread(() -> {
            try (ServerSocket servidorClientes = new ServerSocket(PUERTO_CLIENTES)) {
                while (true) {
                    Socket socket = servidorClientes.accept();
                    new ManejadorCliente(socket).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Hilo para aceptar conexiones de otros servidores
        new Thread(() -> {
            try (ServerSocket servidorDeServidores = new ServerSocket(PUERTO_SERVIDORES)) {
                while (true) {
                    Socket socket = servidorDeServidores.accept();
                    new ManejadorServidor(socket).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Conexión a Servidor A
        conectarAServidores("localhost", 3088);
    }

    private static void conectarAServidores(String host, int puerto) {
        try {
            Socket socket = new Socket(host, puerto);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            otrosServidores.add(salida);
            new ManejadorServidor(socket).start(); // Escuchar respuestas
            System.out.println("Conectado al servidor en puerto " + puerto);
        } catch (IOException e) {
            System.out.println("No se pudo conectar a " + host + ":" + puerto);
        }
    }

    private static void reenviarATodos(String mensaje, boolean reenviarAServidores) {
        synchronized (clientes) {
            for (PrintWriter cliente : clientes) {
                cliente.println(mensaje);
            }
        }
        if (reenviarAServidores) {
            synchronized (otrosServidores) {
                for (PrintWriter servidor : otrosServidores) {
                    servidor.println(mensaje);
                }
            }
        }
    }

    private static class ManejadorCliente extends Thread {
        private Socket socket;
        private PrintWriter salida;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                salida = new PrintWriter(socket.getOutputStream(), true);
                clientes.add(salida);

                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    System.out.println("Mensaje recibido de cliente: " + mensaje);
                    reenviarATodos(mensaje, true);
                }
            } catch (IOException e) {
                System.out.println("Cliente desconectado");
            } finally {
                clientes.remove(salida);
                try {
                    socket.close();
                } catch (IOException e) {}
            }
        }
    }

    private static class ManejadorServidor extends Thread {
        private Socket socket;

        public ManejadorServidor(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)
            ) {
                otrosServidores.add(salida); // 🔧 CLAVE: para que pueda responder
                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    System.out.println("Mensaje recibido de otro servidor: " + mensaje);
                    reenviarATodos(mensaje, false);
                }
            } catch (IOException e) {
                System.out.println("Servidor desconectado");
            }
        }
    }
}
