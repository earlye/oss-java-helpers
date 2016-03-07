![Logo](https://www.clearcapital.com/wp-content/uploads/2015/02/Clear-Capital@2x.png)
# Java Helpers

## What is it?

The Java Helpers library is just a collection of a few Java utilities.

## Why might I need it?

Suppose you'd like your code to be able to report its location in the
call stack. ```StackHelpers``` can help with that. Or maybe you'd like
to use Jackson serialization of JSON without the hassle of setting
up an ObjectMapper with settings that work well with javascript.
```JsonSerializer``` can help with that.

## What's not provided

We've tried to steer clear of stuff that already exists in Guava or
Apache commons, and in fact, use those libraries from this one. If you
ever see us reinventing stuff that lives there, please tell us.
