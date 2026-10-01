package company.vk.edu.distrib.compute.sghdjsdfhgfj.kv;

import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

public class MyRemoteDao implements Dao<String> {
    private final URI url;

    public MyRemoteDao(int port) throws URISyntaxException {
        url = new URI("http://localhost:" + port);
    }

    @Override
    public String get(String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        HttpURLConnection conn = (HttpURLConnection) url.resolve("/v0/entity/" + key).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        int status = conn.getResponseCode();
        if (status == 200) {
            byte[] contents = conn.getInputStream().readAllBytes();
            return new String(contents);
        } else if (status == 404) {
            throw new NoSuchElementException();
        } else {
            throw new IOException(conn.getResponseMessage());
        }
    }

    @Override
    public void upsert(String key, String value) throws IllegalArgumentException, IOException {
        HttpURLConnection conn = (HttpURLConnection) url.resolve("/v0/entity/" + key).toURL().openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.getOutputStream().write(value.getBytes());
        conn.getOutputStream().flush();
        conn.getOutputStream().close();
        int status = conn.getResponseCode();
        if (status != 201) {
            throw new IOException(conn.getResponseMessage());
        }
    }

    @Override
    public void delete(String key) throws IllegalArgumentException, IOException {
        HttpURLConnection conn = (HttpURLConnection) url.resolve("/v0/entity/" + key).toURL().openConnection();
        conn.setRequestMethod("DELETE");
        int status = conn.getResponseCode();
        if (status != 202) {
            throw new IOException(conn.getResponseMessage());
        }
    }

    @Override
    public void close() throws IOException {

    }
}
