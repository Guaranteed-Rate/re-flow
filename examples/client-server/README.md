# Client/Server re-flow Example

This example demonstrates serving a flow from the server to the client to
execute. Since this example is a little more complex than the others, this
README provides the context rather than comments in the code.

The flow is defined in [core.clj](./src/clj/client_server/core.clj) and is
served using transit at the route `/flow`. Transit is used to ensure that the
flow structure is deserialized exactly on the client. It is possible to provide
the flow as JSON, but you would have to do some additional work on the client
to reconstruct the data (e.g., changing some strings to keywords).

The client is structured as suggested in the re-frame docs.

As with the other examples, you may run this example by simply running `lein run
figwheel` in a terminal in this directory.

As a challenge, try introducing making the answers multiple choice and adding
some branching logic based on the answers provided (using transition-specs).
Note that you can add more fields to the states that aren't used by re-flow but
are used by your code instead.
