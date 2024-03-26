# bftbench
BFT Protocol Benchmarking Suite

## Prerequisites

For the benchmarking suite to work, you need to have the following installed on your system:
- Java 21

For the user interface to work, you need to have the following installed on your system:
- Node.js
- pnpm

Installing everything on macOS:

```
brew install openjdk@17 node pnpm
```

## Building

To build the benchmarking suite, run the following command:

```
./gradlew build
```

## Running

To run the benchmarking suite, run the following command:

```
./gradlew run
```

## Web Interface

To run the web interface, run the following command:

```
(cd webui && pnpm install && pnpm run dev)
```

The UI should then be available at http://localhost:3000.
