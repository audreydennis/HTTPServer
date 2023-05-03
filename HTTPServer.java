import java.io.*;
import java.net.*;
import java.util.*;

public class HTTPServer {
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(80);
        System.out.println("HTTP server listening on port 80...");

        while (true) {
            Socket socket = server.accept();
            handleRequest(socket);
        }
    }
    
    private static void handleRequest(Socket socket) throws IOException {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String line = reader.readLine();
        if (line != null) { // was throwing NullPointerException without this check 
            String[] requestLine = line.split(" ");
            String method = requestLine[0]; //sets GET, POST, PUT, DELETE to method
            String path = requestLine[1]; //sets file Name

            String requestParamValue = "";
            String body = "";

            // gets requestParamValue and body if PUT or POST
            if (method.equals("GET") || method.equals("DELETE")) {
                requestParamValue = path.substring(1);
            } else if (method.equals("POST") || method.equals("PUT")) {
                int contentLength = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.split(":")[1].trim());
                    }
                    if (line.trim().isEmpty()) {
                        break;
                    }
                }
                if (contentLength > 0) {
                    char[] buffer = new char[contentLength];
                    reader.read(buffer, 0, contentLength);
                    body = new String(buffer);
                }
                requestParamValue = path.substring(1);
            }
        
            String response = "";
        
            if (method.equals("GET")) {
                handleGet(socket, requestParamValue);
            } else if (method.equals("POST")) {
                handlePost(socket, requestParamValue, body);
            } else if (method.equals("PUT")) {
                handlePut(socket,requestParamValue, body);
            } else if (method.equals("DELETE")) {
                handleDelete(socket, requestParamValue);
            } else {
                response = "Unsupported method: " + method;
                System.err.println(response);
                sendResponse(socket, "HTTP/1.1 405 Method Not Allowed\r\nContent-Length: " + response.length() + "\r\n\r\n" + response);
            }
        }
        socket.close();
    }

    
    private static void handleGet(Socket socket, String requestParamValue) throws IOException{
        String response = "";
        String line;
        String fileName = "";
        File newFile = new File(requestParamValue);
        if(newFile.exists()){
            fileName = newFile.getName();
            System.out.println("GET request: " + newFile.getName());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(newFile)));
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                response += line;
            }
            bufferedReader.close();
            int index = fileName.lastIndexOf('.');
            String contentType = fileName.substring(index + 1);
            sendResponse(socket, "HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\nContent-Length: " + response.length() + "\r\n\r\n" + response);
        }else {
            response = "File not found: " + requestParamValue;
            System.err.println("GET request file not found: " + requestParamValue);
            sendResponse(socket, "HTTP/1.1 404 Not Found\r\nContent-Length: " + response.length() + "\r\n\r\n" + response);
        }
    }

    private static void handlePost(Socket socket, String requestParamValue, String body) throws IOException{
        String response = "";
        String fileName = requestParamValue;
        File newFile = new File(requestParamValue);
        if (newFile.exists()) {
            fileName = newFile.getName();
            int index = fileName.lastIndexOf('.');
            String contentType = fileName.substring(index + 1);
            System.out.println("POST request: " + newFile.getName());
            appendToFile(fileName, body);
            response = "OK";
            sendResponse(socket, "HTTP/1.1 200 OK\r\nContent-Type: " + contentType+ "\r\nContent-Length: " + response.length() + "\r\n\r\n" + response);
        } else {
            response = "File not found: " + requestParamValue;
            System.err.println("POST request file not found: " + requestParamValue);
            sendResponse(socket, "HTTP/1.1 404 Not Found\r\nContent-Length: " + response.length() + "\r\n\r\n" + response);
        }
    }

    private static void handlePut(Socket socket, String requestParamValue, String body) throws IOException{
        String response="";
        String fileName = requestParamValue;
        File newFile = new File(requestParamValue);
        if (newFile.createNewFile()) {
            fileName = newFile.getName();
            System.out.println("PUT request (new file): " + fileName);
            int index = fileName.lastIndexOf('.');
            String contentType = fileName.substring(index + 1);
            writeToFile(fileName, body);
            response = "OK";
            sendResponse(socket, "HTTP/1.1 200 OK\r\nContent-Type: " + contentType+ "\r\nContent-Length: " + response.length() + "\r\n\r\n" + response);
        } else {
            fileName = newFile.getName();
            System.out.println("PUT request (existing file): " + fileName);
            writeToFile(fileName, body);
            response = "OK";
            sendResponse(socket, "HTTP/1.1 200 OK\r\nContent-Length: " + response.length() + "\r\n\r\n" + response);
        }
    }

    private static void handleDelete(Socket socket, String requestParamValue) throws IOException{
        String response="";
        String fileName = requestParamValue;
        File newFile = new File(requestParamValue);
        if (newFile.delete()) {
            System.out.println("DELETE request: " + newFile.getName());
            response = "OK";
            sendResponse(socket, "HTTP/1.1 200 OK\r\nContent-Length: " + response.length() + "\r\n\r\n" + response);
        } else {
            response = "Failed to delete file: " + requestParamValue;
            System.err.println("DELETE request failed: " + requestParamValue);
            sendResponse(socket, "HTTP/1.1 404 Not Found\r\nContent-Length: " + response.length() + "\r\n\r\n" + response);
        }
    }

    private static void sendResponse(Socket socket, String response) throws IOException {
        OutputStream output = socket.getOutputStream();
        byte[] responseBytes = response.getBytes();
        output.write(responseBytes);
        output.flush();
    }

    // appends to end of file 
    private static void appendToFile(String fileName, String str) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
            out.write(str);
            out.close();
        } catch (IOException e) {
            System.err.println("Error appending to file: " + fileName);
            e.printStackTrace();
        }
    }

    //writes over any existing file
    private static void writeToFile(String fileName, String str) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            out.write(str);
            out.close();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + fileName);
            e.printStackTrace();
        }
    }         
}
