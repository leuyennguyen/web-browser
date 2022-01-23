/*
    Le Uyen Nguyen
    100 171 8086
*/
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.StringTokenizer;

public class WebServer implements Runnable {
    /* Renaming .html files for readability */
    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_WEBPAGE = "index.html"; // Main webpage of localhost
    static final String P_MOVED_PAGE = "index1.html"; // Permanently moved page
    static final String REDIRECTED_PAGE = "index2.html"; // If user enter P_MOVED_WEBPAGE, the browser will link to this redirected page
    static final String NOT_FOUND_PAGE = "404.html"; // .html file contains response 404

    /* Connection through PORT 7777 */
    static final int PORT = 7777;

    /* Variable connect will hold connection between client and server */
    private Socket connect;
    /* Constructor takes parameter c from class Socket */
    public WebServer(Socket c) {
        connect = c;
    }

    public static void main (String[] args) {
        try {
            // Creating web server socket at port 7777
            final ServerSocket server = new ServerSocket(PORT);
            System.out.println("Server at port 7777 is ready to receive........");
            // TCP - Server will listen forever unless errors occurs or connection is manually disconnected
            while (true) {
                // Receiving client's request
                WebServer client = new WebServer(server.accept());

                // Dedicated thread managing client's request
                Thread thread = new Thread(client);
                thread.start();
            }
        }
        catch (IOException e) {
            System.err.println("Server connection error: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        BufferedReader in = null; // Reading data from client's request
        PrintWriter out = null; // Preparing server's response
        BufferedOutputStream data = null; // Sending the response to client
        String requestedFile = null; // Requested file from client

        try {
            // Read client's request from socket's input stream
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            // Getting output stream to client (preparing headers)
            out = new PrintWriter(connect.getOutputStream());
            // Getting binary output stream to client (fetching .html file)
            data = new BufferedOutputStream((connect.getOutputStream()));

            // Reading line from client's request
            String input = in.readLine();
            // Parsing request
            StringTokenizer parse = new StringTokenizer(input);
            // Reading HTTP method and capitalize it --> GET
            String method = parse.nextToken().toUpperCase();
            // Reading file name and uncapitalize it to handle case sensitivity
            // Assuming all filenames stored in server are all lowercase
            requestedFile = parse.nextToken().toLowerCase();

            // The program only handles GET method
            if (method.equals("GET")) {
                /* If user access the port without specifying request,
                 * server automatically fetches the default webpage.
                 * Since the program only handles GET method, it is
                 * necessary to manage the "/"
                 */
                if (requestedFile.endsWith("/")) {
                    requestedFile += DEFAULT_WEBPAGE;
                }

                File file = new File(WEB_ROOT, requestedFile);
                // The length of the requested file is read and will be
                // used in the "Content-length" box of the header
                int length = (int) file.length();
                // Assuming that the "Content-Type" box in header is always text/html
                String content = "text/html";

                // Getting file's content for output stream
                byte[] fileContent = readFile(file, length);

                // If file is found on the server, sending response 200
                ReponseOK(out, data, content, length, fileContent);
            }
        }
        /* If file is not found on the server, there are 2 cases:
         * - File is permanently moved 301
         * - File does not exist on the server 404
         */
        catch (FileNotFoundException e) {
            try {
                // Handling the leading "/" of the requested file
                if (requestedFile.startsWith("/")) {
                    requestedFile = requestedFile.substring(1);
                }
                // "index1.html" is hardcoded as the page that is moved permanently
                if (requestedFile.equals(P_MOVED_PAGE)) {
                    ResponsePermanentlyMoved(out, data);
                }
                // Any file else does not exist
                else {
                    ResponseFileNotFound(out, data);
                }
            }
            catch (IOException er) {
                System.err.println("File Not Found Error Exception: " + er);
            }
        }
        catch (IOException e) {
            System.err.println("Server error: " + e);
        }
    }
    /* readFile() receives the file and its length, reads
     * and returns its binary content. */
    private byte[] readFile(File file, int length) throws IOException {
        FileInputStream inFile = null;
        // Reading only the exact length of file's content
        byte[] fileContent = new byte[length];

        try {
            inFile = new FileInputStream(file);
            inFile.read(fileContent);
        }
        finally {
            // If the file is successfully read, it will be closed
            if (inFile != null) {
                inFile.close();
            }
        }
        return fileContent;
    }
    // Header of Response Code 200 OK
    private void ReponseOK(PrintWriter out, BufferedOutputStream data, String content, int length, byte[] fileContent) throws IOException {
        // Composing header field
        out.println("HTTP/1.1 200 OK");
        out.println("Date: " + new Date());
        out.println("Server: Java_HTTP_Server/1.0");
        out.println("Accept-Ranges: bytes");
        out.println("Content-Length: " + length);
        out.println("Connection: Keep-Alive");
        out.println("Content-Type: " + content);
        // This blank line separates header and content section
        out.println();
        out.flush();
        // Writing data out to client
        data.write(fileContent, 0, length);
        data.flush();
    }
    // Header of Response Code 301 Permanently Moved
    private void ResponsePermanentlyMoved(PrintWriter out, OutputStream data) throws IOException {
        // Set status code 301
        URL hh = new URL("http://httpstat.us/301");
        HttpURLConnection conn = (HttpURLConnection)hh.openConnection();

        // Fetching the redirected page
        File file = new File(WEB_ROOT, REDIRECTED_PAGE);
        int length = (int) file.length();
        String content = "text/html";
        byte[] fileContent = readFile(file, length);

        // Composing header field
        out.println("HTTP/1.1 301 Moved Permanently");
        out.println("Date: " + new Date());
        out.println("Server: Java_HTTP_Server/1.0");
        out.println("Accept-Ranges: bytes");
        out.println("Content-Length: " + length);
        out.println("Connection: Keep-Alive");
        out.println("Content-Type: " + content);
        out.println("Location: /" + REDIRECTED_PAGE);
        // This blank line separates header and content section
        out.println();
        out.flush();

        // Writing data out to client
        data.write(fileContent, 0, length);
        data.flush();
    }
    // Header of Response Code 404 Not Found
    private void ResponseFileNotFound(PrintWriter out, OutputStream data) throws IOException {
        File file = new File(WEB_ROOT, NOT_FOUND_PAGE);
        int length = (int) file.length();
        String content = "text/html";
        byte[] fileContent = readFile(file, length);

        // Composing header field
        out.println("HTTP/1.1 404 Not Found");
        out.println("Date: " + new Date());
        out.println("Server: Java_HTTP_Server/1.0");
        out.println("Accept-Ranges: bytes");
        out.println("Content-Length: " + length);
        out.println("Connection: Keep-Alive");
        out.println("Content-Type: " + content);
        // This blank line separates header and content section
        out.println();
        out.flush();

        // Writing data out to client
        data.write(fileContent, 0, length);
        data.flush();
    }
}
