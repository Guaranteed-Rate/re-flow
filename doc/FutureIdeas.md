## Future Ideas

The following list contains some ideas of what we might provide in a future
version of re-flow. Some of these ideas are vague, and many of them are
probably not good ideas. But there are here for us to think about and discuss.

* Sub-flows
  - This would include having states that execute as flows themselves.
  - Maybe have some sort of context stack to manage the sub-flows
  - Adds statechart semantics

* Abstract transition strategy
  - Make the transition implementation pluggable
  - This would allow us to create successor functions for states for dynamic
    flows

* Ephemeral data
  - Sometimes useful to thread data through states without being in a flow-db
  - Data used only for making decisions
  - Would likely dovetail with supporting successor functions to generate states