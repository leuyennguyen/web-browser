# web-browser
 The project aims to build a multithreaded Web server and a simple web client. The Webserver   and   Web   client   communicate   using   a   text-based   protocol   called   HTTP.
 
 Local Host: http://localhost:7777/
 
RESPONSE_200:
- index.html is the default webpage
RESPONSE_301:
- If user requests "index1.html", the status code will be set to 301 and the browser redirects to "index2.html"
RESPONSE_404:
- Any file that is not index.html or index1 or index2 will result in 404
