# cpe2pkg

cpe2pkg is a small utility which can help you to identify actual package names when you only have partial information.

Current version only supports Maven.


# How to build

```shell
mvn clean verify
```

# How to run


```shell
cd target/
java -jar cpe2pkg-0.2.0-jar-with-dependencies.jar --top 3 'vendor:( apache poi ) AND product:( poi )'
```

The command above will print top 3 results for given query.