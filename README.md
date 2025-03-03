# ByzzBench - a BFT Protocol Benchmarking Suite

[![Build Docker Image](https://github.com/joaomlneto/byzzbench/actions/workflows/docker-build.yml/badge.svg)](https://github.com/joaomlneto/byzzbench/actions/workflows/docker-build.yml)

> ByzzBench is a benchmark suite designed to evaluate the performance of testing algorithms in detecting bugs in
> Byzantine
> Fault Tolerance (BFT) protocols. It is designed to be modular and extensible, allowing for easy integration of new
> protocols and scenarios.
>
> ByzzBench is designed for a standardized implementation of BFT protocols and their execution in a controlled testing
> environment, controlling the nondeterminism in concurrency, network and process faults in the protocol execution,
> enabling the functionality to enforce particular execution scenarios and thereby facilitating the implementation of
> testing algorithm for BFT protocols.

This is a Gradle monorepo that contains the following modules:

- `simulator`: The core benchmarking suite, written in Java/Spring Boot. It currently also includes the protocol
  implementations.
- `webui`: A web interface for the benchmarking suite, written in React/NextJS.

## Prerequisites

For the benchmarking suite to work, you need to have the following installed on your system:

- Java 21

For the user interface to work, you need to have the following installed on your system:

- Node.js
- pnpm

### MacOS

Installing everything on macOS using HomeBrew can be done with the following commands:

```
brew install openjdk@17 node pnpm
```

### Windows

Installing JDK:

1. Through Eclipse Adoptium:

    - Download the version you need (JDK-21)
    - When installing, select "Set or override JAVA_HOME variable"

2. Through Windows Package Manager - "winget":

```
// Eclipse Temurin from Eclipse Adoptium
winget install EclipseAdoptium.Temurin.21.JDK

// from Microsoft Build
winget install Microsoft.OpenJDK.21
```

> [!NOTE]
> You might need to set a PATH and JAVA_HOME!

To verify the installation execute the following in cmd (should match the installed version):

```
java -version
```

> [!NOTE]
> If it displays a different version, then the PATH or JAVA_HOME could be set incorrectly (pointing to another version)!

Installing Node.js:

1. Through installer: https://nodejs.org/en/download/prebuilt-installer
2. Through package managers: https://nodejs.org/en/download/package-manager

To install pnpm, you can use npm package manager (installed alongside Node.js):

```
npm install -g pnpm
```

or Corepack:

```
corepack enable pnpm
```

or PowerShell:

```
Invoke-WebRequest https://get.pnpm.io/install.ps1 -UseBasicParsing | Invoke-Expression
```

> [!WARNING]
> Windows Defender may block this option!
>
> Using npm or Corepack is the recommended way!

Reference: https://pnpm.io/installation

For other operating systems, please refer to the respective installation instructions.

## Benchmark Suite

To build and run the benchmarking suite, run the following command:

```
./gradlew bootRun
```

### Configuring

To configure the simulator, you can modify the [
`application.properties` file](simulator/src/main/resources/application.yml) in the `simulator` module.

It has two main subsections:

- `scheduler`: The scheduler configuration: which scheduler to use, and its parameters such as the probability of
  dropping messages.
- `scenario`: The scenario configuration: which scenario to run, and its parameters such as conditions to stop the
  simulation.

## Web Interface

The web UI is a simple React application (using NextJS/TypeScript) that allows you to interact with the simulator. It is
a work in progress, but provides useful insights into the behavior of the protocols.

To build the web interface, run the following command **while the simulator is running**:

```
(cd webui && pnpm install && pnpm run kubb:generate && pnpm run build)
```

> [!NOTE]
> The simulator must be running for `kubb:generate` to succeed.

The above command will generate the necessary TypeScript bindings for the simulator and build the web interface. You
only need to run it once.

Then, to start the web server, run the following command:

```
(cd webui && pnpm run start)
```

The UI should then be available at http://localhost:3000

## Running Benchmarks

We currently have the following protocols implemented:

- ~~[PBFT](simulator/src/main/java/byzzbench/simulator/protocols/pbft/PbftReplica.java): The original PBFT protocol, as
  described in
  the [PBFT paper](https://www.microsoft.com/en-us/research/publication/practical-byzantine-fault-tolerance/);~~
- [PBFT-Java](simulator/src/main/java/byzzbench/simulator/protocols/pbft_java/PbftReplica.java): A buggy implementation
  of the PBFT protocol, ported from the [PBFT-Java repository](https://github.com/caojohnny/pbft-java);
- [Fast-HotStuff](simulator/src/main/java/byzzbench/simulator/protocols/fasthotstuff/FastHotStuffReplica.java): An
  unsuccessful attempt at improving the design of HotStuff, as described in
  the [Fast-HotStuff paper](https://arxiv.org/abs/2010.11454);
- [XRPL](simulator/src/main/java/byzzbench/simulator/protocols/xrpl/XRPLReplica.java): [XRP Ledger Consensus Protocol](https://xrpl.org/docs/concepts/consensus-protocol/consensus-research.html)
  implementation;
- *Your protocol here?* :-)

## Documentation

See additional documentation in the [docs](docs) directory.

- [Implementing new BFT Protocols](docs/implementing-protocols.md)
- [Reproducing Schedules](docs/reproducing-schedules.md)
- [User Interface](docs/user-interface.md)
