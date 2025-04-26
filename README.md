# Umlet-MCM - Reloaded

This repo is based on a fork of [Umlet-MCM](https://github.com/umlet-mcm/umlet-mcm) at revision [e80602e](https://github.com/umlet-mcm/umlet-mcm/commit/e80602ee04cc11506b28953d132ceea832381ffe).

Please refer to the documentation there as well.

## Requirements

You'll need:

* Linux, MacOS or WSL
* Java 21 
* NodeJS 20
* Docker and Docker Compose
* GNU make for convenience
* [devbox](https://www.jetify.com/docs/devbox/installing_devbox/) for even more convenience

If you use devbox, you can start a shell and install Java and NodeJS like this:

```
$ devbox shell
```

## Building the project

TL;DR:

```
$ make
```

To build the prod frontend use a custom target:

```
$ make TARGET=prod
```

This will build both the backend and the frontend as well as their docker images.

If you wish to build components individually:


```bash
$ make backend
$ make frontend
$ make docker
```

You can also lint if you'd like:
```bash
make lint
```


## Running the project

Locally:

```bash
$ make
$ make run
```

