package TcpServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpHttpServer{
    public static void main(String[] args){
        try(ServerSocket serverSocket = new ServerSocket(80)){
            System.out.println("Server started on port 80");
            while(true){
                Socket clienSocket = serverSocket.accept();
                new Thread(() -> {
                    long threadId = Thread.currentThread().getId();
                    try {
                        handleClientReq(clienSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clienSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Thread Id "+threadId+" finished processig");
                    }
                }).start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static String getFileContent(String fileName){
        fileName = fileName.equals("/") ? "/index.html" : fileName;
        String filePath = "www"+fileName;
        File file = new File(filePath);
        filePath = file.exists() ?  filePath : "www/404.html";
        StringBuilder fileContentBuilder = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            String line;
            while((line = br.readLine()) != null){
                fileContentBuilder.append(line).append("\r\n");
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return fileContentBuilder.toString();
    }

    private static void handleClientReq(Socket clientSocket) throws IOException{
        try(BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());){
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String[] reqFirstLine = reader.readLine().split(" ");
                String reqMethod = reqFirstLine[0];
                String reqPath = reqFirstLine[1];
                System.out.println("request :\n" + String.join(" ", reqFirstLine));

                if (reqMethod.equals("GET")) {
                    // Prepare the response
                    String resp = getFileContent(reqPath);
                    String httpResponse ="HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: text/html\r\n" +
                                        "Content-Length: "+resp.getBytes().length+"\r\n" +
                                        "\r\n" +
                                        resp;
                    // Write the response
                    out.write(httpResponse.getBytes());
                    out.flush();
                    return;
                }
        }finally{
            clientSocket.close();
        }
    }
}