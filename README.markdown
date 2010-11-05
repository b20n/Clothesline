# Clothesline

## Introduction ##

Clothesline is a port of the popular HTTP service framework
[WebMachine](http://bitbucket.org/justin/webmachine/wiki/Home) in
Clojure, with provisions for use in Scala and Java. It is currently a
naive port; it closely mimics the WebMachine module signature (as a
java interface) and execution model (even down to the [graph model and
node names](http://webmachine.basho.com/diagram.html)). 

### What Does It Do? ###

Clothesline provides a modular way to provide HTTP services with
correct HTTP 1.1 behavior. It is particularly useful for services that
heavily leverage the HTTP spec, headers, and behaviors. But, it can be
useful even for simple services because of the fine-grained control it
gives over the decision making process for web requests.

Clothesline uses [Ring](http://github.com/mmcgrana/ring) and
[Clout](http://github.com/weavejester/clout) to provide an abstract
base over a variety of web servers (although the default is
Jetty). Much like other Ring-based libraries, Clothesline takes a
routing table and routes it to handlers. Unlike other libraries which
simply plass the request to a naive handler function, Clothesline
moves across a graph of HTTP 1.1 behavior, using your request and the
routed handler to make specific decisions about how to proceed. The
final product of the request is ultimately determined by the graph and
the intermedia products of this interrogation.

### A Simple Example ###

Using the `defsimplehandler` in `clothesline.service.helpers` we can
quickly make a simple hello-world service:

    (ns example1
      (:use clothesline.core
            [clothesline.service.helpers :only [defsimplehandler]])

    ;; A default handler that only cares about content-types.
    ;;
    ;; This not only defines a type, but actually instantiates
    ;; example1-server. defsimplehandler is not meant for anything
    ;; but the simplest use.
    (defsimplehandler example1-simple
      "text/plain" (fn [request graphdata] "Hello World."))
  
    ;; Request is the ring request, passed through. 
    ;; graphdata is the accumulated data about the response.
    (defsimplehandler example1-params
    "text/plain" (fn [request graphdata] 
                     (str "Your params: " (:params request))))
    ;; A traditional clout routing table. Note the colon-params in the
    ;; service are provided and placed in 
    (def routes {"/" example1-simple, "/:gratis" example1-params})
    
    ;; This is our server instance:
    (defonce *server* 
      (produce-server routes {:port 9999 :join? false}))

`defsimplehandler` is actually a very simple macro. It expands our form 
to the relatively simple handler form that overrides 
`content-types-provided` for that specific instance. 

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
you should use the record class defined in
`clothesline.interop.nodetest`, TestResult. This class contains two
cells, one is the `:result` cell which should contain your normal
result value. The other is an `:annotations` cell, which should contain
a Map. The map respects two keys:

* annotate: (should contain a dictionary with Clojure keyword
  keys). Any key placed in this
  dictionary will be carried over to the graphdata as request. See
  later in the documentation for some keys of interest for annotation.
* headers: (should contain a dictionary of string to string). This
  dictionary will be appended to the graphdata response headers
  outside of the normal http logic, in
  `(:headers graphdata)`. The most common header values to insert are
  responses like "Location". 
  
-----  
Please note that some common headers such as Content-Length and
Content-Type should be automatically generated for you, unless your
handler is unusual. Content-Length, in particular, can be disasterous 
to modify since most browsers hang  when confronted with an
over-large Content-Length header.

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
higher code reusability and a cleaner, clearer architecture.

### Departures from WebMachine ###

There are a few key departures from WebMachine's model that should be
noted. The most obvious is the content-types-provided and
content-types-accepted. These are maps of content-type-string to
function, but the functions are different. They *must* take two
arguments: the ring request and the current graphdata. The *must*
return a simple string. There are plans to allow for other return
types (in particular: threads, streams, delay and future objects,
etc), but they are currently not supported.

`allowed-methods` should return a Set as opposed to a List.

`finish-request`'s return values are ignored.

## Meaningful Keys For Annotation ##

Annotation keys are stored in the graphdata structure, which is passed
amongst states and passed to every handler test.

The graphdata structure contain annotations and the sum of the headers
that should be explicitly added. These values can be directly
specified with annotations. If a test called later in the graph specifies a value that
contradicts an earlier value, the later specification overides the
earlier one. It is important to note that these values are special,
but not the only allowed values. *Any key and value is a valid
annotation!* 

:headers
:body 
:content-type
:content-encoding
:content-encoder
:content-charset
:content-converter


## Further Work Towards Completeness ##

* Currently, date-related states in the HTTP graph do not work
properly. 

* Encoding and charset changes also do not work correctly. All Charsets
should be utf-8 for now.

* `content-types-accepted` does not work as one might expect.

* There is still some remaining work to be done with paths leading to
state N11.

* This implementation was spiked, so per-state tests are forthcoming.

* Currently accept headers do not handle `*/*` properly; you can write
  a `*/*` provider but you cannot specify a real content type for the
  result. Obviously, this is an oversight.

* The graphdata requires some updates for consistency.




## Usage

FIXME: write

## Installation

FIXME: write

## License

Copyright (C) 2010 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
