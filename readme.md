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

At present we have a home spun HTTP server, which manages all incoming requests.
One of our current goals is to switch to being a simple abstraction layer on top
of a better foundation, probably Jetty.

### Specifics?

You can see an example server inside the `test.ServerApplicationTest` class.
While it's not complete, and doesn't actually run a server, it shows the basis
of how you'd build a new application.

At some point in the near future, we hope to have an example application
included in the source.

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
