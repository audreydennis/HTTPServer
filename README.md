# HTTPServer

Creating an HTTP Server
For this assignment, you will be building your own HTTP server that implements HTTP/1.1 (RFC2616)Links to an external site.!

You don't have to read the whole RFC, but you do need to understand the following:

- HTTP Message Structure

- HTTP Request-Line and Status-Line

- HTTP Methods

- HTTP Headers (Content-Type and Content-Length)

- MIME Types

- Status Codes

## 
1. GET (2 pts): retrieve static file resource requested

- Make sure Content-Type is correct.

- Make sure Content-Length is correct.

- Your application should support at least two MIME types.

2. POST Links to an external site.(2 pts): 

- append the body of the request to the resource

- text/plain types only

3. PUT.(2 pts): 

- puts the body of the request to the resource

- Creates a new file using the body of the request.

- Overrides the content if the file already exists.

4. DELETE: 

- delete the resource
