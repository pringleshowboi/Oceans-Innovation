package com.framework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.framework.database.DatabaseConnector;
import com.framework.database.ReportDAO;
import com.framework.util.AppProperties;

public class WebFramework {
    private Router router;
    private Middleware middleware;
    private Connection connection;
    private ReportDAO reportDAO;

    public WebFramework() {
        this.router = new Router();
    }

    public void addRoute(String path, Function<HttpRequest, HttpResponse> handler) {
        router.addRoute(path, handler);
    }

    public void setMiddleware(Middleware middleware) {
        this.middleware = middleware;
    }

    public void loadConfiguration(String filePath) throws IOException, SQLException {
        AppProperties properties = new AppProperties(filePath);

        String url = properties.getProperty("database.url");
        String user = properties.getProperty("database.username");
        String password = properties.getProperty("database.password");

        DatabaseConnector connector = new DatabaseConnector(url, user, password);
        this.connection = connector.getConnection();
        this.reportDAO = new ReportDAO(this.connection);
    }

    public void start(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleClientRequest(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClientRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        String line = in.readLine();
        if (line == null || line.isEmpty()) {
            clientSocket.close();
            return;
        }

        String[] requestLine = line.split(" ");
        String method = requestLine[0];
        String path = requestLine[1];

        Map<String, String> headers = new HashMap<>();
        while (!(line = in.readLine()).isEmpty()) {
            String[] header = line.split(": ");
            if (header.length == 2) {
                headers.put(header[0], header[1]);
            }
        }

        StringBuilder body = new StringBuilder();
        while (in.ready()) {
            body.append((char) in.read());
        }

        HttpRequest request = new HttpRequest(method, path, headers, body.toString());
        if (middleware != null) {
            request = middleware.apply(request);
        }

        HttpResponse response = router.handleRequest(request);

        out.write("HTTP/1.1 " + response.getStatusCode() + " OK\r\n");
        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            out.write(header.getKey() + ": " + header.getValue() + "\r\n");
        }
        out.write("\r\n");
        out.write(response.getBody());
        out.flush();

        clientSocket.close();
    }

    public static void main(String[] args) {
        WebFramework app = new WebFramework();

        try {
            app.loadConfiguration("src/main/resources/application.properties");
            System.out.println("Loaded database configuration successfully.");

            app.addRoute("/report", request -> {
                try {
                    String reportData = request.getBody();
                    app.reportDAO.saveReport(reportData);

                    HttpResponse response = new HttpResponse();
                    response.setStatusCode(200);
                    response.setBody("Report received and saved");
                    return response;
                } catch (SQLException e) {
                    e.printStackTrace();
                    HttpResponse response = new HttpResponse();
                    response.setStatusCode(500);
                    response.setBody("Internal Server Error");
                    return response;
                }
            });

            app.addRoute("/reports", request -> {
                try {
                    String reports = app.reportDAO.getAllReports();

                    HttpResponse response = new HttpResponse();
                    response.setStatusCode(200);
                    response.setBody(reports);
                    return response;
                } catch (SQLException e) {
                    e.printStackTrace();
                    HttpResponse response = new HttpResponse();
                    response.setStatusCode(500);
                    response.setBody("Internal Server Error");
                    return response;
                }
            });

            app.start(8080);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
