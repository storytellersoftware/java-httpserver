# java-httpserver

It's an HTTP server, written in JAVA!

Originally a more generic version of the HTTP server used by
[Storyteller](http://storytellersoftware.com), `httpserver` is a poorly named,
easy to use, and simple HTTP server, that's written in Java. 

We originally wrote our own HTTP server for Storyteller because we couldn't find
an easy to integrate and relatively simple (to the end user) HTTP server for
Java. `httpserver` takes inspiration from simple microframeworks like 
[Sinatra](http://www.sinatrarb.com/) and [Flask](http://flask.pocoo.org/).


## How does it work?

java-httpserver has five main components:

-   **HTTPServer**, a really simple class that usually runs in the background,
    waiting for requests, and sending them off to the *HTTPRequest* class to get things done.

-   **HTTPRouter**, a class that maps the path segments (the string
    between the first two `/`s in the request) to *HTTPHandlers*.

-   **HTTPRequest**, a class that takes in a Socket, reads from it, and splits
    apart the incoming data into its component parts (provided that the incoming
    Socket is following the HTTP protocol).

-   **HTTPResponse**, a class that's passed to all handling methods and filled
    in to send data back the the client. After an *HTTPHandler* fills in a
    Response, it sends that data back to the client.

-   **HTTPHandler**, an abstract class whos subclasses are used to actually do
    things with the data, specifically filling out *HTTPResponse*s.

There's also an `HTTPException`, and `DeathHandler`, which are used when bad
things occur. Don't let bad things occur.


## Demo?

A very basic demo showing off most of the features of `httpserver` can be found
in the `demo` package. 

If you're looking for an HTTPHandler that does interesting things, check out the
`tests.HandlerTest`.


## Helping out

If you see something fishy, or want to contribute in any way, including fixing
any obvious issue (and even the non-obvious ones), please submit a pull request,
or make an issue, or email me (Don Kuntz, don@kuntz.co).

Really, if you think something needs fixing, feel free to mention it, and if we
agree, we'll make the fix.

## Credits

java-httpserver is based on some of the work done by
[Don Kuntz](http://don.kuntz.co) and 
[Michael Peterson](http://mpeterson2.github.io) over the summer of 2013 while
working on [Storyteller](http://storytellersoftware.com).

This project is licensed under the MIT License (see `license.md`). While not
required, if you use this, we'd like to know about it, just alert us, somehow.
