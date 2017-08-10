# Examples

In this folder you will find several examples that demonstrate the different
features of re-flow. The following list presents them in the order recommended
to learn re-flow's features. Each example demonstrates a new concept and builds
on the previous examples.

The source code for each example is heavily commented to provide context and
information about how to use and extend re-flow.

In order to present the code as clearly as possible, the examples do not follow
the recommended re-frame code layout. We have found that the recommended layout
works well with re-flow, and we encourage you to follow it in your applications.

To run an example, open the corresponding folder and run `lein figwheel`. The
example application will be available at `localhost:3449`, and figwheel will
automatically push cljs changes to the browser.


* [ping-pong](ping-pong) - A good starter re-flow application that demonstrates
the basics.
* [multi-flow](multi-flow) - An extension of the ping-pong example to include
running multiple flows concurrently.
* [counter](counter) - A counter application that introduces using spec to
manage data and control transitions using db-specs and transition-specs.
* [custom-transition-interceptor](custom-transition-interceptor) - An
application that demonstrates how to modify and extend the behavior of re-flow.
* [client-server](client-server) - An application that serves a flow from the
server and executes it on the client.