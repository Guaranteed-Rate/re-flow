## FAQ

#### Why use functions for performing re-frame operations like creating subscriptions or dispatching events?

The primary reason is for documentation and discoverability. We love re-frame,
but one of the biggest challenges and points of frustration is knowing all the
keys and parameters needed to tie together different parts of the application,
especially when using third-party re-frame libraries. By providing convenience
functions, we are trying to make using re-flow a smoother experience - and one
that can be borne out on the REPL.

However, you _do not_ have to use these functions. There are effects, coeffects,
etc., defined in the corresponding namespaces.


#### Why use spec for specifying flow-db changes and transition values?

One of the early goals we had in designing re-flow was to be able to define a
flow on a server and serve it to a client over an HTTP call and the client
then be able to execute that flow. In order to achieve this, we needed some
representation that could be serialized and deserialized in different
environments. We believed that the best way to achieve this was to build specs
because they could be referenced (and resolved) by a keyword.

It is important to note that these two features are implemented as transition
interceptors, so you are free to rip them out and replace them with something
that fits your project better.

If you come up with some cool, general interceptors, we encourage you to open a
PR so we can discuss pulling them into re-flow!