import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        int port = 1989; // Sunucunun dinleyeceği port
        InetSocketAddress addr = new InetSocketAddress(port);

        // HttpServer nesnesi oluşturuluyor: bu, gelen HTTP bağlantılarını dinleyen ana objedir.
        HttpServer server = HttpServer.create(addr, 0);

        // Rotaları (context) tanımlıyoruz:
        server.createContext("/", new RootHandler());          // ana sayfa handler'ı
        //server.createContext("/hello", new HelloHandler());    // basit query param örneği
        //server.createContext("/echo", new EchoHandler());      // POST veriyi geri döndüren handler
        //server.createContext("/static", new StaticHandler("./static")); // statik dosya sunucusu

        // Thread pool: gelen her isteği işleyecek iş parçacıkları.
        // Tek thread ile de çalışabilir ama thread pool gerçek dünyada daha iyi.
        ExecutorService threadPool = Executors.newFixedThreadPool(8);
        server.setExecutor(threadPool);

        // Sunucuyu başlat:
        System.out.println("Sunucu başlatılıyor: http://localhost:" + port + "/");
        server.start();

        // Programı kapatırken thread pool'u düzgün kapatmak iyi olur.
        // Burada örnek olarak main sonlandıktan sonra JVM kapanana kadar server çalışır.
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // exchange: gelen isteği ve gidecek yanıtı temsil eder.

            // İstek yöntemi (GET, POST vs.)
            String method = exchange.getRequestMethod();

            /*String response = "<html><body>"
                    + "<h1>Ayberk Eren Kaya</h1>"
                    + "<h2>1240505056</h2>"
                    + "<p>POST /echo — gönderdiğiniz veriyi geri alırsınız</p>"
                    + "<p>/static/&lt;dosya&gt; — ./static içindeki dosyalar</p>"
                    + "<p>İstek yöntemi: " + method + "</p>"
                    + "</body></html>";*/
            String response = """
                <html>
                <head>
                  <meta charset="UTF-8">
                  <title>Ayberk Eren Kaya</title>
                  <style>
                    body { font-family: Arial; background:#f2f2f2; text-align:center; padding:30px; }
                    h1 { font-size:15pt; color:#79cdcd; margin:5px; }
                    h2 { font-size:15pt; color:#8deeee; margin:5px; }
                    p  { font-size:17px; color:#333; margin-top:15px; }
                  </style>
                </head>
                <body>
                  <h1>Ayberk Eren Kaya</h1>
                  <h2>1240505056</h2>
                  <p>Kırklareli üniversitesinde yazılım mühendisliği 2. sınıf öğrencisiyim.</p>
                </body>
                </html>
                """;

            // HTTP durum kodu ve Content-Length başlıkları ayarlanır:
            byte[] bytes = response.getBytes("UTF-8");
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);

            // Yanıt gövdesi yazılır ve stream kapatılır:
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // ---------- Yardımcı (utility) methodlar ----------

    // Basit JSON escape: ", \, kontrol karakterleri
    static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int)c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}