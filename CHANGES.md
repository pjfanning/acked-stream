# Notable changes:

## com.github.pjfanning acked-streams 2.0.0

- Built against Apache Pekko 2.0.0. Requires Java 17+; Scala 2.13 and 3 (Scala 2.12 dropped, as Pekko 2 does not support it).
- `OverflowStrategy.dropNew` is no longer handled by the acked buffer, as Pekko 2 removed it.

The sections below describe the original timcharper/acked-stream releases.

## 2.0

- AckedSink.fold / AckedSource.runFold were removed. They are too awkward. You may use AckedFlowOps.fold with AckedSink.head, if you wish. The behavior of the combination of those elements is more clear.

- Naturally, API compatibility was brought into alignment with Akka Streams.
