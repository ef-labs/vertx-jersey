# Swagger Example

This sample shows running jersey inside vert.x using swagger for json or yaml documentation.


## Run It

1. Run from the command line `java -jar target/vertx-jersey-examples-swagger-Version-fat.jar -conf src/test/resources/config.json'
2. Run from inside IDEA creating a JAR Application build configuration with program arguments `-conf src/test/resources/config.json`


Try the following urls in your browser:
* `http://localhost:8080/swagger.json`
* `http://localhost:8080/swagger.yaml`

JSON result

```

{
  "swagger" : "2.0",
  "paths" : {
    "/swagger-test" : {
      "get" : {
        "summary" : "GET MyObject",
        "description" : "",
        "operationId" : "getAsync",
        "produces" : [ "application/json" ],
        "parameters" : [ ],
        "responses" : {
          "200" : {
            "description" : "successful operation",
            "schema" : {
              "$ref" : "#/definitions/MyObject"
            }
          }
        }
      },
      "post" : {
        "summary" : "POST MyObject",
        "description" : "",
        "operationId" : "postJson",
        "consumes" : [ "application/json" ],
        "produces" : [ "application/json" ],
        "parameters" : [ {
          "in" : "body",
          "name" : "body",
          "required" : false,
          "schema" : {
            "$ref" : "#/definitions/MyObject"
          }
        } ],
        "responses" : {
          "200" : {
            "description" : "successful operation",
            "schema" : {
              "$ref" : "#/definitions/MyObject"
            }
          }
        }
      }
    }
  },
  "definitions" : {
    "MyObject" : {
      "type" : "object",
      "properties" : {
        "name" : {
          "type" : "string"
        }
      }
    }
  }
}

```

YAML result

```

---
swagger: "2.0"
paths:
  /swagger-test:
    get:
      summary: "GET MyObject"
      description: ""
      operationId: "getAsync"
      produces:
      - "application/json"
      parameters: []
      responses:
        200:
          description: "successful operation"
          schema:
            $ref: "#/definitions/MyObject"
    post:
      summary: "POST MyObject"
      description: ""
      operationId: "postJson"
      consumes:
      - "application/json"
      produces:
      - "application/json"
      parameters:
      - in: "body"
        name: "body"
        required: false
        schema:
          $ref: "#/definitions/MyObject"
      responses:
        200:
          description: "successful operation"
          schema:
            $ref: "#/definitions/MyObject"
definitions:
  MyObject:
    type: "object"
    properties:
      name:
        type: "string"

```