# Contributing

All are welcome, and should, contribute if they feel like it. We have only a few
requirements (and, for the most part, we'd probably be willing to forgive you if
you didn't follow them, especially on the coding standards part).

## Coding Standards

Very basic, but:

- Two space, soft tabs. That's right, only two, which is probably contrary to
  most Java projects, but that's how we're doing things.

- If someone else might put a space there, you should put a space there. This
  means before an opening paren on keywords (not method calls), around
  operators, and anywhere else that a space seems logical.

- 80 character line limits. Don't go over 80 characters, which is a little
  hard in Java, but try for it... This is the main reason for our two space
  soft tabs.

- If there weren't an 80 character limit, and it would be part of the previous
  line, indent it an extra eight spaces (four tabs). This is actually part of
  the
  [official conventions](http://www.oracle.com/technetwork/java/javase/documentation/codeconventions-136091.html).
  See section 4.2 for a better explanation.

- Javadoc. Everything. I mean it, even private things, or really simple things.  
  The main goal for this project is to be usable by anyone and everyone, which
  means we should have some really nice documentation, internally and
  externally. While we're currently lacking on the external part, we can at
  least try to make the internal part wonderful while we're working on it.

- Likewise, document anything tricky, and try to put down the "why" behind what
  you write, because it can help explain context to people.

- If you write the same long line, or a block of multiple lines multiple times,
  it might be best to turn it into a method. Maybe. I mean there isn't a
  principle called DRY or anything.

## Making a code contribution

It's probably best if you make a fork, develop on your fork, and submit a pull
request to us. We'll look at it, probably try it out, and either make
suggestions or pull it in.

## Other kinds of contributions

External documentation is something we really need. Also, if you run into
anything that even seems mildly bug-like to you, please submit a new issue to
our issue tracker. We want to know about these things.
