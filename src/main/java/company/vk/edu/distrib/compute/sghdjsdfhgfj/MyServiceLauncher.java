package company.vk.edu.distrib.compute.sghdjsdfhgfj;

import company.vk.edu.distrib.compute.sghdjsdfhgfj.kv.MyKVServiceFactory;
import company.vk.edu.distrib.compute.sghdjsdfhgfj.kv.MyShardedRemoteDaoFactory;
import company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener.MyUrlShortenerServiceFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class MyServiceLauncher {
    private static final int DEFAULT_PORT = 8080;

    void main(String... args) throws IOException {
        var port = getPort();

        var kvServiceFactory = new MyKVServiceFactory();
        var urlShortenerServiceFactory = new MyUrlShortenerServiceFactory();
        var shardedRemoteDaoFactory = new MyShardedRemoteDaoFactory();

        var kvServiceCount = 8;
        var kvServicePorts = new int[kvServiceCount];
        for (int i = 0; i < kvServiceCount; i++) {
            kvServicePorts[i] = randomPort(port);
            var kvService = kvServiceFactory.create(kvServicePorts[i]);
            Runtime.getRuntime().addShutdownHook(new Thread(kvService::stop));
            kvService.start();
        }

        var service = urlShortenerServiceFactory.create(port);
        service.setLinksDao(shardedRemoteDaoFactory.create(kvServicePorts));
        Runtime.getRuntime().addShutdownHook(new Thread(service::stop));
        service.start();
    }

    private static int getPort() {
        var envPort = System.getenv("SERVICE_PORT");
        if (envPort == null || envPort.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(envPort);
        } catch (NumberFormatException e) {
            return DEFAULT_PORT;
        }
    }

    public static int randomPort(int... excludes) {
        Arrays.sort(excludes);
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 100_000; i++) {
                final var port = ThreadLocalRandom.current().nextInt(10000, 60000);
                if (Arrays.binarySearch(excludes, port) < 0 && isTcpPortAvailable(port)) {
                    return port;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Interrupted while looking for available port", e);
            }
        }
        throw new IllegalStateException("Can't find available port");
    }

    public static boolean isTcpPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(false);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), port), 1);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
