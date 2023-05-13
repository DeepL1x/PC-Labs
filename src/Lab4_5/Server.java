package Lab4_5;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Server {
    private ServerSocket serverSocket;

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                (new Thread(new ClientHandler(clientSocket))).start();
                System.out.println("New client " + clientSocket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private ExecutorService executor;
        private boolean ready;
        private final Object offSignal = new Object();
        private Integer min, max;

        ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        private void shutdown() {
            executor.shutdown();
        }

        private void process(ArrayList<ArrayList<Integer>> matrix, int threadCount, int m, int n) {
            executor = Executors.newFixedThreadPool(threadCount);
            List<FutureSearch> tasks = new ArrayList<>();
            CompletionService<Integer[]> completionService = new ExecutorCompletionService<>(executor);

            for (int i = 0; i < threadCount; i++) {
                if (m > n) {
                    double step = (double) m / threadCount;
                    tasks.add(
                            new FutureSearch(
                                    matrix,
                                    false,
                                    (int) (i * step),
                                    (int) ((i + 1) * step)));
                } else {
                    double step = (double) n / threadCount;
                    tasks.add(
                            new FutureSearch(
                                    matrix,
                                    true,
                                    (int) (i * step),
                                    (int) ((i + 1) * step)));
                }
            }
            try {
                for (int i = 0; i < threadCount; i++) {
                    completionService.submit(tasks.get(i));
                }
                min = matrix.get(0).get(0);
                max = matrix.get(0).get(0);
                for (int i = 0; i < threadCount; i++) {
                    Future<Integer[]> completedFuture = completionService.take();
                    Integer[] res = completedFuture.get();
                    if (res[0] <= min)
                        min = res[0];
                    else if (res[1] >= max)
                        max = res[1];
                }
                ready = true;
                synchronized (offSignal) {
                    offSignal.notify();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                ready = false;
                ArrayList<ArrayList<Integer>> matrix = new ArrayList<>();
                String inputLine;
                int m = 1, n = 1, t = 4;
                boolean shutdown = false;
                String options = """
                        Your options:
                        1. Set matrix dimensions (only integers): m n t
                        2. Send matrix data inline
                        3. Launch processing
                        4. Check status
                        5. Wait for completion, get result and shutdown
                        6. Shutdown immediately
                        7. Show options""";
                dos.writeUTF(options);
                while (!shutdown) {
                    inputLine = dis.readUTF();
                    switch (inputLine) {
                        case "1" -> {
                            ready = false;
                            dos.writeUTF("Enter configuration (only integers): m n t");
                            String[] conf = dis.readUTF().split(" ");
                            m = Integer.parseInt(conf[0]);
                            n = Integer.parseInt(conf[1]);
                            t = Integer.parseInt(conf[2]);
                            dos.writeUTF("Accepted configuration: m=" + m + "; n=" + n + "; t=" + t);
                        }
                        case "2" -> {
                            ready = false;
                            matrix.clear();
                            dos.writeUTF("Enter matrix data");
                            int j = 0;
                            int size = dis.readInt();
                            System.out.println("Array size: " + size);
                            for (int i = 0; i < size; i++) {
                                if ((i) % n == 0) {
                                    matrix.add(new ArrayList<>());
                                    if (i != 0)
                                        j++;
                                }
                                matrix.get(j).add(dis.readInt());
                            }
                            dos.writeUTF("Accepted matrix[0][0]: " + matrix.get(0).get(0));
                        }
                        case "3" -> {
                            if (matrix.size() == m && matrix.get(0).size() == n) {
                                dos.writeUTF("Processing started");
                                int finalN = n;
                                int finalM = m;
                                int finalT = t;
                                (new Thread(() -> process(matrix, finalT, finalM, finalN))).start();
                            } else {
                                dos.writeUTF("Matrix dimensions do not correspond to the specified");
                            }
                        }
                        case "4" -> {
                            if (ready) {
                                dos.writeUTF("Processing done. min: " + min + "; max: " + max);
                            } else
                                dos.writeUTF("Still processing...");
                        }
                        case "5" -> {
                            synchronized (this) {
                                while (!ready)
                                    wait();
                            }
                            dos.writeUTF("Shutting down. min: " + min + "; max: " + max);
                            shutdown = true;
                        }
                        case "6" -> {
                            shutdown = true;
                            shutdown();
                            dos.writeUTF("Shutting down.");
                        }
                        case "7" -> dos.writeUTF(options);
                        default -> dos.writeUTF("Unknown option");
                    }
                }
                dis.close();
                dos.close();
                clientSocket.close();
                System.out.println("Client finished " + clientSocket);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();

            }
        }
    }


}