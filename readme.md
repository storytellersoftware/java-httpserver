# java-httpserver

It's an HTTP server, written in JAVA!

This is a slightly modified version of the underlying HTTP server used by
[Storyteller](http://storytellersoftware.com). One of the issues we found while
rewriting Storyteller's HTTP server was that there really isn't a nice, easy
to integrate HTTP server available for Java. As such, we wrote our own, which
is now here, waiting for you to use it.

## How does it work?

java-httpserver has four main components:

-   **HTTPServer**, a really simple class that usually runs in the background,
    waiting for requests, and sending them off to the *HTTPRequest* and
    *HTTPResponse* classes to get things done.

-   **HTTPRequest**, a class that takes in a Socket, reads from it, and splits
    apart the incoming data into its component parts (provided that the incoming
    Socket is following the HTTP protocol).

-   **HTTPHandler**, an abstract class whos subclasses are used to actually do
    things with the data.

-   **HTTPResponse**, a class that takes in an *HTTPHandler* and sends the
    handled data back to the browser.

There's also an `HTTPException`, and `DeathHandler`, which are used when bad
things occur. Don't let bad things occur.

## Demo?

You can test this out by running the provided Driver. At present it'll only
display "Hello, World!", no matter what URL you request (it's the one component
we're still working on), unless something bad happens, in which case it'll
display "Well, that went well...".

## Documenatation

The `javadoc` directory includes the generated javadoc for all of the included
files. Additionally, each file is fairly well documented (some are a little
lacking, but that's one of the two things being worked on right now).

## Helping out

If you see something fishy, or want to contribute in any way, including fixing
any obvious issue (and even the non-obvious ones), please submit a pull request,
or make an issue, or email me (Don Kuntz, don@kuntz.co).

Really, if you think something needs fixing, feel free to mention it, and if we
agree, we'll make the fix.

## Credits

java-httpserver is based on some of the work done by
[Don Kuntz](http://don.kuntz.co) and 
[Michael Peterson](https://github.com/mpeterson2) over the summer of 2013 while
working on [Storyteller](http://storytellersoftware.com).

This project is licensed under the MIT License (see license.md). While not
required, if you use this, we'd like to know about it, just alert us, somehow.