# Contributing to re-flow

Thank you for taking the time to contribute! Please note that re-flow follows a
[code of conduct](CODE_OF_CONDUCT.md).


## Reporting bugs

Ensure the bug was not already reported by searching the
[issues](https://github.com/Guaranteed-Rate/re-flow/issues).

If you do not find an open issue that addresses the problem,
[open a new one](https://github.com/Guaranteed-Rate/re-flow/issues/new).
Please be sure to include a title, clear description, and preferably a failing
test or small sample demonstrating the bug.


## Running tests

re-flow supports a few different environments, including ClojureScript, Clojure
1.8, and Clojure 1.9. In order to ensure correct behavior in all these
environments, re-flow provides a suite of tests that run in each.

The ClojureScript tests require [PhantomJS](http://phantomjs.org) to be
installed and on your path.

The following commands describe how to run tests in a variety of environments.

```
lein test-all    # All environments, required to pass before integration
lein test-clj    # Clojure 1.8, 1.9
lein test-cljs   # ClojureScript only
```

If you are actively working on re-flow, it is useful to run tests as files
change. You can do that by running the following command.

```
lein test-cljs-auto
```


## Fixing bugs

If you have fixed a bug and all tests are passing, please open a pull request
against the master branch. Include a good title and clear description of the
bug or a link to an open issue. Also update the Unreleased section of the
[CHANGELOG](CHANGELOG.md).

Once the pull request is opened, we will work with you to assess the code and
request any changes we deem necessary. Once any outstanding review items are
resolved we will integrate the bugfix.


## Adding new features

We highly encourage you to
[open an issue](https://github.com/Guaranteed-Rate/re-flow/issues/new) before
working on new features so that we can discuss it and ensure that it is a good
fit for re-flow.

Once you have completed your feature, open a pull request against the master
branch for review. Note that a feature will not be considered complete without
some tests that cover the feature. Also update the Unreleased section of the
[CHANGELOG](CHANGELOG.md).

Once the pull request is opened, we will work with you to assess the code and
request any changes we deem necessary. Once any outstanding review items are
resolved we will integrate the new feature.
