# Clothesline

## Introduction ##

Clothesline is a port of the popular HTTP service framework
[WebMachine](http://bitbucket.org/justin/webmachine/wiki/Home) in
Clojure, with provisions for use in Scala and Java. It is currently a
naive port; it closely mimics the WebMachine module signature (as a
java interface) and execution model (even down to the [graph model and
node names](http://webmachine.basho.com/diagram.html). 

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
routing table and routes it to hanlders. Unlike other libraries which
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
    ;; service are provided.d
    (def routes {"/" example1-simple, "/:gratis" example1-params})
    
    ;; This is our server instance:
    (defonce *server* 
      (produce-server routes {:port 9999 :join? false}))

`defsimplehandler` is actually a very simple macro. It expands our form to the relatively simple handler form that overrides `content-types-provided` for that specific instance. 

## Format of a Handler ##

Two specifications exist for handlers, in `clothesline/service.clj` and `clothesline/interop/iservice.clj`. The former is considered canonical and the later is maintained as a way to support handlers written in other Java languages. 

\


## Usage

FIXME: write

## Installation

FIXME: write

## License

Copyright (C) 2010 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
