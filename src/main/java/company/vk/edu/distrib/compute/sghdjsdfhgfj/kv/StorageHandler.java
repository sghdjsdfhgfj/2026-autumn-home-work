package company.vk.edu.distrib.compute.sghdjsdfhgfj.kv;

import com.sun.net.httpserver.HttpExchange;
import company.vk.edu.distrib.compute.sghdjsdfhgfj.CustomHttpHandler;
import company.vk.edu.distrib.compute.sghdjsdfhgfj.PersistentDao;
import company.vk.edu.distrib.compute.sghdjsdfhgfj.StatusCodeException;

import java.io.IOException;

class StorageHandler implements CustomHttpHandler {
    private final PersistentDao storage;
    private static final int PATH_DEPTH = 3;

    public StorageHandler(PersistentDao storage) {
        this.storage = storage;
    }

    @Override
    public void handleGet(HttpExchange xch) throws IOException, StatusCodeException {
        String id = getId(xch);
        if (storage.containsKey(id)) {
            byte[] value = storage.get(id);
            xch.sendResponseHeaders(200, 0);
            xch.getResponseBody().write(value);
        } else {
            throw StatusCodeException.notFound();
        }
    }

    @Override
    public void handlePut(HttpExchange xch) throws IOException, StatusCodeException {
        String id = getId(xch);
        byte[] value = xch.getRequestBody().readAllBytes();
        storage.upsert(id, value);
        xch.sendResponseHeaders(201, 0);
    }

    @Override
    public void handleDelete(HttpExchange xch) throws IOException, StatusCodeException {
        String id = getId(xch);
        storage.delete(id);
        xch.sendResponseHeaders(202, 0);
    }

    private String getId(HttpExchange xch) throws StatusCodeException {
        String[] separatedPath = xch.getRequestURI().getPath().split("/");
        if (separatedPath.length <= PATH_DEPTH) {
            throw StatusCodeException.badRequest();
        }
        String id = separatedPath[PATH_DEPTH];
        if (id.isEmpty()) {
            throw StatusCodeException.badRequest();
        }
        return id;
    }
}
