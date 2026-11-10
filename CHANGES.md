# Notable changes:

## com.github.pjfanning acked-streams 2.0.0

- Built against Apache Pekko 2.0.0. Requires Java 17+; Scala 2.13 and 3 (Scala 2.12 dropped, as Pekko 2 does not support it).
- `OverflowStrategy.dropNew` is no longer handled by the acked buffer, as Pekko 2 removed it.

## com.github.pjfanning acked-streams 1.0.1

- Added Scala 3 support (Scala 2.12, 2.13 and 3). Built against Apache Pekko 1.0.1.

## com.github.pjfanning acked-streams 1.0.0

- First release of the Apache Pekko fork. Built against Apache Pekko 1.0.0; Scala 2.12 and 2.13.
- Package renamed to `com.github.pjfanning.acked`.

## timcharper acked-stream 2.0 (historical)

These notes are from the original Akka-based [timcharper/acked-stream](https://github.com/timcharper/acked-stream) 2.0 release.

- AckedSink.fold / AckedSource.runFold were removed. They are too awkward. You may use AckedFlowOps.fold with AckedSink.head, if you wish. The behavior of the combination of those elements is more clear.

- API compatibility was brought into alignment with Akka Streams 2.0 (the API now follows Pekko Streams).
