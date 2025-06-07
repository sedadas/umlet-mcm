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

# Feature Documentation
## Dataspace Import – MCM Backend

The MCM server ingests a one-line NDJSON payload and persists the entries into Neo4j.

### Import Endpoint

- **Endpoint**: `POST /api/v1/dataspace/import`
- **Request**:
    - `multipart/form-data` with field `file`
- **Processing Steps**:
    1. Validate non-empty upload.
    2. Read first non-blank line using `BufferedReader`.
    3. Parse line into `Map<String,Object>`.
    4. Ensure presence of:
        - `"timestamp"`: ISO-8601 string
        - `"datasources"`: array of objects with `name` (String) and `value` (Number or String)
    5. For each datasource entry, call:
       ```
       graphDBService.upsertDataspaceProperties(name, value);
       ```
       to insert or update nodes in Neo4j.
- **Response**:
    - `200 OK` on success
    - `400 Bad Request` or `500 Internal Server Error` with error details on failure

### Data Persistence

- **GraphDBService** manages Neo4j interactions:
    - Opens sessions via the Neo4j Java driver
    - Executes Cypher (or APOC) queries to create or update nodes labeled `Datasource` with properties for `timestamp` and each `value`.

### Template Endpoint

Template generation is delegated to Conemo’s backend; the MCM service focuses solely on import logic.
