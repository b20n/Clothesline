# Clothesline

## Introduction ##

Clothesline is a port of the popular HTTP service framework
[WebMachine](https://github.com/basho/webmachine) in
Clojure, with provisions for use in Scala and Java. It is currently a
naive port; it closely mimics the WebMachine module signature (as a
Java interface) and execution mode, even down to the [graph model and
node names](http://webmachine.basho.com/diagram.html).

### What Does It Do? ###

Clothesline offers a modular way to provide HTTP services that behave correctly
according to the HTTP 1.1 specification. It is particularly useful for services
that heavily leverage the HTTP spec, headers, and behaviors. But, it can be
useful even for simple services because of the fine-grained control it
gives over the decision-making process for web requests.

Clothesline uses [Ring](http://github.com/mmcgrana/ring) and
[Clout](http://github.com/weavejester/clout) to provide an abstract
base over a variety of web servers. The default web server is
Jetty. Much like other Ring-based libraries, Clothesline takes a
routing table which routes paths to handler functions. Unlike other libraries which
simply pass the request to a naive handler function, Clothesline
traverses a graph of HTTP 1.1 behavior, using your request and the
routed handler to make decisions about how to proceed. The
final product of the request is ultimately determined by this graph and
the intermediate products of this interrogation.


#### Why Are You Making It? ####

We are using it to build RESTful services for our internal
APIs. Clojure and Scala both have excellent web framework options, but
none of them are specifically oriented towards designing APIs with correct
HTTP 1.1 behavior. WebMachine, while slightly more awkward than some
web frameworks for content delivery, is superb for designing RESTful
interfaces without having to worry about correctness.

BankSimple's stack is also multi-lingual, using Scala, Clojure, and
JRuby. It's important for our development efforts to have a
plays-well-with-others project where code can be shared between languages. We
think that JVM language crosstalk is going to be a major asset for us, and
increasingly you see other companies talking about similar experiments. Maybe
we're on to something. Clothesline is a way of finding out.

### A Simple Example ###

Using the `defsimplehandler` in `clothesline.service.helpers` we can
quickly make a simple hello-world service:

    (ns example1
      (:use clothesline.core
            [clothesline.service.helpers :only [defsimplehandler]])

    ;; A default handler that only cares about content-types.
    ;;
    ;; This not only defines a type, but creates a named instance
    ;; of the object matching the handler name, ready to be used
    ;; as part of routing.  It is not, however, a classical ring
    ;; handler - only a Clothesline handler.
    (defsimplehandler example1-simple
      "text/plain" (fn [request graphdata] "Hello World."))

    ;; Request is the ring request, passed through.
    ;; Graphdata is the accumulated data about the response.
    (defsimplehandler example1-params
    "text/plain" (fn [request graphdata]
                     (str "Your params: " (:params request))))

    ;; A traditional clout routing table. Note the colon-params in the
    ;; service are provided and placed in
    (def routes {"/" example1-simple, "/:gratis" example1-params})

    ;; This is our server instance:
    (defonce *server*
      (produce-server routes {:port 9999 :join? false}))

`defsimplehandler` is actually a very simple macro. It expands our form to the
relatively simple handler form that overrides `content-types-provided` for
that specific instance.

### A Not-Quite-So-Simple Example ###

`defsimplehandler` is built on the `defhandler` macro, which wraps the
standard behaviour of building a new handler instance with the default
behaviours in place.  A more complex resource is easy to build:

    (ns example1
      (:use clothesline.core
            [clothesline.service.helpers :only [defhandler]]
            [clothesline.protocol.test-helpers :only [annotated-return]]))

    (defhandler hello
      ;; any request without a "greet" parameter is malformed.
      ;; also, augment graphdata with that greeting.
      :malformed-request? (fn [_ {:keys params} _]
                            (if-let [greeting (params "greet")]
                               (annotated-return false {:annotate {:greet greeting}})
                               true))
      ;; ... mostly defaults.
      :resource-exists?   (constantly true)
      :allowed-methods    (constantly #{:get})
      ;; ...and some content generation functions.
      :content-types-provided
        (constantly {"text/html"  fancy-hello
                     "text/plain" (fn [_ _ graphdata]
                                     (str "Hello, " (:greet graphdata)))}))

    ;; A traditional clout routing table. Note the colon-params in the
    ;; service are provided and placed in
    (def routes {"/:greet" hello})

    ;; This is our server instance:
    (defonce *server*
      (produce-server routes {:port 9999 :join? false}))

## Format of a Handler ##

Two specifications exist for handlers, in `clothesline/service.clj`
and `clothesline/interop/iservice.clj`. The former is considered
canonical and the later is maintained as a way to support handlers
written in other Java languages. These functions are identical in
application to the functions detailed in
[the WebMachine Resource Documentation](http://webmachine.basho.com/resources.html),
with a few key exceptions. The most obvious is the naming convention,
any method with "is-" to specify boolean return is instead appended
with a "?".

The most important exception is that, unlike WebMachine's handlers
which use the Erlang Process dictionary to accumulate state,
Clothesline prefers using annotated return values to allow the
accumulated state to be arbitrarily extended. To this end, if you wish
to extend the "graphdata" (Clothesline's name for the extended state)
you should use `clothesline.protocol.test-helpers/annotated-return` to
augment your response

That expands to the record class defined in `clothesline.interop.nodetest`,
`TestResult`. This class contains two cells, one is the `:result` cell which
should contain your normal result value. The other is an `:annotations` cell,
which should contain a Map. The map respects two keys:

* `annotate`: (should contain a dictionary with Clojure keyword keys).
  Any key placed in this dictionary will be available in the graphdata
  as subsequent handlers are called.  Later in the documentation for
  some keys of interest for annotation.

* `headers`: (should contain a dictionary of string to string).
  This dictionary will be appended to the final set of response headers,
  in addition to the normal HTTP logic. The most common header to insert
  here would be `"Location"`.

-----

Please note that some common headers such as `Content-Length`, and
`Content-Type` should be automatically generated for you, unless your handler
is *extremely* unusual. `Content-Length`, in particular, can be disastrous to
modify since most browsers hang when confronted with an over-large
`Content-Length` header.

Augment, don't replace, standard HTTP headers in your handlers.

-----

### Expected Usage: Where Logic Lives ###

All handler calls receive 2 arguments, the Ring request and the
accumulated graphdata structure thus far. Many are expected to return
simply boolean values, and those that are have a "?" at the end of
their name in the Clojure protocol.

When organizing a handler, it makes sense to use handler functions
based off intent; a practice which makes them much easier to read. For
example, many authorization schemes take authorization credentials and
return permission lists. While the `authorized?` handler function is
asked only to return a boolean, it makes sense from an architectural
perspective to fill the graphdata (via annotations) with all the
salient permission data for this request's authorization.

When architecting your restful services, try and keep your logic
organized around these functions. If you do so, it will result in
higher code re-usability and a cleaner, clearer architecture.

### Departures from WebMachine ###

There are a few key departures from WebMachine's model that should be
noted. The most obvious is the content-types-provided and
content-types-accepted. These are maps of content-type-string to function, but
the functions are different. They *must* take two arguments: the ring request
and the current graphdata. The *must* return a simple string or a function
that evaluates to a simple string.  There are plans to allow for other return
types (in particular: threads, streams, delay and future objects, etc), but
they are currently not supported.

`allowed-methods` should return a Set as opposed to a List.

`finish-request`'s return values are ignored.


## Meaningful Keys For Annotation ##

Annotation keys are stored in the graphdata structure, which is passed
amongst states and passed to every handler test.

The graphdata structure contain annotations and the sum of the headers that
should be explicitly added. These values can be directly specified with
annotations. If a test called later in the graph specifies a value that
contradicts an earlier value, the later specification overrides the earlier
one. It is important to note that these values are special, but not the only
allowed values. *Any key and value is a valid annotation!*

`:headers` This is a string-string map of header values. Please note
that headers are case-sensitive. The headers map is used by the graph
logic to store values such as Content-Type.

`:body` This value is the current body computation. Currently, it must
be function evaluating to a string or just a string. If the body is a
function, it will be passed the request and graphdata objects (much
like the `content-types-provided` elements will be. If an explicit
body entry is set, it will override implicit body generation when applicable.

`:content-encoder` The content encoding function is not currently
used, but is set. In future releases it will work.

`:content-converter` The content converting function is not currently
used.


## Further Work Towards Completeness ##

* There are some outstanding issues the Accept header. If you're having
  problems with spurious 204s on clients, advise them to set their "Accept"
  header to exactly the content type they want for now.

* Currently, date-related states in the HTTP graph do not work properly.

* Encoding and charset changes also do not work correctly. All charsets
  should be utf-8 for now.

* Data from `content-types-provided` and `content-types-accepted` is
  not checked during header generation.

* This implementation was spiked, so per-state tests are forthcoming.

* Currently accept headers do not handle `*/*` properly; you can write
  a `*/*` provider but you cannot specify a real content type for the
  result. Obviously, this is an oversight.

* The graphdata requires some updates for consistency.


## Usage

See `test/clothesline/complex-server.clj` for a more complete
demonstration. `clothesline.core` has functions for generating servers
and handlers as necessary.

### Inter-operation With Other Languages ###

As stated, one of our goals with Clothesline is to allow most JVM
languages to express Clothesline handlers. The simple way to do this
is to provide instances of objects that conform to
`clothesline.interop.IService.` To make this easier, there is a base
class named `clothesline.service.BaseService` that provides the same
default behaviors that the default protocol provides.

## Installation

You can grab ClothesLine from [CloJars](http://clojars.org/search?q=clothesline)
using your favorite tool.  Leinengein works well enough.

