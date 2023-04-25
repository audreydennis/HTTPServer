import java.io.*;
import java.net.*;
import java.util.*;
import com.sun.net.httpserver.*;

public class MyHttpServer{
    public static void main(String... args) throws Exception{
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 80), 0);
        server.createContext("/", new  MyHttpHandler());
        server.start();
    }
    
    static class MyHttpHandler implements HttpHandler {    
        // @Override    
        public void handle(HttpExchange httpExchange) throws IOException {
            String requestParamValue=handleURIRequest(httpExchange);
            String body=handleBody(httpExchange);
            if("GET".equals(httpExchange.getRequestMethod())) { 
                handleGetResponse(httpExchange,requestParamValue);
            }else if("POST".equals(httpExchange.getRequestMethod())) { 
                handlePostResponse(httpExchange,requestParamValue, body);       
            }else if("PUT".equals(httpExchange.getRequestMethod())){
                handlePutResponse(httpExchange,requestParamValue, body);
            }else if("DELETE".equals(httpExchange.getRequestMethod())){
                handleDeleteResponse(httpExchange,requestParamValue);
            }
        }

        // parses request
        private String handleURIRequest(HttpExchange httpExchange) {
            return httpExchange.
                    getRequestURI()
                    .toString()
                    .split("/")[1];
        }

        // reads requesst body into string ()
        private String handleBody(HttpExchange httpExchange) throws IOException{
            InputStreamReader isr =  new InputStreamReader(httpExchange.getRequestBody());
            BufferedReader br = new BufferedReader(isr);
            int b;
            StringBuilder buf = new StringBuilder(512);
            while ((b = br.read()) != -1) {
                buf.append((char) b);
            }
            br.close();
            isr.close();
            return buf.toString();
        }

        private void handleGetResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException {
            Headers h = httpExchange.getResponseHeaders();
            String line;
            String resp = "";
            String fileName="";
            try {
                File newFile = new File(requestParamValue);
                fileName = newFile.getName();
                System.out.println("GET request: " + newFile.getName());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(newFile)));
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                    resp += line;
                }
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                httpExchange.sendResponseHeaders(404, resp.length());
                e.printStackTrace();
            }
            int index = fileName.lastIndexOf('.');
            h.add("Content-Type", fileName.substring(index+1));
            httpExchange.sendResponseHeaders(200, resp.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(resp.getBytes());
            os.close();
        }

        private void handlePostResponse(HttpExchange httpExchange, String requestParamValue, String body)  throws  IOException {
            Headers h = httpExchange.getResponseHeaders();
            String resp = "";
            String fileName=requestParamValue;
            File newFile = new File(requestParamValue);
            if(newFile.exists()){
                fileName = newFile.getName();
                System.out.println("POST request: " + newFile.getName());
                appendStrToFile(fileName, body);
                httpExchange.sendResponseHeaders(200, resp.length());
            }else{
                System.out.println("POST request File Not Found:" + fileName);
                httpExchange.sendResponseHeaders(404, resp.length());
            }
            OutputStream os = httpExchange.getResponseBody();
            os.write(resp.getBytes());
            os.close();
        }

        private void handlePutResponse(HttpExchange httpExchange, String requestParamValue, String body)  throws  IOException {
            Headers h = httpExchange.getResponseHeaders();
            String resp = "";
            String fileName=requestParamValue;
            
                File myObj = new File(requestParamValue);
                if (myObj.createNewFile()) {
                    System.out.println("PUT request (new file): " + myObj.getName());
                    appendStrToFile(fileName, body);
                } else {
                    myObj.delete();
                    myObj.createNewFile();
                    System.out.println("PUT request (rewrote file): " + myObj.getName());
                    appendStrToFile(fileName, body);
                }
            
            httpExchange.sendResponseHeaders(200, resp.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(resp.getBytes());
            os.close();
        }

        private void handleDeleteResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException {
            Headers h = httpExchange.getResponseHeaders();
            String resp = "";
            String fileName="";
            File newFile = new File(requestParamValue);
            fileName = newFile.getName();
            if (newFile.delete()) { 
                System.out.println("DELETE request: " + newFile.getName());
            } else {
                System.out.println("Failed to delete the file.");
            } 
            httpExchange.sendResponseHeaders(200, resp.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(resp.getBytes());
            os.close();
        }
    }

    public static void appendStrToFile(String fileName, String str){
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
            out.write(str);
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception occurred" + e);
        }
    }
}